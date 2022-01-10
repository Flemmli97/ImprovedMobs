package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.mixin.MobEntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class FlyRidingGoal extends Goal {

    public static final ResourceLocation EMPTY = new ResourceLocation(ImprovedMobs.MODID, "empty");
    protected final MobEntity living;
    private int wait = 0, pathCheckWait, flyDelay;
    private boolean start;

    private PathNavigator flyer;

    public FlyRidingGoal(MobEntity living) {
        this.living = living;
        this.flyer = new FlyingPathNavigator(living, living.world) {
            @Override
            public boolean canEntityStandOnPos(BlockPos blockPos) {
                return true;
            }
        };
    }

    @Override
    public boolean shouldExecute() {
        if (this.living.getRidingEntity() instanceof ParrotEntity) {
            return true;
        }
        if (!this.living.isPassenger() && this.living.getAttackTarget() != null && this.living.getAttackTarget().isAlive()) {
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
    public boolean shouldContinueExecuting() {
        if (this.living.getRidingEntity() instanceof ParrotEntity) {
            if (this.living.getAttackTarget() == null)
                this.wait++;
            else
                this.wait = 0;
            return this.wait < 100;
        }
        return false;
    }

    @Override
    public void resetTask() {
        this.living.stopRiding();
        this.living.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 200, 1));
        this.wait = 0;
    }

    @Override
    public void startExecuting() {
        this.start = true;
    }

    @Override
    public void tick() {
        if (this.start) {
            if (!this.living.isPassenger()) {
                ParrotEntity boat = EntityType.PARROT.create(this.living.world);
                BlockPos pos = this.living.getPosition();
                boat.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.living.rotationYaw, this.living.rotationPitch);
                if (this.living.world.hasNoCollisions(boat)) {
                    ((MobEntityMixin) boat).setDeathLootTable(EMPTY);
                    boat.getPersistentData().putBoolean(ImprovedMobs.waterRiding, true);
                    this.living.world.addEntity(boat);
                    this.living.startRiding(boat);
                    this.flyDelay = 0;
                }
            }
            this.start = false;
        }
        Entity entity = this.living.getRidingEntity();
        if (!(entity instanceof ParrotEntity) || !entity.isAlive())
            return;
        if (++this.flyDelay >= 40 && this.isOnLand(entity))
            this.living.stopRiding();
    }

    private boolean checkFlying() {
        Path path = this.living.getNavigator().getPath();
        if (path == null || (path.isFinished() && !path.reachesTarget())) {
            Path flyer = this.flyer.pathfind(this.living.getAttackTarget(), 1);
            double dist = path == null ? this.living.getPosition().manhattanDistance(this.living.getAttackTarget().getPosition()) : path.func_224769_l();
            return flyer != null && (flyer.reachesTarget() || flyer.func_224769_l() < dist);
        }
        return false;
    }

    private boolean isOnLand(Entity riding) {
        if(this.living.getNavigator().noPath() && riding.world.getBlockState(riding.getPosition().down()).getMaterial().isSolid())
            return true;
        if(this.living.getAttackTarget() != null && this.living.getAttackTarget().getDistanceSq(this.living) < 1) {
            return riding.world.getBlockState(riding.getPosition().down()).getMaterial().isSolid();
        }
        return false;
    }
}
