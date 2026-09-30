package net.dark.spiritum.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.*;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public abstract class OfferingBlockEntity extends BlockEntity {
    protected final List<ItemStack> offerings = new ArrayList<>();

    protected OfferingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public List<ItemStack> getOfferings() {
        return Collections.unmodifiableList(offerings);
    }

    protected void add(ItemStack incoming) {
        for (ItemStack stored : offerings) {
            if (ItemStack.areItemsAndComponentsEqual(stored, incoming)
                    && stored.getCount() < stored.getMaxCount()) {
                int count = Math.min(incoming.getCount(), stored.getMaxCount() - stored.getCount());
                stored.increment(count);
                incoming.decrement(count);
                if (incoming.isEmpty()) break;
            }
        }
        while (!incoming.isEmpty()) {
            int count = Math.min(incoming.getCount(), incoming.getMaxCount());
            offerings.add(incoming.copyWithCount(count));
            incoming.decrement(count);
        }
        changed();
    }

    public void changed() {
        markDirty();
        if (world != null)
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
    }

    protected void dropAll() {
        if (world != null && !world.isClient())
            for (ItemStack stack : offerings) Block.dropStack(world, pos.up(), stack);
        offerings.clear();
        changed();
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        dropAll();
        super.onBlockReplaced(pos, oldState);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        offerings.clear();
        view.getTypedListView("Offerings", ItemStack.CODEC).forEach(offerings::add);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        var list = view.getListAppender("Offerings", ItemStack.CODEC);
        offerings.stream().filter(s -> !s.isEmpty()).forEach(list::add);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
        return createNbt(lookup);
    }
}
