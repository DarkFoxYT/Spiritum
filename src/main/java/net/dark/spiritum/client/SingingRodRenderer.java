package net.dark.spiritum.client;

import net.dark.spiritum.entity.SingingRodEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SingingRodRenderer extends EntityRenderer<SingingRodEntity, EntityRenderState> {
    private final ModelPart model;

    public SingingRodRenderer(EntityRendererFactory.Context context) {
        super(context);
        ModelData data = new ModelData();
        data.getRoot()
                .addChild(
                        "group",
                        ModelPartBuilder.create()
                                .uv(8, 16)
                                .cuboid(-2, -62, -2, 4, 2, 4)
                                .uv(0, 1)
                                .cuboid(-1, -60, -1, 2, 60, 2)
                                .uv(8, 22)
                                .cuboid(-2, 0, -2, 4, 2, 4),
                        ModelTransform.origin(0, 22, 0));
        model = TexturedModelData.of(data, 64, 64).createModel();
        shadowRadius = .15f;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(
            EntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera) {
        matrices.push();
        matrices.translate(0, 1.5, 0);
        matrices.scale(-1, -1, 1);
        queue.submitModelPart(
                model,
                matrices,
                RenderLayers.entityCutout(
                        Identifier.of("spiritum", "textures/entity/singing_rod.png")),
                state.light,
                OverlayTexture.DEFAULT_UV,
                null);
        matrices.pop();
        super.render(state, matrices, queue, camera);
    }
}
