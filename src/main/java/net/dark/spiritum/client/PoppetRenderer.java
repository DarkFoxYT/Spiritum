package net.dark.spiritum.client;

import net.dark.spiritum.entity.PoppetEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PoppetRenderer extends EntityRenderer<PoppetEntity, PoppetRenderer.State> {
    private static final Identifier TEXTURE =
            Identifier.of("spiritum", "textures/entity/poppet.png");
    private final ModelPart[] parts = new ModelPart[6];

    public static class State extends EntityRenderState {
        final Vector3f[] positions = new Vector3f[6];
        final Quaternionf[] rotations = new Quaternionf[6];
    }

    public PoppetRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = .25f;
        float[][] size = {
            {3.84f, 4.8f, 2.24f},
            {3.2f, 2.56f, 2.56f},
            {1.92f, 4.48f, 1.92f},
            {1.92f, 4.48f, 1.92f},
            {1.76f, 4.8f, 1.92f},
            {1.76f, 4.8f, 1.92f}
        };
        for (int i = 0; i < 6; i++) {
            ModelData data = new ModelData();
            float[] s = size[i];
            data.getRoot()
                    .addChild(
                            "piece",
                            ModelPartBuilder.create()
                                    .uv(i * 8, 0)
                                    .cuboid(-s[0] / 2, -s[1] / 2, -s[2] / 2, s[0], s[1], s[2]),
                            ModelTransform.NONE);
            parts[i] = TexturedModelData.of(data, 64, 32).createModel().getChild("piece");
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(PoppetEntity entity, State state, float delta) {
        super.updateRenderState(entity, state, delta);
        for (int i = 0; i < 6; i++) {
            state.positions[i] = new Vector3f(entity.partPosition(i));
            state.rotations[i] = new Quaternionf(entity.partRotation(i));
        }
    }

    @Override
    public void render(
            State state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera) {
        for (int i = 0; i < 6; i++) {
            matrices.push();
            Vector3f p = state.positions[i];
            matrices.translate(p.x, p.y, p.z);
            matrices.multiply(state.rotations[i]);
            queue.submitModelPart(
                    parts[i],
                    matrices,
                    RenderLayers.entityCutout(TEXTURE),
                    state.light,
                    OverlayTexture.DEFAULT_UV,
                    null);
            matrices.pop();
        }
        super.render(state, matrices, queue, camera);
    }
}
