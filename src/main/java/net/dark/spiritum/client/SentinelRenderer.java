package net.dark.spiritum.client;

import net.dark.spiritum.entity.SentinelEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;

/** Temporary argent armor model with visible dormant, slash and thrust poses. */
public class SentinelRenderer
        extends MobEntityRenderer<
                SentinelEntity, SentinelRenderer.State, SentinelRenderer.ArmorModel> {
    public static class State extends LivingEntityRenderState {
        boolean awake;
        int attack;
        float attackRemaining;
    }

    public SentinelRenderer(EntityRendererFactory.Context context) {
        super(context, new ArmorModel(model()), .45f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public Identifier getTexture(State state) {
        return Identifier.of("spiritum", "textures/entity/sentinel.png");
    }

    @Override
    public void updateRenderState(SentinelEntity entity, State state, float delta) {
        super.updateRenderState(entity, state, delta);
        state.awake = entity.isAwake();
        state.attack = entity.attackPose();
        state.attackRemaining = Math.max(0, entity.attackRemaining() - delta);
    }

    private static ModelPart model() {
        ModelData data = new ModelData();
        var root = data.getRoot();
        root.addChild(
                "head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4, -8, -4, 8, 8, 8),
                ModelTransform.origin(0, 0, 0));
        root.addChild(
                "body",
                ModelPartBuilder.create().uv(16, 16).cuboid(-5, 0, -3, 10, 12, 6),
                ModelTransform.NONE);
        root.addChild(
                "left_arm",
                ModelPartBuilder.create().uv(40, 16).cuboid(-1, -2, -2.5f, 4, 13, 5),
                ModelTransform.origin(6, 2, 0));
        var right =
                root.addChild(
                        "right_arm",
                        ModelPartBuilder.create().uv(40, 16).cuboid(-3, -2, -2.5f, 4, 13, 5),
                        ModelTransform.origin(-6, 2, 0));
        right.addChild(
                "blade",
                ModelPartBuilder.create().uv(0, 40).cuboid(-1, 8, -2, 2, 18, 1),
                ModelTransform.NONE);
        root.addChild(
                "left_leg",
                ModelPartBuilder.create().uv(0, 16).cuboid(-2, 0, -2.5f, 4, 12, 5),
                ModelTransform.origin(2.5f, 12, 0));
        root.addChild(
                "right_leg",
                ModelPartBuilder.create().uv(0, 16).cuboid(-2, 0, -2.5f, 4, 12, 5),
                ModelTransform.origin(-2.5f, 12, 0));
        return TexturedModelData.of(data, 64, 64).createModel();
    }

    public static class ArmorModel extends EntityModel<State> {
        ArmorModel(ModelPart root) {
            super(root);
        }

        @Override
        public void setAngles(State state) {
            super.setAngles(state);
            var head = root.getChild("head");
            var left = root.getChild("left_arm");
            var right = root.getChild("right_arm");
            head.pitch = state.awake ? state.pitch * (float) Math.PI / 180 : .3f;
            head.yaw = state.relativeHeadYaw * (float) Math.PI / 180;
            float walk =
                    state.awake
                            ? (float) Math.cos(state.limbSwingAnimationProgress * .65f)
                                    * state.limbSwingAmplitude
                            : 0;
            root.getChild("left_leg").pitch = walk;
            root.getChild("right_leg").pitch = -walk;
            left.pitch = -walk * .6f;
            right.pitch = walk * .6f;
            if (state.attack == 1) {
                float swing = Math.min(1, Math.max(0, (7 - state.attackRemaining) / 4));
                right.pitch = -1.4f + swing * .8f;
                right.roll = -1.1f + swing * 1.9f;
            }
            if (state.attack == 2) {
                right.pitch = -1.7f;
                left.pitch = -.5f;
                root.getChild("body").pitch = .15f;
            }
        }
    }
}
