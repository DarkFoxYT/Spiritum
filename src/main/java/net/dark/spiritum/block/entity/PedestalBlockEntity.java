package net.dark.spiritum.block.entity;

import net.dark.spiritum.magic.*;
import net.dark.spiritum.magic.RingMagic;
import net.dark.spiritum.registry.ModContent;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

public class PedestalBlockEntity extends OfferingBlockEntity {
    private String activeRitual = "";
    private int elapsed;
    private int extinguished;
    private boolean sustained;
    private UUID boundPlayer;
    private final Set<UUID> boundPlayers = new LinkedHashSet<>();
    private Vec3d callingOrigin;
    private String callingDimension = "";
    private final List<BlockPos> candles = new ArrayList<>();

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.PEDESTAL_ENTITY, pos, state);
    }

    public String getActiveRitual() {
        return activeRitual;
    }

    public UUID getBoundPlayer() {
        return boundPlayer;
    }

    public Set<UUID> getBoundPlayers() {
        return Collections.unmodifiableSet(boundPlayers);
    }

    public void insert(ItemStack stack, PlayerEntity player) {
        if (!activeRitual.isEmpty()) {
            player.sendMessage(Text.translatable("message.spiritum.ritual_busy"), true);
            return;
        }
        int amount = player.isSneaking() ? stack.getCount() : 1;
        add(stack.copyWithCount(amount));
        if (!player.isCreative()) stack.decrement(amount);
        InteractionEffects.offering(world, pos);
    }

    public void extract(PlayerEntity player) {
        if (!activeRitual.isEmpty()) {
            player.sendMessage(
                    Text.translatable(
                            "message.spiritum.active_ritual",
                            Text.translatable("ritual.spiritum." + activeRitual)),
                    true);
            return;
        }
        if (offerings.isEmpty()) return;
        if (player.isSneaking()) {
            for (ItemStack stack : offerings) player.getInventory().offerOrDrop(stack);
            offerings.clear();
        } else player.getInventory().offerOrDrop(offerings.removeLast());
        InteractionEffects.offering(world, pos);
        changed();
    }

    private List<BlockPos> availableCandles() {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos candidate : BlockPos.iterate(pos.add(-5, -2, -5), pos.add(5, 2, 5))) {
            if (pos.getSquaredDistance(candidate) <= 25
                    && world.isChunkLoaded(candidate)
                    && world.getBlockEntity(candidate) instanceof CandleBlockEntity candle
                    && candle.availableFor(pos)) result.add(candidate.toImmutable());
        }
        result.sort(Comparator.comparingDouble(p -> p.getSquaredDistance(pos)));
        return result;
    }

    private boolean begin(OfferingRecipe recipe, List<BlockPos> selected) {
        Set<UUID> bindings = new LinkedHashSet<>();
        for (BlockPos candlePos : selected) {
            UUID binding = ((CandleBlockEntity) world.getBlockEntity(candlePos)).getBoundPlayer();
            if (binding != null) bindings.add(binding);
        }
        if (bindings.size() > 1 && !recipe.id().equals("withering")) return false;
        boundPlayer = bindings.stream().findFirst().orElse(null);
        if (RitualCandles.requiresOwner(recipe) && boundPlayer == null) return false;
        if (recipe.id().equals("calling")) {
            var player = world.getServer().getPlayerManager().getPlayer(boundPlayer);
            if (player == null || !player.isAlive() || RingMagic.warded(player)) return false;
            callingOrigin = player.getEntityPos();
            callingDimension = player.getEntityWorld().getRegistryKey().getValue().toString();
            player.sendMessage(Text.translatable("message.spiritum.calling_started"), false);
        }
        boundPlayers.clear();
        boundPlayers.addAll(bindings);
        candles.clear();
        candles.addAll(selected);
        for (BlockPos candlePos : candles)
            ((CandleBlockEntity) world.getBlockEntity(candlePos)).claim(pos);
        activeRitual = recipe.id();
        elapsed = 0;
        extinguished = 0;
        sustained = false;
        changed();
        return true;
    }

    private boolean intact() {
        for (int i = extinguished; i < candles.size(); i++) {
            BlockPos candlePos = candles.get(i);
            if (!world.isChunkLoaded(candlePos)
                    || !(world.getBlockEntity(candlePos) instanceof CandleBlockEntity candle)
                    || !candle.availableFor(pos)
                    || !candle.claim(pos)) return false;
        }
        return true;
    }

    private void stop() {
        Warding.remove(world, pos);
        for (BlockPos candlePos : candles)
            if (world.getBlockEntity(candlePos) instanceof CandleBlockEntity candle)
                candle.release(pos);
        activeRitual = "";
        elapsed = 0;
        extinguished = 0;
        sustained = false;
        candles.clear();
        boundPlayer = null;
        boundPlayers.clear();
        callingOrigin = null;
        callingDimension = "";
        changed();
    }

    private boolean callingCancelled() {
        var player = world.getServer().getPlayerManager().getPlayer(boundPlayer);
        return player == null
                || RingMagic.warded(player)
                || !player.isAlive()
                || callingOrigin == null
                || !player.getEntityWorld()
                        .getRegistryKey()
                        .getValue()
                        .toString()
                        .equals(callingDimension)
                || player.getEntityPos().squaredDistanceTo(callingOrigin) > 1.0e-8;
    }

    private void consumeCallingCancellation(OfferingRecipe recipe) {
        InteractionEffects.snuff(world, pos);
        if (recipe.matches(offerings)) recipe.consume(offerings);
        for (BlockPos candlePos : candles)
            if (world.getBlockEntity(candlePos) instanceof CandleBlockEntity candle
                    && candle.flame() >= 4) candle.snuff();
        var player = world.getServer().getPlayerManager().getPlayer(boundPlayer);
        if (player != null)
            player.sendMessage(Text.translatable("message.spiritum.calling_cancelled"), false);
        stop();
    }

    public static void tick(
            World world, BlockPos pos, BlockState state, PedestalBlockEntity pedestal) {
        ServerWorld server = (ServerWorld) world;
        if (pedestal.activeRitual.isEmpty()) {
            if (world.getTime() % 10 != 0 || pedestal.offerings.isEmpty()) return;
            List<BlockPos> available = pedestal.availableCandles();
            // Prefer the more specific ingredient set when offerings could match multiple rites.
            for (OfferingRecipe recipe :
                    MagicRecipes.RITUALS.stream()
                            .sorted(
                                    Comparator.comparingInt((OfferingRecipe r) -> r.id().equals("imp_binding") ? 0 : 1)
                                            .thenComparing(Comparator.comparingInt(
                                                    (OfferingRecipe r) ->
                                                            r.ingredients().values().stream()
                                                                    .mapToInt(Integer::intValue)
                                                                    .sum())
                                            .reversed()))
                            .toList()) {
                if (!recipe.matches(pedestal.offerings)) continue;
                List<BlockPos> selected = RitualCandles.select(world, pos, recipe, available);
                if (!selected.isEmpty() && pedestal.begin(recipe, selected)) break;
            }
            return;
        }
        OfferingRecipe recipe = MagicRecipes.ritual(pedestal.activeRitual);
        if (recipe != null && recipe.id().equals("calling") && pedestal.callingCancelled()) {
            pedestal.consumeCallingCancellation(recipe);
            return;
        }
        if (recipe == null || !pedestal.intact()) {
            pedestal.stop();
            return;
        }
        pedestal.elapsed++;
        if (pedestal.elapsed % 5 == 0) RitualEffects.sigil(server, pos, recipe, pedestal.elapsed);
        if (pedestal.sustained) {
            if (pedestal.elapsed >= 12000) {
                for (BlockPos candlePos : List.copyOf(pedestal.candles))
                    if (world.getBlockEntity(candlePos) instanceof CandleBlockEntity candle) candle.snuff();
                pedestal.stop();
                return;
            }
            RitualEffects.sustain(server, pos, recipe, pedestal.elapsed, pedestal.boundPlayers);
        } else if (recipe.persistent() && pedestal.elapsed >= 60) {
            if (!recipe.matches(pedestal.offerings)) {
                pedestal.stop();
                return;
            }
            recipe.consume(pedestal.offerings);
            pedestal.sustained = true;
            pedestal.elapsed = 0;
            pedestal.changed();
        } else if (!recipe.persistent() && pedestal.elapsed >= 40 + 20 * pedestal.extinguished) {
            BlockPos next = pedestal.candles.get(pedestal.extinguished);
            ((CandleBlockEntity) world.getBlockEntity(next)).snuff();
            pedestal.extinguished++;
            pedestal.changed();
            if (pedestal.extinguished == pedestal.candles.size()) {
                if (recipe.matches(pedestal.offerings)) {
                    recipe.consume(pedestal.offerings);
                    RitualEffects.trigger(server, pos, recipe, pedestal.boundPlayers);
                }
                pedestal.stop();
            }
        }
        if (pedestal.elapsed % 20 == 0) pedestal.markDirty();
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        stop();
        super.onBlockReplaced(pos, oldState);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        activeRitual = view.getString("Ritual", "");
        elapsed = view.getInt("Elapsed", 0);
        extinguished = view.getInt("Extinguished", 0);
        sustained = view.getBoolean("Sustained", false);
        boundPlayer = view.read("BoundPlayer", Uuids.CODEC).orElse(null);
        boundPlayers.clear();
        view.getTypedListView("BoundPlayers", Uuids.CODEC).forEach(boundPlayers::add);
        if (boundPlayers.isEmpty() && boundPlayer != null) boundPlayers.add(boundPlayer);
        callingOrigin = view.read("CallingOrigin", Vec3d.CODEC).orElse(null);
        callingDimension = view.getString("CallingDimension", "");
        candles.clear();
        view.getTypedListView("Candles", BlockPos.CODEC).forEach(candles::add);
        OfferingRecipe recipe = MagicRecipes.ritual(activeRitual);
        if (recipe == null
                || !RitualCandles.validCount(recipe, candles.size())
                || extinguished < 0
                || extinguished >= candles.size()) {
            activeRitual = "";
            candles.clear();
            extinguished = 0;
            sustained = false;
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putString("Ritual", activeRitual);
        view.putInt("Elapsed", elapsed);
        view.putInt("Extinguished", extinguished);
        view.putBoolean("Sustained", sustained);
        view.putNullable("BoundPlayer", Uuids.CODEC, boundPlayer);
        var bindings = view.getListAppender("BoundPlayers", Uuids.CODEC);
        boundPlayers.forEach(bindings::add);
        view.putNullable("CallingOrigin", Vec3d.CODEC, callingOrigin);
        view.putString("CallingDimension", callingDimension);
        var list = view.getListAppender("Candles", BlockPos.CODEC);
        candles.forEach(list::add);
    }
}
