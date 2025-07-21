package io.github.flemmli97.improvedmobs.common.entities.ai;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.entities.AquaticSummonEntity;
import io.github.flemmli97.improvedmobs.common.entities.RiddenSummonEntity;
import io.github.flemmli97.improvedmobs.common.entities.ai.pathfinding.WaterNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WaterRidingGoal extends Goal {

    public static final ResourceLocation EMPTY = ImprovedMobs.modRes("empty");
    protected final Mob living;
    private int wait = 0;
    private int jumpingTick;
    private boolean start;

    public WaterRidingGoal(Mob living) {
        this.living = living;
    }

    @Override
    public boolean canUse() {
        if (this.living.getVehicle() instanceof AquaticSummonEntity) {
            return true;
        }
        LivingEntity target = this.living.getTarget();
        if (target == null || !target.isAlive() || !this.living.isWithinRestriction(target.blockPosition()) || this.isAquatic())
            return false;
        if (this.living.isInWater() && !this.living.isPassenger()) {
            if (this.wait == 80) {
                this.wait = 0;
                return true;
            }
            this.wait++;
        }
        return false;
    }

    private boolean isAquatic() {
        return this.living.getType().is(EntityTypeTags.AQUATIC) || this.living.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
                || this.living.getNavigation() instanceof WaterBoundPathNavigation || this.living.getNavigation() instanceof WaterNavigation
                || this.living.getNavigation() instanceof AmphibiousPathNavigation;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.living.getVehicle() instanceof AquaticSummonEntity) {
            if (this.living.getTarget() == null)
                this.wait++;
            else
                this.wait = 0;
            return this.wait < 100;
        }
        return false;
    }

    @Override
    public void stop() {
        if (this.living.getVehicle() instanceof RiddenSummonEntity mount)
            mount.scheduledDismount();
        this.wait = 0;
    }

    @Override
    public void start() {
        this.start = true;
    }

    @Override
    public void tick() {
        if (this.start) {
            if (!this.living.isPassenger()) {
                AquaticSummonEntity summon = new AquaticSummonEntity(this.living.level());
                BlockPos pos = this.living.blockPosition();
                summon.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.living.getYRot(), this.living.getXRot());
                if (this.living.level().noCollision(summon)) {
                    this.living.level().addFreshEntity(summon);
                    summon.scheduledRide(this.living);
                }
            }
            this.start = false;
        }
        Entity entity = this.living.getVehicle();
        if (!(entity instanceof AquaticSummonEntity mount) || !mount.isAlive())
            return;
        this.living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 10, 1, false, false));
        if (this.nearShore(entity, 0)) {
            this.jumpingTick = 20;
            Vec3 facing = entity.getLookAngle().scale(0.5).add(entity.getDeltaMovement()).scale(0.7);
            entity.setDeltaMovement(new Vec3(0, 1, 0));
            mount.setLeapDir(new Vec3(facing.x, 0, facing.z));
        }
        if (this.jumpingTick-- > 0) {
            Vec3 facing = entity.getLookAngle().scale(0.5);
            entity.setDeltaMovement(new Vec3(facing.x, entity.getDeltaMovement().y, facing.z));
        }
        if (this.isOnLand(entity)) {
            this.living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 5, false, false));
            this.living.getNavigation().stop();
            mount.scheduledDismount();
        }
    }

    private boolean isOnLand(Entity riding) {
        if (!riding.isInWater()) {
            return riding.level().getBlockState(riding.blockPosition().below()).isSolid();
        }
        return false;
    }

    private boolean nearShore(Entity riding, int cliffSize) {
        if (cliffSize < 3) {
            BlockPos pos = riding.blockPosition().relative(riding.getDirection()).above(cliffSize);
            BlockState state = riding.level().getBlockState(pos);
            if (state.blocksMotion() && !riding.level().getBlockState(pos.above()).blocksMotion())
                return true;
            else
                return this.nearShore(riding, cliffSize + 1);
        }
        return false;
    }
}
