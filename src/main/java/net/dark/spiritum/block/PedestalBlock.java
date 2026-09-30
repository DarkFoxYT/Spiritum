package net.dark.spiritum.block;

import com.mojang.serialization.MapCodec;

import net.dark.spiritum.block.entity.PedestalBlockEntity;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;

public class PedestalBlock extends BlockWithEntity {
    public static final MapCodec<PedestalBlock> CODEC = createCodec(PedestalBlock::new);

    public PedestalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient()
                ? null
                : validateTicker(type, ModContent.PEDESTAL_ENTITY, PedestalBlockEntity::tick);
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(
                Block.createCuboidShape(2, 0, 2, 14, 9, 14),
                Block.createCuboidShape(0, 9, 0, 16, 16, 16));
    }

    @Override
    protected ActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit) {
        if (stack.isEmpty()) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!world.isClient() && world.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)
            pedestal.insert(stack, player);
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(
            BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && world.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)
            pedestal.extract(player);
        return ActionResult.SUCCESS;
    }
}
