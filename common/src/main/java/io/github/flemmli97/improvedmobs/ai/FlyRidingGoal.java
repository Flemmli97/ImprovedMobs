package io.github.flemmli97.improvedmobs.ai;

import io.github.flemmli97.improvedmobs.entities.FlyingSummonEntity;
import io.github.flemmli97.improvedmobs.mixin.MobEntityMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

public class FlyRidingGoal extends Goal {

    protected final Mob living;
    private int iddle, pathCheckWait, flyDelay, targetDelay;
    private boolean start;

    private final PathNavigation flyer;

    public FlyRidingGoal(Mob living) {
        this.living = living;
        this.flyer = new FlyingPathNavigation(living, living.level) {

            @Override
            protected PathFinder createPathFinder(int maxVisitedNodes) {
                this.nodeEvaluator = new FlyNodeEvalRider();
                this.nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
            }

            @Override
            public boolean isStableDestination(BlockPos blockPos) {
                return true;
            }
        };
    }

    @Override
    public boolean canUse() {
        if (this.living.getVehicle() instanceof FlyingSummonEntity) {
            return true;
        }
        LivingEntity target = this.living.getTarget();
        if (target == null || !target.isAlive() || !this.living.isWithinRestriction(target.blockPosition())) {
            this.targetDelay = 0;
        } else if (!this.living.isPassenger() && ++this.targetDelay > 40) {
            if (--this.pathCheckWait <= 0) {
                this.pathCheckWait = 25;
                if (this.checkFlying()) {
                    this.targetDelay = 0;
                    this.iddle = 0;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.living.getVehicle() instanceof FlyingSummonEntity) {
            if (this.living.getTarget() == null)
                this.iddle++;
            else
                this.iddle = 0;
            return this.iddle < 100;
        }
        return false;
    }

    @Override
    public void stop() {
        this.living.stopRiding();
        this.living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 1));
        this.iddle = 0;
        this.targetDelay = 0;
    }

    @Override
    public void start() {
        this.start = true;
    }

    @Override
    public void tick() {
        if (this.start) {
            if (!this.living.isPassenger()) {
                FlyingSummonEntity boat = new FlyingSummonEntity(this.living.level);
                BlockPos pos = this.living.blockPosition();
                boat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.living.getYRot(), this.living.getXRot());
                if (boat.doesntCollideWithRidden(this.living)) {
                    this.living.level.addFreshEntity(boat);
                    this.living.startRiding(boat);
                    this.flyDelay = 0;
                }
            }
            this.start = false;
        }
        Entity entity = this.living.getVehicle();
        if (!(entity instanceof FlyingSummonEntity) || !entity.isAlive())
            return;
        if (++this.flyDelay >= 40 && this.isOnLand(entity))
            this.living.stopRiding();
    }

    private boolean checkFlying() {
        //Check if entity tries to move somewhere already
        if (Math.abs(this.living.xxa) > 0.005 || Math.abs(this.living.zza) > 0.005)
            return false;
        if (this.living.isNoGravity() || !this.living.isOnGround())
            return false;
        Path path = this.living.getNavigation().getPath();
        if (path == null || (path.isDone() && !path.canReach())) {
            Path ground = this.living.getNavigation().createPath(this.living.getTarget(), 1);
            if (ground != null && ground.canReach())
                return false;
            Path flyer = this.flyer.createPath(this.living.getTarget(), 1);
            double dist = path == null ? this.living.blockPosition().distManhattan(this.living.getTarget().blockPosition()) : path.getDistToTarget();
            return flyer != null && (flyer.canReach() || flyer.getDistToTarget() < dist);
        }
        return false;
    }

    private boolean isOnLand(Entity riding) {
        if (this.living.getNavigation().isDone() && riding.level.getBlockState(riding.blockPosition().below()).getMaterial().isSolid())
            return true;
        LivingEntity target = this.living.getTarget();
        PathNavigation trueNav = ((MobEntityMixin) this.living).getTrueNavigator();
        if (target != null) {
            if (BehaviorUtils.isWithinMeleeAttackRange(this.living, target))
                return riding.level.getBlockState(riding.blockPosition().below()).getMaterial().isSolid();
            if (--this.pathCheckWait > 0)
                return false;
            Path ground = trueNav.createPath(target, 1);
            this.pathCheckWait = 25;
            if (ground != null && ground.canReach())
                return riding.level.getBlockState(riding.blockPosition().below()).getMaterial().isSolid();
        }
        return false;
    }
}
