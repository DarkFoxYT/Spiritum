package net.dark.spiritum.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.item.ItemGroup$EntriesImpl")
public class ItemGroupEntriesMixin {
    @WrapOperation(method = "add",at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    private int spiritum$emptyCounts(ItemStack instance, Operation<Integer> original) {
        if (instance.isEmpty())
            return 1;
        return original.call(instance);
    }
}
