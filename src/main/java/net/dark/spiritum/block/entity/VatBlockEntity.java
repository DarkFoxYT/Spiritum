package net.dark.spiritum.block.entity;

import net.dark.spiritum.block.AlchemyVatBlock;
import net.dark.spiritum.magic.*;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.block.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public class VatBlockEntity extends OfferingBlockEntity {
    private int boilTicks;

    public VatBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.VAT_ENTITY, pos, state);
    }

    public int getBoilTicks() {
        return boilTicks;
    }

    public void fill() {
        if (world == null) return;
        boilTicks = 600;
        InteractionEffects.splash(world, pos);
        world.setBlockState(
                pos, getCachedState().with(AlchemyVatBlock.FILLED, true), Block.NOTIFY_ALL);
        changed();
    }

    private void empty() {
        boilTicks = 0;
        world.setBlockState(
                pos, getCachedState().with(AlchemyVatBlock.FILLED, false), Block.NOTIFY_ALL);
        changed();
    }

    public static void tick(World world, BlockPos pos, BlockState state, VatBlockEntity vat) {
        if (!state.get(AlchemyVatBlock.FILLED)) return;
        ServerWorld server = (ServerWorld) world;
        if (world.getTime() % 5 == 0)
            server.spawnParticles(
                    ParticleTypes.BUBBLE_POP,
                    pos.getX() + .5,
                    pos.getY() + .8,
                    pos.getZ() + .5,
                    3,
                    .25,
                    .05,
                    .25,
                    .01);
        if (world.getTime() % 20 == 0) {
            server.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + .5,
                    pos.getY() + 1,
                    pos.getZ() + .5,
                    1,
                    .1,
                    0,
                    .1,
                    .01);
            vat.markDirty();
        }
        Box opening =
                new Box(
                        pos.getX() + .12,
                        pos.getY() + .25,
                        pos.getZ() + .12,
                        pos.getX() + .88,
                        pos.getY() + 1.25,
                        pos.getZ() + .88);
        for (ItemEntity entity :
                world.getEntitiesByClass(
                        ItemEntity.class, opening, e -> e.isAlive() && !e.getStack().isEmpty())) {
            // Accept only ingredients of at least one known recipe; accidental tools are safe.
            ItemStack stack = entity.getStack();
            if (MagicRecipes.ALCHEMY.stream()
                    .noneMatch(r -> r.ingredients().containsKey(stack.getItem()))) continue;
            vat.add(stack.copy());
            entity.discard();
        }
        for (OfferingRecipe recipe : MagicRecipes.ALCHEMY)
            if (recipe.matches(vat.offerings)) {
                recipe.consume(vat.offerings);
                InteractionEffects.magic(world, Vec3d.ofCenter(pos).add(0, .5, 0), true);
                vat.dropAll(); // Return extra ingredients rather than silently deleting them.
                vat.empty();
                ItemEntity result =
                        new ItemEntity(
                                world,
                                pos.getX() + .5,
                                pos.getY() + .65,
                                pos.getZ() + .5,
                                new ItemStack(recipe.output()));
                result.setVelocity(0, .16, 0);
                world.spawnEntity(result);
                if (recipe.id().equals("slimeball"))
                    world.spawnEntity(
                            new ItemEntity(
                                    world,
                                    pos.getX() + .5,
                                    pos.getY() + 1,
                                    pos.getZ() + .5,
                                    new ItemStack(Items.GLASS_BOTTLE)));
                server.spawnParticles(
                        ParticleTypes.ENCHANT,
                        pos.getX() + .5,
                        pos.getY() + 1,
                        pos.getZ() + .5,
                        25,
                        .25,
                        .25,
                        .25,
                        .1);
                world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_BREWING_STAND_BREW,
                        SoundCategory.BLOCKS,
                        1,
                        1);
                return;
            }
        if (--vat.boilTicks <= 0) {
            InteractionEffects.snuff(world, pos);
            vat.empty();
            vat.dropAll();
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        boilTicks = view.getInt("BoilTicks", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("BoilTicks", boilTicks);
    }
}
