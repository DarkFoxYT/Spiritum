package net.dark.spiritum.mixin;

import net.dark.spiritum.registry.ModContent;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class TunedBladeArmMixin {
    @Shadow public ModelPart head;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At("TAIL"))
    private void spiritum$pointBlade(BipedEntityRenderState state, CallbackInfo ci) {
        if (!state.isUsingItem) return;
        Arm arm = state.activeHand == Hand.MAIN_HAND ? state.mainArm : state.mainArm.getOpposite();
        if (!state.getItemStackForArm(arm).isOf(ModContent.TUNED_BLADE)) return;
        ModelPart heldArm = arm == Arm.RIGHT ? rightArm : leftArm;
        heldArm.pitch = -(float) Math.PI / 2 + head.pitch;
        heldArm.yaw = head.yaw;
        heldArm.roll = 0;
    }
}
