package net.dark.spiritum.block.entity;

import net.dark.spiritum.magic.*;
import net.dark.spiritum.mixin.accessor.BucketItemAccessor;
import net.dark.spiritum.mixin.accessor.EntityAccessor;
import net.dark.spiritum.registry.SpiritumContent;
import net.dark.spiritum.recipe.AlchemyRecipe;
import net.dark.spiritum.registry.SpiritumRegistries;
import net.dark.spiritum.vat.LiquidPotion;
import net.minecraft.block.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.Optional;

@SuppressWarnings("unused")
public class VatBlockEntity extends OfferingBlockEntity {
    private int boilTicks = -1;
    private int maxBoilTicks = 40;
    private Fluid fluid = Fluids.EMPTY;
    private Fluid prevFluid;
    private volatile int color = -1;
    private float prevHeight;
    private float height;

    private float volume;

    private float prevBoilingFactor;
    private float boilingFactor;

    public VatBlockEntity(BlockPos pos, BlockState state) {
        super(SpiritumContent.VAT_ENTITY, pos, state);
    }

    public int getBoilTicks() {
        return boilTicks;
    }

    public int getColor() {
        return color;
    }

    public Fluid getFluid() {
        return Optional.ofNullable(fluid).orElse(Fluids.EMPTY);
    }

    public Fluid getPrevFluid() {
        return Optional.ofNullable(prevFluid).orElse(Fluids.EMPTY);
    }

    @Override
    public void changed() {
        super.changed();
    }

    private int calculateColor() {
        if (offerings.isEmpty()) return 0x3F76E4;

        int red = 0, green = 0, blue = 0, total = 0;
        for (ItemStack stack : offerings) {
            int color = ingredientColor(stack.getItem());
            int count = stack.getCount();
            red += ((color >> 16) & 255) * count;
            green += ((color >> 8) & 255) * count;
            blue += (color & 255) * count;
            total += count;
        }
        return (total == 0 ? 0xFFFFFF : ((red / total) << 16) | ((green / total) << 8) | (blue / total));
    }

    private static int ingredientColor(Item item) {
        if (item == Items.REDSTONE) return 0xC73643;
        if (item == Items.GLOWSTONE_DUST || item == Items.GLOWSTONE || item == Items.HONEY_BOTTLE) return 0xE8C65B;
        if (item == Items.CHARCOAL) return 0x494253;
        if (item == SpiritumContent.SPIRIT_FRAGMENT || item == SpiritumContent.SPIRIT_GEM) return 0x8DDD64;
        if (item == Items.ROTTEN_FLESH || item == SpiritumContent.LIVING_FLESH) return 0xB67473;
        if (item == SpiritumContent.HEX_ASH || item == SpiritumContent.CALX_OF_HADES) return 0x8F70B2;
        if (item == SpiritumContent.ARGENT_INGOT) return 0xB9D5CF;
        return 0xCEC7AD;
    }

