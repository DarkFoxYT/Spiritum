package net.dark.spiritum.client;

import net.dark.spiritum.block.HexedCandleBlock;
import net.dark.spiritum.block.entity.VatBlockEntity;
import net.dark.spiritum.item.BottleOfHadesItem;
import net.dark.spiritum.item.GemSocketTooltipData;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        BlockEntityRendererFactories.register(ModContent.PEDESTAL_ENTITY, PedestalRenderer::new);
        BlockRenderLayerMap.putBlock(ModContent.HEXED_CANDLE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModContent.ALCHEMY_VAT, BlockRenderLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tint) ->
                        switch (state.get(HexedCandleBlock.FLAME)) {
                            case 2, 4 -> 0x70E9EF;
                            case 5 -> 0xB291FF;
                            default -> 0xFFFFFF;
                        },
                ModContent.HEXED_CANDLE);
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tint) ->
                        world != null
                                        && pos != null
                                        && world.getBlockEntity(pos) instanceof VatBlockEntity vat
                                ? vat.getWaterColor()
                                : 0x3F76E4,
                ModContent.ALCHEMY_VAT);
        ItemTooltipCallback.EVENT.register(
                (stack, context, type, lines) -> {
                    var id = Registries.ITEM.getId(stack.getItem());
                    if (!id.getNamespace().equals("spiritum")) return;
                    if (stack.isOf(ModContent.VOODOO_POPPET)
                            || stack.isOf(ModContent.SENTINEL)
                            || stack.isOf(ModContent.TUNED_BLADE))
                        lines.add(
                                Text.translatable("tooltip.spiritum." + id.getPath())
                                        .formatted(Formatting.GRAY));
                    if (stack.isOf(ModContent.SUMMONERS_RING)
                            || stack.isOf(ModContent.SOULBIND_RING)
                            || stack.isOf(ModContent.WARDING_RING))
                        lines.add(
                                Text.translatable("tooltip.spiritum." + id.getPath())
                                        .formatted(Formatting.GRAY));
                    var gem =
                            stack.isOf(ModContent.SPIRIT_GEM) ? stack : SpiritBinding.socket(stack);
                    SpiritBinding.player(gem)
                            .ifPresent(
                                    uuid ->
                                            lines.add(
                                                    Text.translatable(
                                                                    "tooltip.spiritum.bound",
                                                                    SpiritBinding.playerName(gem))
                                                            .formatted(Formatting.AQUA)));
                    if (stack.isOf(ModContent.BOTTLE_OF_HADES))
                        lines.add(
                                Text.translatable(
                                                "tooltip.spiritum.bottle_contents",
                                                BottleOfHadesItem.count(stack))
                                        .formatted(Formatting.AQUA));
                });
    }
}
