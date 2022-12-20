package io.github.flemmli97.improvedmobs.entities;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FlyingSummonEntity extends RiddenSummonEntity {

    public static final ResourceLocation SUMMONED_FLYING_ID = new ResourceLocation(ImprovedMobs.MODID, "flying_entity");

    private static final EntityDataAccessor<Integer> DATA_ID_SIZE = SynchedEntityData.defineId(FlyingSummonEntity.class, EntityDataSerializers.INT);

    public FlyingSummonEntity(Level level) {
        super(EntityType.PHANTOM, level);
        if (!level.isClientSide) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2);
            this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(24);
        }
        this.moveControl = new CustomFlyMoveControl(this);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_SIZE, -1);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return true;
            }
        };
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        flyingPathNavigation.setCanPassDoors(true);
        return flyingPathNavigation;
    }

    @Override
    public EntityDimensions originDimension(Pose pose) {
        int i = this.entityData.get(DATA_ID_SIZE);
        EntityDimensions entityDimensions = super.originDimension(pose);
        float f = (entityDimensions.width + 0.2f * (float) i) / entityDimensions.width;
        return entityDimensions.scale(f);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if (this.getPassengers().isEmpty()) {
            float widthPassenger = passenger.getBbWidth();
            int w = (int) ((widthPassenger - 0.8f) / 0.2f);
            this.entityData.set(DATA_ID_SIZE, w);
            if (passenger instanceof Mob mob)
                ((FlyingPathNavigation) this.getNavigation()).setCanOpenDoors(mob.getNavigation().getNodeEvaluator().canOpenDoors());
        }
        super.addPassenger(passenger);
    }

    @Override
    public ResourceLocation serverSideID() {
        return SUMMONED_FLYING_ID;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_ID_SIZE.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isInWater()) {
            this.moveRelative(0.02f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8f));
        } else if (this.isInLava()) {
            this.moveRelative(0.02f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            float friction = 0.91f;
            this.moveRelative(0.02f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(friction));
        }
        this.calculateEntityAnimation(this, false);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.8f;
    }

    protected static class CustomFlyMoveControl extends MoveControl {

        private float speed;

        public CustomFlyMoveControl(FlyingSummonEntity mob) {
            super(mob);
            this.speed = 0.1f;
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO || this.mob.getNavigation().isDone()) {
                this.mob.setSpeed(0.0f);
                return;
            }
            if (this.mob.horizontalCollision) {
                this.mob.setYRot(this.mob.getYRot() + 180.0f);
                this.speed = 0.1f;
            }
            Vec3 dir = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
            dir = dir.normalize();
            float rotPre = this.mob.getYRot();

            double horLen = Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z());
            this.mob.setXRot(Mth.wrapDegrees((float) (-(Mth.atan2(dir.y(), horLen) * Mth.RAD_TO_DEG))));
            float newRot = Mth.wrapDegrees((float) (Mth.atan2(dir.z(), dir.x()) * Mth.RAD_TO_DEG));
            this.mob.setYRot(Mth.approachDegrees(rotPre + 90, newRot, 8.0f) - 90.0f);
            this.mob.yBodyRot = this.mob.getYRot();

            float throttleTreshold = 12;
            if (!this.mob.getNavigation().isDone()) {
                BlockPos target = this.mob.getNavigation().getPath().getTarget();
                if (this.mob.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5) < 4.5)
                    throttleTreshold = 3;
            }
            this.speed = Mth.degreesDifferenceAbs(rotPre, this.mob.getYRot()) < throttleTreshold ? Mth.approach(this.speed, 1.8f, 0.009f * (1.8f / this.speed)) : Mth.approach(this.speed, 0.2f, 0.025f);

            Vec3 moveDir = Vec3.directionFromRotation(this.mob.getXRot(), this.mob.getYRot());
            double xDir = this.speed * moveDir.x() * 0.02;
            double yDir = this.speed * moveDir.y() * 0.02;
            double zDir = this.speed * moveDir.z() * 0.02;

            Vec3 delta = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(delta.add(xDir, yDir, zDir));
        }
    }
}
