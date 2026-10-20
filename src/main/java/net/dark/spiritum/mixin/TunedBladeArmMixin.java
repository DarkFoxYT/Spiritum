package net.dark.spiritum.mixin;

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

    @Inject(
            method =
                    "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
            at = @At("TAIL"))
    private void spiritum$pointBlade(BipedEntityRenderState state, CallbackInfo ci) {
        if (!state.isUsingItem) return;
        Arm arm = state.activeHand == Hand.MAIN_HAND ? state.mainArm : state.mainArm.getOpposite();
        if (!(state.getItemStackForArm(arm).getItem()
                instanceof net.dark.spiritum.item.TunedBladeItem)) return;
        ModelPart heldArm = arm == Arm.RIGHT ? rightArm : leftArm;
        ModelPart supportingArm = arm == Arm.RIGHT ? leftArm : rightArm;
        float side = arm == Arm.RIGHT ? 1 : -1;
        heldArm.pitch = -(float) Math.PI / 2 + head.pitch;
        heldArm.yaw = head.yaw - .3f * side;
        heldArm.roll = 0;
        supportingArm.pitch = heldArm.pitch;
        supportingArm.yaw = head.yaw + .6f * side;
        supportingArm.roll = 0;
    }
}
