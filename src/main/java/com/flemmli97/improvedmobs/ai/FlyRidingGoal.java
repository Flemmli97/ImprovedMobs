package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.mixin.MobEntityMixin;
import com.flemmli97.improvedmobs.utils.GeneralHelperMethods;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
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
    private int iddle, pathCheckWait, flyDelay, targetDelay;
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
        LivingEntity target = this.living.getAttackTarget();
        if (target == null || !target.isAlive()) {
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
    public boolean shouldContinueExecuting() {
        if (this.living.getRidingEntity() instanceof ParrotEntity) {
            if (this.living.getAttackTarget() == null)
                this.iddle++;
            else
                this.iddle = 0;
            return this.iddle < 100;
        }
        return false;
    }

    @Override
    public void resetTask() {
        this.living.stopRiding();
        this.living.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 200, 1));
        this.iddle = 0;
        this.targetDelay = 0;
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
                AttributeModifier mod = this.living.getAttribute(Attributes.MOVEMENT_SPEED)
                        .getModifier(GeneralHelperMethods.attMod);
                if (mod != null)
                    boat.getAttribute(Attributes.FLYING_SPEED)
                            .applyPersistentModifier(new AttributeModifier(GeneralHelperMethods.attMod, "ride.fly.boost", mod.getAmount() * 1.2, AttributeModifier.Operation.ADDITION));
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
        //Check if entity tries to move somewhere already
        LivingEntity target = this.living.getAttackTarget();
        if (Math.abs(this.living.moveForward) > 0.005 || Math.abs(this.living.moveStrafing) > 0.005 || this.living.getDistanceSq(target.getPosX(), target.getPosY(), target.getPosZ()) <= this.getAttackReachSqr(target))
            return false;
        if (this.living.hasNoGravity() || !this.living.isOnGround())
            return false;
        Path path = this.living.getNavigator().getPath();
        if (path == null || (path.isFinished() && !path.reachesTarget())) {
            Path ground = this.living.getNavigator().pathfind(this.living.getAttackTarget(), 1);
            if (ground != null && ground.reachesTarget())
                return false;
            Path flyer = this.flyer.pathfind(target, 1);
            double dist = path == null ? this.living.getPosition().manhattanDistance(target.getPosition()) : path.func_224769_l();
            return flyer != null && (flyer.reachesTarget() || flyer.func_224769_l() < dist);
        }
        return false;
    }

    private boolean isOnLand(Entity riding) {
        if (this.living.getNavigator().noPath() && riding.world.getBlockState(riding.getPosition().down()).getMaterial().isSolid())
            return true;
        LivingEntity target = this.living.getAttackTarget();
        if (target != null && this.living.getDistanceSq(target.getPosX(), target.getPosY(), target.getPosZ()) <= this.getAttackReachSqr(target)) {
            return riding.world.getBlockState(riding.getPosition().down()).getMaterial().isSolid();
        }
        PathNavigator trueNav = ((MobEntityMixin) this.living).getTrueNavigator();
        if (target != null) {
            if (this.living.getDistanceSq(target.getPosX(), target.getPosY(), target.getPosZ()) <= this.getAttackReachSqr(target))
                return riding.world.getBlockState(riding.getPosition().down()).getMaterial().isSolid();
            if (--this.pathCheckWait > 0)
                return false;
            Path ground = trueNav.pathfind(target, 1);
            this.pathCheckWait = 25;
            if (ground != null && ground.reachesTarget())
                return riding.world.getBlockState(riding.getPosition().down()).getMaterial().isSolid();
        }
        return false;
    }

    protected double getAttackReachSqr(LivingEntity attackTarget) {
        return this.living.getWidth() * 2.0F * this.living.getWidth() * 2.0F + attackTarget.getWidth();
    }
}
