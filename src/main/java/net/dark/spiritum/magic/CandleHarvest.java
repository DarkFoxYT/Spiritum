package net.dark.spiritum.magic;

import net.dark.spiritum.block.entity.CandleBlockEntity;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;

public final class CandleHarvest {
    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register(
                (entity, source) -> {
                    if (!(entity.getEntityWorld() instanceof ServerWorld world)) return;
                    ItemStack weapon = source.getWeaponStack();
                    boolean hexblade =
                            source.isDirect() && weapon != null && weapon.isOf(ModContent.HEXBLADE);
                    if (hexblade && entity instanceof PlayerEntity player) {
                        ItemStack gem = SpiritBinding.socket(weapon);
                        if (!gem.isEmpty()) {
                            SpiritBinding.bind(gem, player);
                            SpiritBinding.socket(weapon, gem);
                        }
                    }
                    if (!(entity instanceof MobEntity) && !hexblade) return;
                    boolean nearby = false;
                    for (BlockPos pos :
                            BlockPos.iterate(
                                    entity.getBlockPos().add(-5, -5, -5),
                                    entity.getBlockPos().add(5, 5, 5))) {
                        if (Vec3d.ofCenter(pos).squaredDistanceTo(entity.getEntityPos()) <= 25
                                && world.getBlockEntity(pos) instanceof CandleBlockEntity candle
                                && candle.flame() == 3) {
                            nearby = true;
                            break;
                        }
                    }
                    if (!nearby && !hexblade) return;
                    // One roll per death: 2% two fragments, 20% one fragment, 78% nothing.
                    int roll = world.random.nextInt(100);
                    int count = roll < 2 ? 2 : roll < 22 ? 1 : 0;
                    if (count > 0)
                        world.spawnEntity(
                                new ItemEntity(
                                        world,
                                        entity.getX(),
                                        entity.getY() + .25,
                                        entity.getZ(),
                                        new ItemStack(ModContent.SPIRIT_FRAGMENT, count)));
                });
    }

    private CandleHarvest() {}
}
