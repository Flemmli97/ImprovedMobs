package io.github.flemmli97.improvedmobs.entities;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.WaterNavigation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class AquaticSummonEntity extends RiddenSummonEntity {

    public static final ResourceLocation SUMMONED_AQUATIC_ID = new ResourceLocation(ImprovedMobs.MODID, "aquatic_entity");

    private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(AquaticSummonEntity.class, EntityDataSerializers.BOOLEAN);

    private Vec3 leapDir;
    private int leapTick;

    public AquaticSummonEntity(Level level) {
        super(EntityType.GUARDIAN, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
        this.moveControl = new AquaticMoveControl(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_MOVING, false);
    }

    public boolean isMoving() {
        return this.entityData.get(DATA_ID_MOVING);
    }

    protected void setMoving(boolean moving) {
        this.entityData.set(DATA_ID_MOVING, moving);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterNavigation(this, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        --this.leapTick;
        if (this.leapTick < 0 || this.isOnGround())
            this.leapDir = null;
        if (this.isInWaterOrBubble()) {
            this.setAirSupply(300);
        }
    }

    @Override
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.1f, travelVector);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (!this.isMoving() && this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            if (this.leapDir != null) {
                this.move(MoverType.SELF, this.leapDir);
            }
            super.travel(travelVector);
        }
    }

    @Override
    public ResourceLocation serverSideID() {
        return SUMMONED_AQUATIC_ID;
    }

    @Override
    public boolean rideableUnderWater() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GUARDIAN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.8f;
    }

    public void setLeapDir(Vec3 dir) {
        this.leapDir = dir;
        this.leapTick = 20;
    }

    /**
     * Copy of Guardian.GuardianMoveControl
     */
    protected static class AquaticMoveControl extends MoveControl {

        private final AquaticSummonEntity mount;

        public AquaticMoveControl(AquaticSummonEntity mob) {
            super(mob);
            this.mount = mob;
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO || this.mount.getNavigation().isDone()) {
                this.mount.setSpeed(0.0f);
                this.mount.setMoving(false);
                return;
            }
            Vec3 dir = new Vec3(this.wantedX - this.mount.getX(), this.wantedY - this.mount.getY(), this.wantedZ - this.mount.getZ());
            double len = dir.length();
            double e = dir.x / len;
            double f = dir.y / len;
            double g = dir.z / len;
            float h = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
            this.mount.setYRot(this.rotlerp(this.mount.getYRot(), h, 90.0f));
            this.mount.yBodyRot = this.mount.getYRot();
            float i = (float) (this.speedModifier * this.mount.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float j = Mth.lerp(0.125f, this.mount.getSpeed(), i);
            this.mount.setSpeed(j);
            double k = Math.sin((this.mount.tickCount + this.mount.getId()) * 0.5) * 0.05;
            double l = Math.cos(this.mount.getYRot() * Mth.DEG_TO_RAD);
            double m = Math.sin(this.mount.getYRot() * Mth.DEG_TO_RAD);
            double n = Math.sin((this.mount.tickCount + this.mount.getId()) * 0.75) * 0.05;
            this.mount.setDeltaMovement(this.mount.getDeltaMovement().add(k * l * 0.8, n * (m + l) * 0.15 + j * f * 0.1, k * m * 0.8));
            LookControl lookControl = this.mount.getLookControl();
            double o = this.mount.getX() + e * 2.0;
            double p = this.mount.getEyeY() + f / len;
            double q = this.mount.getZ() + g * 2.0;
            double r = lookControl.getWantedX();
            double s = lookControl.getWantedY();
            double t = lookControl.getWantedZ();
            if (!lookControl.isLookingAtTarget()) {
                r = o;
                s = p;
                t = q;
            }
            this.mount.getLookControl().setLookAt(Mth.lerp(0.125, r, o), Mth.lerp(0.125, s, p), Mth.lerp(0.125, t, q), 10.0f, 40.0f);
            this.mount.setMoving(true);
        }
    }
}
