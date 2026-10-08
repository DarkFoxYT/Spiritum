package net.dark.spiritum.registry;

import net.dark.spiritum.block.*;
import net.dark.spiritum.block.entity.*;
import net.dark.spiritum.item.*;
import net.dark.spiritum.recipe.NeedleCoatingRecipe;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ModContent {
    public static final List<Item> ITEMS = new ArrayList<>();

    public static Identifier id(String name) {
        return Identifier.of("spiritum", name);
    }

    private static AbstractBlock.Settings settings(String name, Block copy) {
        return AbstractBlock.Settings.copy(copy)
                .registryKey(RegistryKey.of(RegistryKeys.BLOCK, id(name)));
    }

    private static <T extends Block> T block(String name, T block) {
        Registry.register(Registries.BLOCK, id(name), block);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(name));
        Item item =
                Registry.register(
                        Registries.ITEM,
                        key,
                        new BlockItem(
                                block,
                                new Item.Settings()
                                        .registryKey(key)
                                        .useBlockPrefixedTranslationKey()));
        ITEMS.add(item);
        return block;
    }

    private static Item item(String name) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(name));
        Item item =
                Registry.register(
                        Registries.ITEM, key, new Item(new Item.Settings().registryKey(key)));
        ITEMS.add(item);
        return item;
    }

    public static final Block HEXSTONE =
            block("hexstone", new Block(settings("hexstone", Blocks.CALCITE).strength(1.5f)));
    public static final Block POLISHED_HEXSTONE =
            block(
                    "polished_hexstone",
                    new Block(settings("polished_hexstone", Blocks.CALCITE).strength(1.5f)));
    public static final Block HEXSTONE_BRICKS =
            block(
                    "hexstone_bricks",
                    new Block(settings("hexstone_bricks", Blocks.CALCITE).strength(1.5f)));
    public static final Block HEXSTONE_STAIRS = stairs("hexstone_stairs", HEXSTONE);
    public static final Block HEXSTONE_SLAB = slab("hexstone_slab", HEXSTONE);
    public static final Block HEXSTONE_WALL = wall("hexstone_wall", HEXSTONE);
    public static final Block POLISHED_HEXSTONE_STAIRS =
            stairs("polished_hexstone_stairs", POLISHED_HEXSTONE);
    public static final Block POLISHED_HEXSTONE_SLAB =
            slab("polished_hexstone_slab", POLISHED_HEXSTONE);
    public static final Block POLISHED_HEXSTONE_WALL =
            wall("polished_hexstone_wall", POLISHED_HEXSTONE);
    public static final Block HEXSTONE_BRICK_STAIRS =
            stairs("hexstone_brick_stairs", HEXSTONE_BRICKS);
    public static final Block HEXSTONE_BRICK_SLAB = slab("hexstone_brick_slab", HEXSTONE_BRICKS);
    public static final Block HEXSTONE_BRICK_WALL = wall("hexstone_brick_wall", HEXSTONE_BRICKS);

    private static Block stairs(String name, Block base) {
        return block(name, new StairsBlock(base.getDefaultState(), settings(name, base)));
    }

    private static Block slab(String name, Block base) {
        return block(name, new SlabBlock(settings(name, base)));
    }

    private static Block wall(String name, Block base) {
        return block(name, new WallBlock(settings(name, base)));
    }

    public static final HexedCandleBlock HEXED_CANDLE =
            block(
                    "hexed_candle",
                    new HexedCandleBlock(
                            settings("hexed_candle", Blocks.CANDLE)
                                    .luminance(
                                            s ->
                                                    s.get(HexedCandleBlock.FLAME) == 0
                                                            ? 0
                                                            : s.get(HexedCandleBlock.FLAME) == 5
                                                                    ? 15
                                                                    : 9)));
    public static final PedestalBlock RITUAL_PEDESTAL =
            block(
                    "ritual_pedestal",
                    new PedestalBlock(settings("ritual_pedestal", Blocks.STONE).strength(2.5f)));
    public static final AlchemyVatBlock ALCHEMY_VAT =
            block(
                    "alchemy_vat",
                    new AlchemyVatBlock(
                            settings("alchemy_vat", Blocks.CAULDRON)
                                    .strength(2.5f)
                                    .luminance(s -> s.get(AlchemyVatBlock.FILLED) ? 5 : 0)));
    public static final Item SPIRIT_FRAGMENT = item("spirit_fragment");
    public static final Item ARGENT_NUGGET = item("argent_nugget");
    public static final Item ARGENT_INGOT = item("argent_ingot");
    public static final Item SPIRIT_GEM =
            custom("spirit_gem", SpiritGemItem::new, new Item.Settings());
    public static final Item LIVING_FLESH = item("living_flesh");
    public static final Item HEX_ASH = item("hex_ash");
    public static final Item CALX_OF_HADES = item("calx_of_hades");
    public static final Item VOODOO_POPPET =
            custom("voodoo_poppet", PoppetItem::new, new Item.Settings().maxDamage(6));
    public static final Item SENTINEL =
            custom("sentinel", SentinelItem::new, new Item.Settings().maxCount(1));
    public static final Item ARGENT_NEEDLE =
            custom("argent_needle", ArgentNeedleItem::new, new Item.Settings().maxCount(1));
    public static final Item HEXBLADE = hexblade();
    public static final Item TUNED_BLADE =
            custom(
                    "tuned_blade",
                    TunedBladeItem::new,
                    new Item.Settings().sword(ToolMaterial.NETHERITE, 3.0f, -2.4f).fireproof());
    public static final Item BOTTLE_OF_HADES =
            custom("bottle_of_hades", BottleOfHadesItem::new, new Item.Settings().maxCount(1));
    public static final Item SUMMONERS_RING =
            custom("summoners_ring", Item::new, new Item.Settings().maxCount(1));
    public static final Item SOULBIND_RING =
            custom("soulbind_ring", SoulbindRingItem::new, new Item.Settings().maxCount(1));
    public static final Item WARDING_RING =
            custom("warding_ring", Item::new, new Item.Settings().maxCount(1));

    private static Item hexblade() {
        var key = RegistryKey.of(RegistryKeys.ITEM, id("hexblade"));
        Item item =
                Registry.register(
                        Registries.ITEM,
                        key,
                        new GemSocketItem(
                                new Item.Settings()
                                        .registryKey(key)
                                        .sword(ToolMaterial.IRON, 3.0f, -2.4f)));
        ITEMS.add(item);
        return item;
    }

    public static final BlockEntityType<CandleBlockEntity> CANDLE_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    id("hexed_candle"),
                    FabricBlockEntityTypeBuilder.create(CandleBlockEntity::new, HEXED_CANDLE)
                            .build());
    public static final BlockEntityType<PedestalBlockEntity> PEDESTAL_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    id("ritual_pedestal"),
                    FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, RITUAL_PEDESTAL)
                            .build());
    public static final BlockEntityType<VatBlockEntity> VAT_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    id("alchemy_vat"),
                    FabricBlockEntityTypeBuilder.create(VatBlockEntity::new, ALCHEMY_VAT).build());

    public static void initialize() {
        NeedleCoatingRecipe.initialize();
        Registry.register(
                Registries.ITEM_GROUP,
                id("spiritum"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.spiritum"))
                        .icon(() -> new ItemStack(SPIRIT_GEM))
                        .entries((context, entries) -> ITEMS.forEach(entries::add))
                        .build());
    }

    private static Item custom(
            String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        var key = RegistryKey.of(RegistryKeys.ITEM, id(name));
        Item item =
                Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
        ITEMS.add(item);
        return item;
    }

    private ModContent() {}
}
