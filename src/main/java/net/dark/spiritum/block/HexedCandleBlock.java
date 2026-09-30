package net.dark.spiritum.block;

import com.mojang.serialization.MapCodec;

import net.dark.spiritum.block.entity.CandleBlockEntity;
import net.dark.spiritum.item.SpiritBinding;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class HexedCandleBlock extends BlockWithEntity {
    public static final MapCodec<HexedCandleBlock> CODEC = createCodec(HexedCandleBlock::new);
    // 0: unlit, 1: blaze, 2: soul, 3: harvest, 4: ritual.
    public static final IntProperty FLAME = IntProperty.of("flame", 0, 5);

    public HexedCandleBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FLAME, 0));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FLAME);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CandleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient()
                ? null
                : validateTicker(type, ModContent.CANDLE_ENTITY, CandleBlockEntity::tick);
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(6, 0, 6, 10, 14, 10);
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
        if (state.get(FLAME) != 0) return ActionResult.PASS;
        int flame =
                stack.isOf(Items.BLAZE_POWDER)
                        ? 1
                        : stack.isOf(Items.SOUL_SOIL)
                                ? 2
                                : stack.isOf(Items.FLINT_AND_STEEL)
                                        ? 3
                                        : stack.isOf(ModContent.SPIRIT_FRAGMENT)
                                                ? 4
                                                : stack.isOf(ModContent.SPIRIT_GEM) ? 5 : 0;
        if (flame == 0) return ActionResult.PASS;
        if (!world.isClient() && world.getBlockEntity(pos) instanceof CandleBlockEntity candle) {
            candle.light(flame);
            if (flame == 5) candle.bind(SpiritBinding.player(stack).orElse(null));
            if (flame == 3)
                stack.damage(
                        1,
                        player,
                        hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            else if (!player.isCreative()) stack.decrement(1);
            world.playSound(
                    null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, 1);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(
            BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (state.get(FLAME) == 0) return ActionResult.PASS;
        if (!world.isClient() && world.getBlockEntity(pos) instanceof CandleBlockEntity candle)
            candle.snuff();
        return ActionResult.SUCCESS;
    }
}
