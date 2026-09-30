package net.dark.spiritum.item;

import net.dark.spiritum.entity.OwnedDemonEntity;
import net.dark.spiritum.magic.InteractionEffects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import org.slf4j.LoggerFactory;

import java.util.*;

public final class BottleOfHadesItem extends Item {
    public static final int CAPACITY = 10;

    public BottleOfHadesItem(Settings settings) {
        super(settings);
    }

    private static List<NbtCompound> stored(ItemStack stack) {
        var data =
                stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        List<NbtCompound> result = new ArrayList<>();
        if (data.get("SpiritumDemons") instanceof NbtList list)
            for (var entry : list)
                if (entry instanceof NbtCompound demon && result.size() < CAPACITY)
                    result.add(demon.copy());
        return result;
    }

    public static int count(ItemStack stack) {
        return stored(stack).size();
    }

    private static void store(ItemStack stack, List<NbtCompound> demons) {
        NbtComponent.set(
                DataComponentTypes.CUSTOM_DATA,
                stack,
                data -> {
                    if (demons.isEmpty()) data.remove("SpiritumDemons");
                    else {
                        NbtList list = new NbtList();
                        demons.forEach(list::add);
                        data.put("SpiritumDemons", list);
                    }
                });
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, !demons.isEmpty());
    }

    public static int capture(ServerWorld world, PlayerEntity player, ItemStack bottle) {
        List<NbtCompound> contents = stored(bottle);
        var demons =
                world.getEntitiesByClass(
                        OwnedDemonEntity.class,
                        player.getBoundingBox().expand(12),
                        demon ->
                                demon.isAlive()
                                        && player.getUuid().equals(demon.getOwnerUuid())
                                        && demon.squaredDistanceTo(player) <= 144
                                        && !demon.hasVehicle()
                                        && !demon.hasPassengers());
        demons.sort(Comparator.comparingDouble(demon -> demon.squaredDistanceTo(player)));
        int captured = 0;
        for (var demon : demons) {
            if (contents.size() == CAPACITY) break;
            try (var reporter =
                    new ErrorReporter.Logging(
                            demon.getErrorReporterContext(),
                            LoggerFactory.getLogger("SpiritumBottle"))) {
                var view = NbtWriteView.create(reporter, world.getRegistryManager());
                if (!demon.saveSelfData(view)) continue;
                contents.add(view.getNbt().copy());
                store(bottle, contents);
                InteractionEffects.magic(world, demon.getEntityPos().add(0, .3, 0), true);
                demon.discard();
                captured++;
            }
        }
        return captured;
    }

    public static int release(ServerWorld world, BlockPos ground, ItemStack bottle) {
        List<NbtCompound> remaining = new ArrayList<>();
        int released = 0;
        for (NbtCompound data : stored(bottle)) {
            var loaded =
                    EntityType.loadEntityWithPassengers(
                            data, world, SpawnReason.TRIGGERED, entity -> entity);
            if (!(loaded instanceof OwnedDemonEntity demon)) {
                remaining.add(data);
                continue;
            }
            boolean spawned = false;
            for (int attempt = 0; attempt < 24; attempt++) {
                double angle = 2 * Math.PI * (released + attempt) / 10;
                double radius = .8 + (attempt / 8) * .8;
                Vec3d destination =
                        Vec3d.ofBottomCenter(ground)
                                .add(
                                        Math.cos(angle) * radius,
                                        attempt >= 16 ? 1 : 0,
                                        Math.sin(angle) * radius);
                if (!world.isSpaceEmpty(
                        demon,
                        demon.getBoundingBox().offset(destination.subtract(demon.getEntityPos()))))
                    continue;
                // Releasing a copied creative bottle must still produce a unique entity identity.
                demon.setUuid(UUID.randomUUID());
                demon.refreshPositionAndAngles(destination.x, destination.y, destination.z, 0, 0);
                demon.setVelocity(Vec3d.ZERO);
                if (world.spawnEntity(demon)) {
                    InteractionEffects.magic(world, destination.add(0, .3, 0), true);
                    released++;
                    spawned = true;
                }
                break;
            }
            if (!spawned) remaining.add(data);
        }
        store(bottle, remaining);
        return released;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!player.isSneaking()) return ActionResult.PASS;
        if (world instanceof ServerWorld server) {
            ItemStack bottle = player.getStackInHand(hand);
            int captured = capture(server, player, bottle);
            if (captured > 0) InteractionEffects.atHand(player, true);

            player.getItemCooldownManager().set(bottle, 20);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = context.getPlayer();
        ItemStack bottle = context.getStack();
        if (player == null || count(bottle) == 0) return ActionResult.PASS;
        if (context.getSide() != Direction.UP) return ActionResult.PASS;
        if (context.getWorld() instanceof ServerWorld server) {
            int released = release(server, context.getBlockPos().up(), bottle);

            player.getItemCooldownManager().set(bottle, 20);
        }
        return ActionResult.SUCCESS;
    }
}
