package net.dark.spiritum.client;

import net.dark.spiritum.block.HexedCandleBlock;
import net.dark.spiritum.item.BottleOfHadesItem;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpiritumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DemonRenderers.initialize();
        SpiritParticles.initialize();
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
                        world != null && pos != null
                                ? BiomeColors.getWaterColor(world, pos)
                                : 0x3F76E4,
                ModContent.ALCHEMY_VAT);
        ItemTooltipCallback.EVENT.register(
                (stack, context, type, lines) -> {
                    var id = Registries.ITEM.getId(stack.getItem());
                    if (!id.getNamespace().equals("spiritum")) return;
                    lines.add(
                            Text.translatable("tooltip.spiritum." + id.getPath())
                                    .formatted(Formatting.DARK_PURPLE));
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
                    if (stack.isOf(ModContent.HEXBLADE)
                            || stack.isOf(ModContent.VOODOO_POPPET)
                            || stack.isOf(ModContent.SOULBIND_RING))
                        lines.add(
                                Text.translatable(
                                                gem.isEmpty()
                                                        ? "tooltip.spiritum.socket_empty"
                                                        : "tooltip.spiritum.socket_full")
                                        .formatted(Formatting.GRAY));
                    if (stack.isOf(ModContent.ARGENT_NEEDLE)
                            && stack.contains(DataComponentTypes.POTION_CONTENTS))
                        lines.add(
                                Text.translatable("tooltip.spiritum.coated")
                                        .formatted(Formatting.AQUA));
                    if (stack.isOf(ModContent.BOTTLE_OF_HADES))
                        lines.add(
                                Text.translatable(
                                                "tooltip.spiritum.bottle_contents",
                                                BottleOfHadesItem.count(stack))
                                        .formatted(Formatting.AQUA));
                });
    }
}
