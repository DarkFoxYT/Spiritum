package net.dark.spiritum.entity;

import net.dark.spiritum.magic.InteractionEffects;
import net.dark.spiritum.mixin.ItemPickupOwnerAccessor;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Comparator;

public class ImpDemonEntity extends OwnedDemonEntity {
    private static final TrackedData<ItemStack> CARRIED =
            DataTracker.registerData(ImpDemonEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private int attackCooldown;
    private int deliveryCooldown;

    public ImpDemonEntity(EntityType<? extends ImpDemonEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CARRIED, ItemStack.EMPTY);
    }

    public ItemStack getCarriedStack() {
        return dataTracker.get(CARRIED);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld world) || !isAlive()) return;
        if (attackCooldown > 0) attackCooldown--;
        if (deliveryCooldown > 0) deliveryCooldown--;
        var owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            getNavigation().stop();
            return;
        }
        LivingEntity target = combatTarget(owner);
        if (target != null) {
            approach(target.getEntityPos(), 1.4);
            if (attackCooldown == 0 && squaredDistanceTo(target) < 2.25 && canSee(target)) {
                target.damage(world, DemonDamage.source(world, DemonDamage.IMP, this), 4f);
                attackCooldown = 20;
            }
            return;
        }
        ItemStack carried = getCarriedStack();
        if (!carried.isEmpty()) {
            approach(owner.getEntityPos(), 1.2);
            if (squaredDistanceTo(owner) < 4) {
                ItemStack delivery = carried.copy();
                owner.getInventory().insertStack(delivery);
                InteractionEffects.atHand(owner, false);
                dataTracker.set(CARRIED, ItemStack.EMPTY);
                if (!delivery.isEmpty()) {
                    ItemEntity dropped =
                            new ItemEntity(
                                    world, owner.getX(), owner.getY() + .4, owner.getZ(), delivery);
                    dropped.setOwner(owner.getUuid());
                    dropped.setPickupDelay(20);
                    world.spawnEntity(dropped);
                }
                deliveryCooldown = 40;
            }
            return;
        }
        if (deliveryCooldown == 0) {
            ItemEntity item =
                    world
                            .getEntitiesByClass(
                                    ItemEntity.class,
                                    owner.getBoundingBox().expand(12),
                                    e -> {
                                        var pickupOwner =
                                                ((ItemPickupOwnerAccessor) e)
                                                        .spiritum$getPickupOwner();
                                        return e.isAlive()
                                                && !e.cannotPickup()
                                                && !e.getStack().isEmpty()
                                                && (pickupOwner == null
                                                        || owner.getUuid().equals(pickupOwner));
                                    })
                            .stream()
                            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(this)))
                            .orElse(null);
            if (item != null) {
                approach(item.getEntityPos(), 1.25);
                if (squaredDistanceTo(item) < 1.5 && canSee(item)) {
                    dataTracker.set(CARRIED, item.getStack().copy());
                    item.discard();
                    InteractionEffects.magic(world, getEntityPos().add(0, .3, 0), false);
                }
                return;
            }
        }
        follow(owner);
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!getEntityWorld().isClient() && !getCarriedStack().isEmpty()) {
            getEntityWorld()
                    .spawnEntity(
                            new ItemEntity(
                                    getEntityWorld(),
                                    getX(),
                                    getY(),
                                    getZ(),
                                    getCarriedStack().copy()));
            dataTracker.set(CARRIED, ItemStack.EMPTY);
        }
        super.onDeath(source);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("CarriedItem", ItemStack.OPTIONAL_CODEC, getCarriedStack());
        view.putInt("AttackCooldown", attackCooldown);
        view.putInt("DeliveryCooldown", deliveryCooldown);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        dataTracker.set(
                CARRIED,
                view.read("CarriedItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        attackCooldown = view.getInt("AttackCooldown", 0);
        deliveryCooldown = view.getInt("DeliveryCooldown", 0);
    }
}
