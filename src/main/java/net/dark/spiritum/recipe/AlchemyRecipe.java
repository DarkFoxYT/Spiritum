package net.dark.spiritum.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dark.spiritum.magic.OfferingRecipe;
import net.dark.spiritum.registry.ModContent;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.item.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.*;
import net.minecraft.world.World;

import java.util.*;

/** A datapack recipe for the vat; counts can span multiple dropped stacks. */
public record AlchemyRecipe(List<Input> inputs, ItemStack result, List<ItemStack> byproducts)
        implements Recipe<RecipeInput> {
    public record Input(Item item, int count) {
        public static final Codec<Input> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Registries.ITEM.getCodec().fieldOf("item").forGetter(Input::item),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Input::count)
        ).apply(instance, Input::new));
    }

    public static final MapCodec<AlchemyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Input.CODEC.listOf(1, 64).fieldOf("ingredients").forGetter(AlchemyRecipe::inputs),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(AlchemyRecipe::result),
            ItemStack.VALIDATED_CODEC.listOf().optionalFieldOf("byproducts", List.of())
                    .forGetter(AlchemyRecipe::byproducts)
    ).apply(instance, AlchemyRecipe::new));
    public static final RecipeType<AlchemyRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() { return "spiritum:alchemy"; }
    };
    public static final RecipeSerializer<AlchemyRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<AlchemyRecipe> codec() { return CODEC; }
        @Override
        public PacketCodec<RegistryByteBuf, AlchemyRecipe> packetCodec() {
            return PacketCodecs.registryCodec(CODEC.codec());
        }
    };

    public static void initialize() {
        Registry.register(Registries.RECIPE_TYPE, ModContent.id("alchemy"), TYPE);
        Registry.register(Registries.RECIPE_SERIALIZER, ModContent.id("alchemy"), SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(SERIALIZER);
    }

    public OfferingRecipe offering(String id) {
        Map<Item, Integer> ingredients = new LinkedHashMap<>();
        inputs.forEach(input -> ingredients.merge(input.item(), input.count(), Integer::sum));
        return new OfferingRecipe(id, 0, false, Collections.unmodifiableMap(ingredients), result.copy());
    }

    @Override
    public boolean matches(RecipeInput input, World world) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < input.size(); slot++) stacks.add(input.getStackInSlot(slot));
        return offering("").matches(stacks);
    }

    @Override
    public ItemStack craft(RecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<AlchemyRecipe> getSerializer() { return SERIALIZER; }
    @Override
    public RecipeType<AlchemyRecipe> getType() { return TYPE; }
    @Override
    public boolean isIgnoredInRecipeBook() { return true; }
    @Override
    public IngredientPlacement getIngredientPlacement() { return IngredientPlacement.NONE; }
    @Override
    public RecipeBookCategory getRecipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
}
