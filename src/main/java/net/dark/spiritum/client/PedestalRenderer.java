package net.dark.spiritum.client;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.*;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.*;

import java.util.*;

public class PedestalRenderer
        implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.State> {
    public static class State extends BlockEntityRenderState {
        final List<ItemRenderState> items = new ArrayList<>();
        float rotation;
    }

    private final ItemModelManager models;

    public PedestalRenderer(BlockEntityRendererFactory.Context context) {
        models = context.itemModelManager();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(
            PedestalBlockEntity pedestal,
            State state,
            float tickProgress,
            Vec3d cameraPos,
            ModelCommandRenderer.CrumblingOverlayCommand overlay) {
        BlockEntityRenderer.super.updateRenderState(
                pedestal, state, tickProgress, cameraPos, overlay);
        state.rotation =
                pedestal.getWorld() == null
                        ? 0
                        : (pedestal.getWorld().getTime() + tickProgress) * 2;
        state.items.clear();
        // Keep every stack in storage; display up to 16 representatives to bound rendering costs.
        int count = Math.min(16, pedestal.getOfferings().size());
        for (int i = 0; i < count; i++) {
            ItemRenderState item = new ItemRenderState();
            models.clearAndUpdate(
                    item,
                    pedestal.getOfferings().get(i),
                    ItemDisplayContext.GROUND,
                    pedestal.getWorld(),
                    null,
                    i);
            state.items.add(item);
        }
    }

    @Override
    public void render(
            State state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState camera) {
        for (int i = 0; i < state.items.size(); i++) {
            double angle = 2 * Math.PI * i / state.items.size() + Math.toRadians(state.rotation);
            double radius = state.items.size() == 1 ? 0 : .32;
            matrices.push();
            matrices.translate(
                    .5 + Math.cos(angle) * radius,
                    1.15 + .07 * Math.sin(angle * 2),
                    .5 + Math.sin(angle) * radius);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.rotation));
            matrices.scale(.55f, .55f, .55f);
            state.items
                    .get(i)
                    .render(
                            matrices,
                            queue,
                            state.lightmapCoordinates,
                            OverlayTexture.DEFAULT_UV,
                            0);
            matrices.pop();
        }
    }
}
