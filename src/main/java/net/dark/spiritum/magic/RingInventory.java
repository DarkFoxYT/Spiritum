package net.dark.spiritum.magic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public final class RingInventory implements Inventory {
    private final PlayerEntity player;

    public RingInventory(PlayerEntity player) {
        this.player = player;
    }

    public int size() {
        return 1;
    }

    public boolean isEmpty() {
        return getStack(0).isEmpty();
    }

    public ItemStack getStack(int slot) {
        return ((RingBearer) player).spiritum$getRing();
    }

    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = getStack(slot).split(amount);
        markDirty();
        return result;
    }

    public ItemStack removeStack(int slot) {
        ItemStack result = getStack(slot);
        setStack(slot, ItemStack.EMPTY);
        return result;
    }

    public void setStack(int slot, ItemStack stack) {
        ((RingBearer) player).spiritum$setRing(stack);
        markDirty();
    }

    public void markDirty() {
        player.getInventory().markDirty();
    }

    public boolean canPlayerUse(PlayerEntity user) {
        return user == player;
    }

    public boolean isValid(int slot, ItemStack stack) {
        return RingMagic.isRing(stack);
    }

    public int getMaxCountPerStack() {
        return 1;
    }

    public void clear() {
        setStack(0, ItemStack.EMPTY);
    }
}
