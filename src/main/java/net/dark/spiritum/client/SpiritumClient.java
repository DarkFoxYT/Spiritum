package net.dark.spiritum.client;

import net.dark.spiritum.Spiritum;
import net.dark.spiritum.block.HexedCandleBlock;
import net.dark.spiritum.client.block.PedestalRenderer;
import net.dark.spiritum.client.block.VatRenderer;
import net.dark.spiritum.event.vanilla.ItemPreTooltipCallback;
import net.dark.spiritum.item.BottleOfHadesItem;
import net.dark.spiritum.item.GemSocketTooltipData;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.item.TunedBladeItem;
import net.dark.spiritum.registry.SpiritumContent;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.dark.spiritum.registry.SpiritumVatLiquids;
import net.dark.spiritum.vat.LiquidPotion;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SpiritumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TunedBladeGuidance.initialize();
        DemonRenderers.initialize();
        SpiritParticles.initialize();
        TooltipComponentCallback.EVENT.register(
                data ->
                        data instanceof GemSocketTooltipData gem
                                ? new GemSocketTooltip(gem)
                                : null);
        BlockEntityRendererFactories.register(SpiritumContent.PEDESTAL_ENTITY, PedestalRenderer::new);
        BlockEntityRendererFactories.register(SpiritumContent.VAT_ENTITY, VatRenderer::new);
        BlockRenderLayerMap.putBlock(SpiritumContent.HEXED_CANDLE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(SpiritumContent.ALCHEMY_VAT, BlockRenderLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tint) ->
                        switch (state.get(HexedCandleBlock.FLAME)) {
                            case 2, 4 -> 0x70E9EF;
                            case 5 -> 0xB291FF;
                            default -> 0xFFFFFF;
                        },
                SpiritumContent.HEXED_CANDLE);
        ItemPreTooltipCallback.BEFORE.register(
                (stack, context, type, lines) -> {
                    var id = Registries.ITEM.getId(stack.getItem());
                    if (!id.getNamespace().equals("spiritum")) return;
                    if (stack.isOf(SpiritumContent.VOODOO_POPPET)
                            || stack.isOf(SpiritumContent.SENTINEL)
                            || stack.getItem() instanceof TunedBladeItem)
                        lines.add(
                            getTooltip(Text.translatable("tooltip.spiritum." + id.getPath())
                                        .formatted(Formatting.GRAY)));
                    if (stack.isOf(SpiritumContent.SUMMONERS_RING)) {
                        lines.add(ScreenTexts.EMPTY);
                        lines.add(
                            Text.translatable("tooltip.spiritum.equipped")
                                .formatted(Formatting.GRAY));
                        lines.add(
                            Text.translatable("tooltip.spiritum." + id.getPath()+".1")
                                .formatted(Formatting.BLUE));
                        lines.add(
                            Text.translatable("tooltip.spiritum." + id.getPath()+".2")
                                .formatted(Formatting.BLUE));
                        lines.add(
                            Text.translatable("tooltip.spiritum." + id.getPath()+".3")
                                .formatted(Formatting.RED));
                    }
                    if (stack.isOf(SpiritumContent.SOULBIND_RING) || stack.isOf(SpiritumContent.WARDING_RING)) {
                        lines.add(ScreenTexts.EMPTY);
                        lines.add(
                            Text.translatable("tooltip.spiritum.equipped")
                                .formatted(Formatting.GRAY));
                        lines.add(
                            getTooltip(Text.translatable("tooltip.spiritum." + id.getPath())
                                .formatted(Formatting.DARK_GRAY)));
                    }
                    var gem =
                            stack.isOf(SpiritumContent.SPIRIT_GEM) ? stack : SpiritBinding.socket(stack);
                    SpiritBinding.player(gem)
                            .ifPresent(
                                    uuid ->
                                            lines.add(
                                                    Text.translatable(
                                                                    "tooltip.spiritum.bound",
                                                                    SpiritBinding.playerName(gem))
                                                            .formatted(Formatting.AQUA)));
                    if (stack.isOf(SpiritumContent.BOTTLE_OF_HADES))
                        lines.add(
                                Text.translatable(
                                                "tooltip.spiritum.bottle_contents",
                                                BottleOfHadesItem.count(stack))
                                        .formatted(Formatting.AQUA));
                });
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR,
            Spiritum.id("vat_tooltip"),VatHudTooltip::render);

        for (Fluid fluid : SpiritumRegistries.VAT_LIQUID) {
            if (fluid instanceof LiquidPotion liquidPotion) {
                FluidRenderHandlerRegistry.INSTANCE.register(fluid,
                    new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL, SimpleFluidRenderHandler.WATER_FLOWING) {
                        @Override
                        public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                            if (state.getFluid() instanceof LiquidPotion)
                                return new PotionContentsComponent(Optional.ofNullable(LiquidPotion.ofFluid(fluid)).orElse(Potions.WATER))
                                    .getColor(0x3F76E4);
                            return super.getFluidColor(view, pos, state);
                        }
                    });
                FluidVariantRendering.register(fluid, new FluidVariantRenderHandler(){
                    @Override
                    public void appendTooltip(FluidVariant fluidVariant, List<Text> tooltip, TooltipType tooltipType) {
                        FluidVariantRenderHandler.super.appendTooltip(fluidVariant, tooltip, tooltipType);
                        PotionContentsComponent.buildTooltip(liquidPotion.getPotion().getEffects(), tooltip::add,1f,20);
                    }
                });
            }
        }
        FluidRenderHandlerRegistry.INSTANCE.register(SpiritumVatLiquids.MILK_LIQUID,
            new SimpleFluidRenderHandler(Spiritum.id("block/milk_still"), SimpleFluidRenderHandler.WATER_FLOWING) {
                @Override
                public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                    return 0xFFFFFF;
                }
        });

    }

    public Text getTooltip(Text text) {
        return MinecraftClient.getInstance().isShiftPressed() ? text :
            Text.translatable("tooltip.spiritum.show",Text.translatable("tooltip.spiritum.key").formatted(Formatting.GRAY)).formatted(Formatting.DARK_GRAY);
    }
}
