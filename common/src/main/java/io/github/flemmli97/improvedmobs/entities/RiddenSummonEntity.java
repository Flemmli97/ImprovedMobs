package io.github.flemmli97.improvedmobs.entities;

import com.google.common.collect.Iterables;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Entity thats used as vehicle. Has some properties like bounding box expansion and unable to hold items
 */
public abstract class RiddenSummonEntity extends Mob {

    private boolean clearedAI;

    public RiddenSummonEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        if (!level.isClientSide) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(5);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        return spawnData;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getPassengers().contains(source.getEntity()))
            return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    @Override
    public boolean hasEffect(Holder<MobEffect> potion) {
        return false;
    }

    @Nullable
    @Override
    public MobEffectInstance getEffect(Holder<MobEffect> potion) {
        return null;
    }

    @Override
    public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance potioneffect) {
        return false;
    }

    @Override
    public void forceAddEffect(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
    }

    @Override
    protected void dropAllDeathLoot(DamageSource damageSource) {
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public SlotAccess getSlot(int slot) {
        return SlotAccess.NULL;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions = this.originDimension(pose);
        if (this.isVehicle()) {
            Entity e = this.getFirstPassenger();
            if (e != null) {
                EntityDimensions otherDim = e.getDimensions(e.getPose());
                float rideOffsetY = (float) this.getPassengerAttachmentPoint(e, dimensions, 1).y();
                return EntityDimensions.scalable(Math.max(dimensions.width(), otherDim.width()), Math.max(dimensions.height(), otherDim.height() + rideOffsetY));
            }
        }
        return dimensions;
    }

    protected EntityDimensions originDimension(Pose pose) {
        return this.getType().getDimensions();
    }

    public static AABB riddenAABB(AABB thisAABB, AABB other) {
        double d = Math.min(thisAABB.minX, other.minX);
        double e = thisAABB.minY;
        double f = Math.min(thisAABB.minZ, other.minZ);
        double g = Math.max(thisAABB.maxX, other.maxX);
        double h = Math.max(thisAABB.maxY, other.maxY);
        double i = Math.max(thisAABB.maxZ, other.maxZ);
        return new AABB(d, e, f, g, h, i);
    }

    @Override
    public Iterable<ItemStack> getAllSlots() {
        return Iterables.concat();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.clearedAI) {
            this.clearedAI = true;
            this.goalSelector.getAvailableGoals().forEach(WrappedGoal::stop);
            this.removeFreeWill();
        }
        if (!this.isVehicle())
            this.remove(RemovalReason.KILLED);
    }

    @Override
    protected Component getTypeName() {
        return this.getType().getDescription();
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        this.refreshDimensions();
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag compound) {
        CompoundTag tag = super.saveWithoutId(compound);
        tag.getCompound(EntityFlags.TAG_ID).putString(EntityFlags.SERVER_ENTITY_TAG_ID, this.serverSideID().toString());
        return tag;
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    public abstract ResourceLocation serverSideID();

    public boolean doesntCollideWithRidden(Entity rider) {
        return this.level().noCollision(this, riddenAABB(this.getBoundingBox(), rider.getBoundingBox()));
    }
}
