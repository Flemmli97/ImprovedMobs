package io.github.flemmli97.improvedmobs.ai;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.mixin.MobEntityMixin;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.pathfinder.Path;

public class FlyRidingGoal extends Goal {

    public static final ResourceLocation EMPTY = new ResourceLocation(ImprovedMobs.MODID, "empty");
    protected final Mob living;
    private int wait = 0, pathCheckWait, flyDelay;
    private boolean start;

    private PathNavigation flyer;

    public FlyRidingGoal(Mob living) {
        this.living = living;
        this.flyer = new FlyingPathNavigation(living, living.level) {
            @Override
            public boolean isStableDestination(BlockPos blockPos) {
                return true;
            }
        };
    }

    @Override
    public boolean canUse() {
        if (this.living.getVehicle() instanceof Parrot) {
            return true;
        }
        if (!this.living.isPassenger() && this.living.getTarget() != null) {
            if (this.wait >= 80 && --this.pathCheckWait <= 0) {
                if (this.checkFlying()) {
                    this.wait = 0;
                    return true;
                }
                this.pathCheckWait = 25;
            }
            this.wait++;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.living.getVehicle() instanceof Parrot) {
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
        this.living.stopRiding();
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
                Parrot boat = EntityType.PARROT.create(this.living.level);
                BlockPos pos = this.living.blockPosition();
                boat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.living.getYRot(), this.living.getXRot());
                if (this.living.level.noCollision(boat)) {
                    ((MobEntityMixin) boat).setDeathLootTable(EMPTY);
                    EntityFlags.get(boat).rideSummon = true;
                    this.living.level.addFreshEntity(boat);
                    this.living.startRiding(boat);
                    this.flyDelay = 0;
                }
            }
            this.start = false;
        }
        Entity entity = this.living.getVehicle();
        if (!(entity instanceof Parrot) || !entity.isAlive())
            return;
        if (++this.flyDelay >= 40 && this.isOnLand(entity))
            this.living.stopRiding();
    }

    private boolean checkFlying() {
        Path path = this.living.getNavigation().getPath();
        if (path == null || (path.isDone() && !path.canReach())) {
            Path flyer = this.flyer.createPath(this.living.getTarget(), 1);
            double dist = path == null ? this.living.blockPosition().distManhattan(this.living.getTarget().blockPosition()) : path.getDistToTarget();
            return flyer != null && (flyer.canReach() || flyer.getDistToTarget() < dist);
        }
        return false;
    }

    private boolean isOnLand(Entity riding) {
        return this.living.getNavigation().isDone() && riding.level.getBlockState(riding.blockPosition().below()).getMaterial().isSolid();
    }
}