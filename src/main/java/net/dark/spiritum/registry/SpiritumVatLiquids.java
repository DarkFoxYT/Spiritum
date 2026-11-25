package net.dark.spiritum.registry;

import net.dark.spiritum.Spiritum;
import net.dark.spiritum.vat.LiquidPotion;
import net.dark.spiritum.vat.VatLiquid;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpiritumVatLiquids {

    public static final Fluid MILK_LIQUID = Registry.register(SpiritumRegistries.VAT_LIQUID, Spiritum.id("milk"), new VatLiquid(Items.MILK_BUCKET){
        @Override
        protected void onEntityCollision(World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
            super.onEntityCollision(world, pos, entity, handler);
            if (entity instanceof LivingEntity living)
                living.clearStatusEffects();
        }
    });

    public static void initialize() {
        for (Fluid fluid : Registries.FLUID) {
            SpiritumRegistries.VAT_LIQUID.createEntry(fluid);
            Registry.register(SpiritumRegistries.VAT_LIQUID, Registries.FLUID.getId(fluid), fluid);
        }

        RegistryEntryAddedCallback.event(Registries.FLUID).register((ignored, id, fluid)-> {
            SpiritumRegistries.VAT_LIQUID.createEntry(fluid);
            Registry.register(SpiritumRegistries.VAT_LIQUID, id, fluid);
        });


        for (Potion potion : Registries.POTION)
            if (potion != Potions.WATER.value())
                Registry.register(SpiritumRegistries.VAT_LIQUID,Registries.POTION.getId(potion),new LiquidPotion(potion));

        RegistryEntryAddedCallback.event(Registries.POTION).register((ignored,id,potion)->
            Registry.register(SpiritumRegistries.VAT_LIQUID,id,new LiquidPotion(potion))
        );
    }
}
