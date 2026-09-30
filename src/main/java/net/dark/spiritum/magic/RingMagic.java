package net.dark.spiritum.magic;

import net.dark.spiritum.entity.OwnedDemonEntity;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public final class RingMagic {
    private static final Identifier REACH = ModContent.id("summoner_reach");
    private static final Identifier ATTACK = ModContent.id("soulbind_attack");
    public static final RegistryKey<DamageType> TRANSFER =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, ModContent.id("soulbind_transfer"));

    public static ItemStack equipped(PlayerEntity player) {
        return player instanceof RingBearer bearer ? bearer.spiritum$getRing() : ItemStack.EMPTY;
    }

    public static boolean isRing(ItemStack stack) {
        return stack.isOf(ModContent.SUMMONERS_RING)
                || stack.isOf(ModContent.SOULBIND_RING)
                || stack.isOf(ModContent.WARDING_RING);
    }

    public static boolean warded(PlayerEntity player) {
        return equipped(player).isOf(ModContent.WARDING_RING);
    }

    private static boolean summoner(PlayerEntity player) {
        return player != null && equipped(player).isOf(ModContent.SUMMONERS_RING);
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(
                server -> server.getPlayerManager().getPlayerList().forEach(RingMagic::update));
    }

    public static void update(PlayerEntity player) {
        if (player.getEntityWorld().isClient()) return;
        modifier(
                player,
                EntityAttributes.ENTITY_INTERACTION_RANGE,
                REACH,
                -1,
                EntityAttributeModifier.Operation.ADD_VALUE,
                summoner(player));
        var ring = equipped(player);
        var bound = boundPlayer(player, ring);
        boolean nearby =
                bound != null
                        && bound != player
                        && bound.isAlive()
                        && bound.getEntityWorld() == player.getEntityWorld()
                        && bound.squaredDistanceTo(player) <= 256;
        modifier(
                player,
                EntityAttributes.ATTACK_DAMAGE,
                ATTACK,
                .15,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                nearby);
    }

    private static ServerPlayerEntity boundPlayer(PlayerEntity wearer, ItemStack ring) {
        if (!ring.isOf(ModContent.SOULBIND_RING)) return null;
        var id = SpiritBinding.player(SpiritBinding.socket(ring)).orElse(null);
        if (id == null) return null;
        return wearer.getEntityWorld().getServer().getPlayerManager().getPlayer(id);
    }

    private static void modifier(
            PlayerEntity player,
            RegistryEntry<EntityAttribute> attribute,
            Identifier id,
            double value,
            EntityAttributeModifier.Operation operation,
            boolean enabled) {
        var instance = player.getAttributeInstance(attribute);
        if (instance == null) return;
        if (!enabled) instance.removeModifier(id);
        else if (!instance.hasModifier(id))
            instance.addTemporaryModifier(new EntityAttributeModifier(id, value, operation));
    }

    /** A 50% defense increase divides received damage by 1.5. */
    public static float adjust(LivingEntity victim, DamageSource source, float amount) {
        if (source.getSource() instanceof OwnedDemonEntity demon) {
            if (summoner(demon.getOwner())) amount *= 1.2f;
            if (victim instanceof PlayerEntity player && warded(player)) amount *= .5f;
        }
        if (victim instanceof OwnedDemonEntity demon && summoner(demon.getOwner())) amount /= 1.5f;
        return amount;
    }

    /** Share after armor and resistance; transfer damage cannot feed back through other rings. */
    public static float share(LivingEntity victim, DamageSource source, float damage) {
        if (!(victim instanceof ServerPlayerEntity target) || damage <= 0 || source.isOf(TRANSFER))
            return damage;
        var wearers =
                List.copyOf(target.getEntityWorld().getServer().getPlayerManager().getPlayerList());
        for (var wearer : wearers) {
            if (wearer == target
                    || !wearer.isAlive()
                    || wearer.isSpectator()
                    || wearer.isCreative()) continue;
            ItemStack ring = equipped(wearer);
            if (!ring.isOf(ModContent.SOULBIND_RING)) continue;
            var bound = SpiritBinding.player(SpiritBinding.socket(ring)).orElse(null);
            if (!target.getUuid().equals(bound)) continue;
            var world = wearer.getEntityWorld();
            DamageSource transfer =
                    new DamageSource(
                            world.getRegistryManager()
                                    .getOrThrow(RegistryKeys.DAMAGE_TYPE)
                                    .getOrThrow(TRANSFER));
            float half = damage * .5f;
            if (wearer.damage(world, transfer, half)) damage -= half;
        }
        return damage;
    }

    private RingMagic() {}
}
