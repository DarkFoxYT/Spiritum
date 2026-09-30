package net.dark.spiritum.entity;

import net.dark.spiritum.registry.ModParticles;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.world.World;

import java.util.*;

public class LemureDemonEntity extends OwnedDemonEntity {
    private int repairTicks;

    public LemureDemonEntity(EntityType<? extends LemureDemonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isFlyingDemon() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld world) || !isAlive()) return;
        setTarget(null); // Lemures never acquire or attack combat targets.
        var owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            getNavigation().stop();
            return;
        }
        follow(owner);
        if (++repairTicks < 400) return;
        repairTicks = 0;
        if (squaredDistanceTo(owner) > 64) return;
        // Five durability total per pulse, apportioned among held tools and worn armor.
        List<ItemStack> damaged = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND
                    || slot == EquipmentSlot.OFFHAND
                    || slot.isArmorSlot()) {
                ItemStack stack = owner.getEquippedStack(slot);
                if (stack.isDamageable() && stack.isDamaged() && !damaged.contains(stack))
                    damaged.add(stack);
            }
        }
        if (damaged.isEmpty()) return;
        int restored = 0, index = 0;
        while (restored < 5 && !damaged.isEmpty()) {
            ItemStack stack = damaged.get(index % damaged.size());
            stack.setDamage(Math.max(0, stack.getDamage() - 1));
            restored++;
            if (!stack.isDamaged()) damaged.remove(stack);
            else index++;
        }
        world.spawnParticles(
                ModParticles.HEXFLAME,
                owner.getX(),
                owner.getY() + 1,
                owner.getZ(),
                12,
                .3,
                .4,
                .3,
                0);
        world.spawnParticles(
                ParticleTypes.HAPPY_VILLAGER,
                owner.getX(),
                owner.getY() + 1,
                owner.getZ(),
                5,
                .3,
                .3,
                .3,
                0);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("RepairTicks", repairTicks);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        repairTicks = view.getInt("RepairTicks", 0);
    }
}