    public float getHeight(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevHeight, this.height);
    }

    public float getBoilingFactor(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevBoilingFactor, this.boilingFactor);
    }

    public ActionResult fillVat(ItemStack stack, PlayerEntity player, Hand hand) {
        if (world == null) return ActionResult.PASS;
        if (stack.isEmpty()) return ActionResult.PASS; // avoid liquids that have no bucket item.
        if (fluid != Fluids.EMPTY) return ActionResult.PASS;

        var liquid = Fluids.EMPTY;
        if (stack.getItem() instanceof BucketItem bucketItem)
            liquid = bucketItem.getFluid();
        else if (SpiritumRegistries.VAT_LIQUID.stream().anyMatch(f-> stack.isOf(f.getBucketItem())))
            liquid = SpiritumRegistries.VAT_LIQUID.stream().filter(f-> stack.isOf(f.getBucketItem())).findAny().orElse(Fluids.EMPTY);

        if (liquid == Fluids.EMPTY)
            return ActionResult.PASS;

        changed();
        boilTicks = -1;
        prevFluid = fluid;
        volume = 1000;
        fluid = liquid;
        if (fluid == Fluids.WATER)
            InteractionEffects.splash(world, pos);
        if (stack.getItem() instanceof BucketItem bucketItem)
            ((BucketItemAccessor) bucketItem).spiritum$playEmptyingSound(player, world, pos);
        else
            ((BucketItemAccessor)Items.WATER_BUCKET).spiritum$playEmptyingSound(player,world,pos);

        color = calculateColor();
        checkValidRecipe();

        if (player != null)
            player.setStackInHand(
                hand, ItemUsage.exchangeStack(stack, player, Items.BUCKET.getDefaultStack()));

        return ActionResult.SUCCESS;
    }

    public ActionResult incrementVat(ItemStack stack, PlayerEntity player, Hand hand) { // when a potion is used
        if (world == null) return ActionResult.PASS;
        if (stack.get(DataComponentTypes.POTION_CONTENTS) == null) return ActionResult.PASS;
        if (LiquidPotion.ofFluid(fluid) != null && !stack.get(DataComponentTypes.POTION_CONTENTS).matches(LiquidPotion.ofFluid(fluid))) return ActionResult.PASS;
        if (volume+333.33f > 1000) return ActionResult.PASS;
        volume+=333.33f;
        prevFluid = fluid;
        fluid = LiquidPotion.ofPotion(stack.get(DataComponentTypes.POTION_CONTENTS).potion().orElse(Potions.WATER));
        color = calculateColor();
        world.playSoundAtBlockCenterClient(pos,SoundEvents.ITEM_BOTTLE_EMPTY,SoundCategory.BLOCKS,1f,1f,true);
        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        if (player != null)
            player.setStackInHand(
                hand, ItemUsage.exchangeStack(stack, player, Items.GLASS_BOTTLE.getDefaultStack()));
        return ActionResult.SUCCESS;
    }

    public ActionResult decrementVat(ItemStack stack, PlayerEntity player, Hand hand) { // when a glass bottle is used
        if (world == null) return ActionResult.PASS;
        if (fluid != Fluids.WATER && !(fluid instanceof LiquidPotion)) return ActionResult.PASS;
        if (volume < 333.33f) return ActionResult.PASS;
        volume-=333.33f;
        world.playSoundAtBlockCenterClient(pos,SoundEvents.ITEM_BOTTLE_FILL,SoundCategory.BLOCKS,1f,1f,true);
        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        if (player != null)
            player.setStackInHand(
                hand, ItemUsage.exchangeStack(stack, player,
                    PotionContentsComponent.createStack(Items.POTION, Optional.ofNullable(LiquidPotion.ofFluid(fluid)).orElse(Potions.WATER))
                )
            );
        if (volume <= 0.1)
            emptyVat();
        return ActionResult.SUCCESS;
    }

    public ActionResult emptyVat(ItemStack stack, PlayerEntity player, Hand hand) {
        if (world == null) return ActionResult.PASS;
        if (fluid == Fluids.EMPTY) return ActionResult.PASS;
        if (Math.round(volume) != 1000) return ActionResult.PASS;
        if (fluid.getBucketItem().getDefaultStack().isEmpty()) return ActionResult.PASS;

        emptyVat();
        if (prevFluid.getBucketFillSound().isPresent())
            world.playSoundAtBlockCenterClient(pos,prevFluid.getBucketFillSound().get(),SoundCategory.BLOCKS,1f,1f,true);
        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        if (player != null)
            player.setStackInHand(
                hand, ItemUsage.exchangeStack(stack, player, prevFluid.getBucketItem().getDefaultStack()));
        return ActionResult.SUCCESS;
    }

    private void emptyVat() {
        changed();
        boilTicks = -1;
        prevFluid = fluid;
        fluid = Fluids.EMPTY;
        volume = 0;
        dropAll();
    }

    public void insert(ItemStack stack, PlayerEntity player) {
        if (!canInsert(stack)) return;
        if (world == null) return;
        int amount = player == null || player.isSneaking() ? stack.getCount() : 1;
        add(stack.copyWithCount(amount));
        if (player != null && !player.isCreative()) stack.decrement(amount);
        InteractionEffects.offering(world, pos);
        checkValidRecipe();
        world.playSound(
            null,
            pos,
            SoundEvents.ENTITY_GENERIC_SPLASH,
            SoundCategory.BLOCKS,
            0.25f,
            0.8f
        );
        color = calculateColor();
    }

    public void checkValidRecipe() {
        if (Math.round(volume) != 1000) return;

        var recipes =
            getWorld() instanceof ServerWorld server ?
            server.getRecipeManager().getAllOfType(AlchemyRecipe.TYPE) :
            getWorld() instanceof ClientWorld client ?
            client.getRecipeManager().getSynchronizedRecipes().getAllOfType(AlchemyRecipe.TYPE) :
            new ArrayList<RecipeEntry<AlchemyRecipe>>();

        for (RecipeEntry<AlchemyRecipe> entry : recipes) {
            if (entry.value().matches(offerings) && entry.value().fluid() == fluid)
                boilTicks = maxBoilTicks = entry.value().duration();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canInsert(ItemStack stack) {
        if (world instanceof ServerWorld server) {
            var recipes = server.getRecipeManager().getAllOfType(AlchemyRecipe.TYPE);

            return recipes.stream().filter(recipe -> recipe.value().fluid() == getFluid())
                .anyMatch(r -> r.value().inputs().stream()
                    .anyMatch(input -> stack.isOf(input.item())));
        }
        if (world instanceof ClientWorld client) {
            var recipes = client.getRecipeManager().getSynchronizedRecipes().getAllOfType(AlchemyRecipe.TYPE);

            return recipes.stream().filter(recipe -> recipe.value().fluid() == getFluid())
                .anyMatch(r -> r.value().inputs().stream()
                    .anyMatch(input -> stack.isOf(input.item())));
        }
        return false;
    }

    public static void tickClient(World world, BlockPos pos, BlockState state, VatBlockEntity vat) {
        vat.prevHeight = vat.height;

        if (vat.fluid != Fluids.EMPTY)
            vat.height = MathHelper.lerp(0.25f,vat.height,vat.volume/1000);
        else
            vat.height = MathHelper.lerp(0.5f,vat.height,0f);
        if (vat.height < 1E-5)
            vat.height = 0f;

        vat.prevBoilingFactor = vat.boilingFactor;

        if (vat.boilTicks == 0)
            vat.boilTicks = -1;

        if (vat.boilTicks>0) vat.boilTicks--;

        vat.boilingFactor = Math.max(1E-4f,MathHelper.lerp(0.5f,vat.boilingFactor,vat.boilTicks == -1 ? 1 : vat.boilTicks/(float)vat.maxBoilTicks));

        if (vat.fluid != Fluids.EMPTY) {
            Random random = world.getRandom();
            if (world.getTime() % 20 == 0) {
                world.addParticleClient(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                    0.0,
                    0.04,
                    0.0
                );
            }

            if (Math.round(world.getTime() % (5L * vat.boilingFactor)) == 0 && LiquidPotion.ofFluid(vat.fluid) != null) {
                world.addParticleClient(
                    ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + 0.4 + (0.4 * vat.height),
                    pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
                    0,
                    .05,
                    0
                );
                world.playSoundAtBlockCenterClient(
                    pos,
                    SoundEvents.UI_HUD_BUBBLE_POP,
                    SoundCategory.BLOCKS,
                    Math.min(1f,0.3f/vat.boilingFactor),
                    Math.max(0.5f,MathHelper.sqrt(vat.boilingFactor)),
                    true
                );
            }
        }
    }

    public static void tickServer(World world, BlockPos pos, BlockState state, VatBlockEntity vat) {
        if (vat.fluid == Fluids.EMPTY) return;
        ServerWorld server = (ServerWorld) world;
        Box opening =
                new Box(
                        pos.getX() + .12,
                        pos.getY() + .25,
                        pos.getZ() + .12,
                        pos.getX() + .88,
                        pos.getY() + 1,
                        pos.getZ() + .88);

        for (Entity entity :
                world.getEntitiesByClass(
                        Entity.class, opening, Entity::isAlive)) {
            // Accept only ingredients of at least one known recipe; accidental tools are safe.
            if (entity instanceof ItemEntity itemEntity && !itemEntity.getStack().isEmpty()){
                ItemStack stack = itemEntity.getStack();
                if (!vat.canInsert(stack)) continue;
                vat.insert(stack.copy(), null);
                entity.discard();
            }
            if (vat.fluid != Fluids.EMPTY && !entity.isRemoved()) {
                vat.fluid.getDefaultState().onEntityCollision(world,pos,entity,((EntityAccessor)entity).spiritum$getCollisionHandler());
                ((EntityAccessor)entity).spiritum$getCollisionHandler().runCallbacks(entity);
            }
        }
        if (vat.getBoilTicks() == -1)
            return;

        if (vat.boilTicks == 0) { // craft is finished
            var recipes = server.getRecipeManager().getAllOfType(AlchemyRecipe.TYPE);
            for (var entry : recipes) {
                if (entry.value().matches(vat.offerings) && entry.value().fluid() == vat.fluid) {
                    entry.value().consume(vat.offerings);
                    InteractionEffects.magic(world, Vec3d.ofCenter(pos).add(0, .5, 0), true);
                    vat.emptyVat();
                    ItemEntity result =
                        new ItemEntity(
                            world,
                            pos.getX() + .5,
                            pos.getY() + .65,
                            pos.getZ() + .5,
                            entry.value().result());
                    result.setVelocity(0, .16, 0);
                    world.spawnEntity(result);
                    for (ItemStack byproduct : entry.value().byproducts())
                        world.spawnEntity(
                            new ItemEntity(
                                world,
                                pos.getX() + .5,
                                pos.getY() + 1,
                                pos.getZ() + .5,
                                byproduct.copy()));
                    server.spawnParticles(
                        ParticleTypes.ENCHANT,
                        pos.getX() + .5,
                        pos.getY() + 1,
                        pos.getZ() + .5,
                        25,
                        .25,
                        .25,
                        .25,
                        .1
                    );
                    world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_BREWING_STAND_BREW,
                        SoundCategory.BLOCKS,
                        1,
                        1
                    );
                }
            }
        }

        if (vat.boilTicks>0) vat.boilTicks--;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        boilTicks = view.getInt("BoilTicks", -1);
        volume = view.getFloat("Volume", 0);
        fluid = Optional.of(SpiritumRegistries.VAT_LIQUID.get(Identifier.tryParse(view.getString("Fluid","")))).orElse(Fluids.EMPTY);
        prevFluid = Optional.of(SpiritumRegistries.VAT_LIQUID.get(Identifier.tryParse(view.getString("PrevFluid","")))).orElse(fluid);
        color = view.getInt("Color",-1);
        if (world != null && world.isClient()) world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("BoilTicks", boilTicks);
        view.putInt("Color", color);
        view.putFloat("Volume", volume);
        view.putString("Fluid", SpiritumRegistries.VAT_LIQUID.getId(fluid).toString());
        view.putString("PrevFluid", SpiritumRegistries.VAT_LIQUID.getId(prevFluid).toString());
    }
}
