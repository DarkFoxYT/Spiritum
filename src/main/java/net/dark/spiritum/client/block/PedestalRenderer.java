package net.dark.spiritum.client.block;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.dark.spiritum.client.AnimationAngles;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.*;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.*;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockRenderView;

import java.util.*;

public class PedestalRenderer
        implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.State> {
    public static class State extends BlockEntityRenderState {
        final List<ItemRenderState> items = new ArrayList<>();
        float rotation;
        BlockRenderView blockRenderView;
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
        state.blockRenderView = pedestal.getWorld();
        state.rotation =
                pedestal.getWorld() == null
                        ? 0
                        : AnimationAngles.pedestal(pedestal.getWorld().getTime(), tickProgress);
        state.items.clear();
        // Keep every stack in storage; display up to 16 representatives to bound rendering costs.
        forItem: for (int i = 0; i < pedestal.getOfferings().size(); i++) {
            for (int j = 0; j < Math.max(1, pedestal.getOfferings().stream().mapToInt(ItemStack::getCount).sum() > 16 ?
                (pedestal.getOfferings().get(i).getCount()*16/pedestal.getOfferings().stream().mapToInt(ItemStack::getCount).sum()) : pedestal.getOfferings().get(i).getCount()
            ); j++) {
                ItemRenderState item = new ItemRenderState();
                models.clearAndUpdate(
                    item,
                    pedestal.getOfferings().get(i).copyWithCount(1),
                    ItemDisplayContext.GROUND,
                    pedestal.getWorld(),
                    null,
                    i);
                state.items.add(item);
                if (state.items.size()>15)
                    break forItem;
            }
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
