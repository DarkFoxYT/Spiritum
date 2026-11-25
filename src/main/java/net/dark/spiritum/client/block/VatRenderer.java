package net.dark.spiritum.client.block;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.dark.spiritum.block.entity.VatBlockEntity;
import net.dark.spiritum.client.AnimationAngles;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

public class VatRenderer
        implements BlockEntityRenderer<VatBlockEntity, VatRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Fluid fluid;
        int color;
        float rotation;
        float height;
        float waveFactor;
        BlockRenderView blockRenderView;
    }

    public VatRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(
            VatBlockEntity vat,
            State state,
            float tickProgress,
            Vec3d cameraPos,
            ModelCommandRenderer.CrumblingOverlayCommand overlay) {

        BlockEntityRenderer.super.updateRenderState(
                vat, state, tickProgress, cameraPos, overlay
        );
        state.blockRenderView = vat.getWorld();
        state.fluid = vat.getFluid() == Fluids.EMPTY ? vat.getPrevFluid() : vat.getFluid();
        state.color = vat.getColor();
        state.height = vat.getHeight(tickProgress);
        state.waveFactor = Math.min((1/(vat.getBoilingFactor(tickProgress)*vat.getBoilingFactor(tickProgress))),3.5f)*0.03f;
        if (vat.getFluid() == Fluids.EMPTY && vat.getPrevFluid() == Fluids.EMPTY)
            state.height = 0f;
        state.rotation =
            vat.getWorld() == null || state.height == 0f
                ? 0
                : AnimationAngles.vat(vat.getWorld().getTime(), tickProgress,30f/Math.max(0.25f,state.fluid.getTickRate(vat.getWorld())));
    }

    @Override
    public void render(
        State state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState camera) {

        if (state.height == 0f)
            return;

        matrices.push();

        FluidVariant fluidVariant = FluidVariant.of(state.fluid);

        queue.submitCustom(matrices, RenderLayers.itemEntityTranslucentCull(FluidVariantRendering.getSprite(fluidVariant).getAtlasId()),
            (pose, vertexConsumer) -> {
            var color = state.color == -1 || state.fluid != Fluids.WATER ?
                FluidVariantRendering.getColor(fluidVariant,state.blockRenderView,state.pos) :
                ColorHelper.fullAlpha(state.color);

            var sprite = FluidVariantRendering.getSprite(fluidVariant);

            if (FabricLoader.getInstance().isModLoaded("sodium") && SpriteUtil.INSTANCE.hasAnimation(sprite))
                SpriteUtil.INSTANCE.markSpriteActive(sprite); // else the sprite is not animated

            var minU = sprite.getFrameU(2/16f);
            var maxU = sprite.getFrameU(14/16f);
            var minV = sprite.getFrameV(2/16f);
            var maxV = sprite.getFrameV(14/16f);

            var light = state.fluid == Fluids.LAVA ? LightmapTextureManager.MAX_LIGHT_COORDINATE : state.lightmapCoordinates;

            vertexConsumer.vertex(pose, 2/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin(state.rotation/360*MathHelper.TAU)*state.waveFactor),
                    2/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(minU,minV).light(light).normal(1,1,1);

            vertexConsumer.vertex(pose,2/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin((state.rotation+90)/360*MathHelper.TAU)*state.waveFactor),
                    14/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(minU,maxV).light(light).normal(1,1,1);

            vertexConsumer.vertex(pose,14/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin((state.rotation+180)/360*MathHelper.TAU)*state.waveFactor),
                    14/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(maxU,maxV).light(light).normal(1,1,1);

            vertexConsumer.vertex(pose,14/16f,
                    (0.4f*state.height)+.4f+(MathHelper.sin((state.rotation+270)/360*MathHelper.TAU)*state.waveFactor),
                    2/16f).overlay(OverlayTexture.DEFAULT_UV)
                .color(color).texture(maxU,minV).light(light).normal(1,1,1);
        });

        matrices.pop();
    }
}
