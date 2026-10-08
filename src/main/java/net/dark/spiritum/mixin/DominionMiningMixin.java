package net.dark.spiritum.mixin;

import net.dark.spiritum.magic.Dominion;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class DominionMiningMixin {
    @Inject(method = "calcBlockBreakingDelta", at = @At("RETURN"), cancellable = true)
    private void spiritum$obsidian(
            BlockState state,
            PlayerEntity player,
            BlockView view,
            BlockPos pos,
            CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() <= 0
                || !(view instanceof World world)
                || !Dominion.restricted(world, pos, player)) return;
        BlockState obsidian = Blocks.OBSIDIAN.getDefaultState();
        cir.setReturnValue(
                player.getBlockBreakingSpeed(obsidian)
                        / 50f
                        / (player.canHarvest(obsidian) ? 30f : 100f));
    }
}
