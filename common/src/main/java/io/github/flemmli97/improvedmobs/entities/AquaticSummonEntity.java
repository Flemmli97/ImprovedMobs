package io.github.flemmli97.improvedmobs.entities;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class AquaticSummonEntity extends RiddenSummonEntity {

    public static final ResourceLocation SUMMONED_AQUATIC_ID = new ResourceLocation(ImprovedMobs.MODID, "aquatic_entity");

    private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(AquaticSummonEntity.class, EntityDataSerializers.BOOLEAN);

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
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.isVehicle())
            this.remove(RemovalReason.KILLED);
        if (this.isInWaterOrBubble()) {
            this.setAirSupply(300);
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.1f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (!this.isMoving() && this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
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

    /**
     * Copy of Guardian.GuardianMoveControl
     */
    protected static class AquaticMoveControl extends MoveControl {

        private final AquaticSummonEntity guardian;

        public AquaticMoveControl(AquaticSummonEntity guardian) {
            super(guardian);
            this.guardian = guardian;
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO || this.guardian.getNavigation().isDone()) {
                this.guardian.setSpeed(0.0f);
                this.guardian.setMoving(false);
                return;
            }
            Vec3 dir = new Vec3(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
            double d = dir.length();
            double e = dir.x / d;
            double f = dir.y / d;
            double g = dir.z / d;
            float h = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
            this.guardian.setYRot(this.rotlerp(this.guardian.getYRot(), h, 90.0f));
            this.guardian.yBodyRot = this.guardian.getYRot();
            float i = (float) (this.speedModifier * this.guardian.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float j = Mth.lerp(0.125f, this.guardian.getSpeed(), i);
            this.guardian.setSpeed(j);
            double k = Math.sin((this.guardian.tickCount + this.guardian.getId()) * 0.5) * 0.05;
            double l = Math.cos(this.guardian.getYRot() * Mth.DEG_TO_RAD);
            double m = Math.sin(this.guardian.getYRot() * Mth.DEG_TO_RAD);
            double n = Math.sin((this.guardian.tickCount + this.guardian.getId()) * 0.75) * 0.05;
            this.guardian.setDeltaMovement(this.guardian.getDeltaMovement().add(k * l, n * (m + l) * 0.25 + j * f * 0.1, k * m));
            LookControl lookControl = this.guardian.getLookControl();
            double o = this.guardian.getX() + e * 2.0;
            double p = this.guardian.getEyeY() + f / d;
            double q = this.guardian.getZ() + g * 2.0;
            double r = lookControl.getWantedX();
            double s = lookControl.getWantedY();
            double t = lookControl.getWantedZ();
            if (!lookControl.isLookingAtTarget()) {
                r = o;
                s = p;
                t = q;
            }
            this.guardian.getLookControl().setLookAt(Mth.lerp(0.125, r, o), Mth.lerp(0.125, s, p), Mth.lerp(0.125, t, q), 10.0f, 40.0f);
            this.guardian.setMoving(true);
        }
    }
}
