package net.dark.spiritum.client;

import net.dark.spiritum.block.entity.VatBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.Comparator;

public class VatHudTooltip {
    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (MinecraftClient.getInstance().player == null)
            return;

        var player = MinecraftClient.getInstance().player;
        var hitResult = player.raycast(player.getAttributeValue(
            EntityAttributes.BLOCK_INTERACTION_RANGE),tickCounter.getDynamicDeltaTicks(), false
        );

        if (!(hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHitResult))
            return;
        if (!(MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.getBlockEntity(blockHitResult.getBlockPos()) instanceof VatBlockEntity vat))
            return;

        if (!vat.getOfferings().isEmpty()) {
            var offerings = new ArrayList<>(vat.getOfferings());
            offerings.sort(Comparator.comparingInt(ItemStack::getCount));
            int height = 4;
            if (vat.getBoilTicks() == -1)
                for (ItemStack stack : offerings) {
                    context.drawItem(stack,context.getScaledWindowWidth()/2+4,context.getScaledWindowHeight()/2+height);
                    context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, stack, context.getScaledWindowWidth()/2+4,context.getScaledWindowHeight()/2+height);
                    height += 16;
                }
            if (vat.getBoilTicks() != -1) {
                String duration = vat.getBoilTicks() % 20 == 0 || vat.getBoilTicks() > 100 ?
                    Integer.toString(vat.getBoilTicks()/20) :
                    Float.toString(Math.round(vat.getBoilTicks()/2f)/10F);
                context.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.literal(duration + "s"), context.getScaledWindowWidth() / 2 + 6, context.getScaledWindowHeight() / 2 - 4,
                    -1, true
                );
            }
        }

    }
}
