package net.dark.spiritum.client;

import net.dark.spiritum.registry.ModContent;

final class JeiRecipeChecks {
    static boolean upgradeRecipesHiddenInJei() {
        try {
            var field =
                    net.dark.spiritum.client.compat.SpiritumJeiPlugin.class.getDeclaredField(
                            "runtime");
            field.setAccessible(true);
            var runtime = (mezz.jei.api.runtime.IJeiRuntime) field.get(null);
            if (runtime == null) return false;
            var manager = runtime.getRecipeManager();
            var smithing = mezz.jei.api.constants.RecipeTypes.SMITHING;
            boolean registered =
                    manager.createRecipeLookup(smithing)
                            .includeHidden()
                            .get()
                            .anyMatch(
                                    recipe ->
                                            recipe.id()
                                                    .getValue()
                                                    .equals(ModContent.id("retrosonus")));
            boolean hidden =
                    manager.createRecipeLookup(smithing)
                            .get()
                            .noneMatch(
                                    recipe ->
                                            recipe.id()
                                                    .getValue()
                                                    .equals(ModContent.id("retrosonus")));
            boolean ingotHidden =
                    manager.createRecipeLookup(mezz.jei.api.constants.RecipeTypes.CRAFTING)
                            .get()
                            .noneMatch(
                                    recipe ->
                                            recipe.id()
                                                    .getValue()
                                                    .equals(ModContent.id("exalted_ingot")));
            return registered && hidden && ingotHidden;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot inspect JEI upgrade recipes", e);
        }
    }
}
