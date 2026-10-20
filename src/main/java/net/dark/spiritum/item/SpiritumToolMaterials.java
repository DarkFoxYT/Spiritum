package net.dark.spiritum.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class SpiritumToolMaterials {
    public static final ToolMaterial HEXBLADE = material(ToolMaterial.IRON, "hexblade_repairs");
    public static final ToolMaterial TUNED_BLADE =
            material(ToolMaterial.NETHERITE, "tuned_blade_repairs");

    private SpiritumToolMaterials() {}

    private static ToolMaterial material(ToolMaterial base, String repairTag) {
        return new ToolMaterial(
                base.incorrectBlocksForDrops(),
                base.durability(),
                base.speed(),
                base.attackDamageBonus(),
                base.enchantmentValue(),
                TagKey.of(RegistryKeys.ITEM, Identifier.of("spiritum", repairTag)));
    }
}
