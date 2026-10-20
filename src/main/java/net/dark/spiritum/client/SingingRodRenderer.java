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
                        "rod",
                        ModelPartBuilder.create()
                                .uv(0, 0)
                                .cuboid(-2, 0, -2, 4, 2, 4)
                                .uv(0, 0)
                                .cuboid(-1, 2, -1, 2, 28, 2)
                                .uv(0, 0)
                                .cuboid(-2, 30, -2, 4, 2, 4),
                        ModelTransform.NONE);
        model = TexturedModelData.of(data, 16, 64).createModel();
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
        queue.submitModelPart(
                model,
                matrices,
                RenderLayers.entityCutout(Identifier.ofVanilla("textures/block/iron_block.png")),
                state.light,
                OverlayTexture.DEFAULT_UV,
                null);
        super.render(state, matrices, queue, camera);
    }
}
