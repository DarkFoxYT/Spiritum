package net.dark.spiritum.item;

import net.dark.spiritum.network.TunedBladeTargetPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TunedBladeItem extends GemSocketItem {
    public TunedBladeItem(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean acceptsGem(ItemStack stack) {
        return super.acceptsGem(stack) && SpiritBinding.player(stack).isPresent();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack gem = SpiritBinding.socket(user.getStackInHand(hand));
        if (gem.isEmpty() || user.isSneaking()) return super.use(world, user, hand);
        var binding = SpiritBinding.player(gem);
        if (binding.isEmpty()) return ActionResult.PASS;
        if (!world.isClient()
                && world.getServer().getPlayerManager().getPlayer(binding.get()) == null)
            return ActionResult.PASS;
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof ServerPlayerEntity player)) return;
        var target =
                SpiritBinding.player(SpiritBinding.socket(stack))
                        .map(
                                id ->
                                        player.getEntityWorld()
                                                .getServer()
                                                .getPlayerManager()
                                                .getPlayer(id))
                        .orElse(null);
        if (target == null) {
            player.stopUsingItem();
            return;
        }
        Vec3d direction = target.getEyePos().subtract(player.getEyePos());
        if (direction.lengthSquared() < .0001) return;
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch =
                (float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalLength()));
        if (ServerPlayNetworking.canSend(player, TunedBladeTargetPayload.ID))
            ServerPlayNetworking.send(player, new TunedBladeTargetPayload(yaw, pitch));
    }
}
