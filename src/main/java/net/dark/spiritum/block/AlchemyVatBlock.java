package net.dark.spiritum.block;

import com.mojang.serialization.MapCodec;

import net.dark.spiritum.block.entity.VatBlockEntity;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.*;
import net.minecraft.world.*;

public class AlchemyVatBlock extends BlockWithEntity {
    public static final MapCodec<AlchemyVatBlock> CODEC = createCodec(AlchemyVatBlock::new);
    public static final BooleanProperty FILLED = BooleanProperty.of("filled");

    public AlchemyVatBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FILLED, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VatBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient()
                ? null
                : validateTicker(type, ModContent.VAT_ENTITY, VatBlockEntity::tick);
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(
                Block.createCuboidShape(0, 0, 0, 3, 6, 3),
                Block.createCuboidShape(13, 0, 0, 16, 6, 3),
                Block.createCuboidShape(0, 0, 13, 3, 6, 16),
                Block.createCuboidShape(13, 0, 13, 16, 6, 16),
                Block.createCuboidShape(0, 6, 0, 16, 7, 16),
                Block.createCuboidShape(0, 7, 0, 2, 16, 16),
                Block.createCuboidShape(14, 7, 0, 16, 16, 16),
                Block.createCuboidShape(2, 7, 0, 14, 16, 2),
                Block.createCuboidShape(2, 7, 14, 14, 16, 16));
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
        if (!stack.isOf(Items.WATER_BUCKET) || state.get(FILLED)) return ActionResult.PASS;
        if (!world.isClient() && world.getBlockEntity(pos) instanceof VatBlockEntity vat) {
            vat.fill();
            player.setStackInHand(
                    hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
        }
        return ActionResult.SUCCESS;
    }
}
