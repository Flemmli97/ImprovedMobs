package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.mixin.MobEntityMixin;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class WaterRidingGoal extends Goal {

    public static final ResourceLocation EMPTY = new ResourceLocation(ImprovedMobs.MODID, "empty");
    protected final MobEntity living;
    private int wait = 0;
    private int jumpingTick;
    private boolean start;

    public WaterRidingGoal(MobEntity living) {
        this.living = living;
    }

    @Override
    public boolean shouldExecute() {
        if (this.living.getRidingEntity() instanceof GuardianEntity) {
            return true;
        }
        if (this.living.isInWater() && !this.living.isPassenger() && this.living.getAttackTarget() != null) {
            if (this.wait == 80) {
                this.wait = 0;
                return true;
            }
            this.wait++;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.living.getRidingEntity() instanceof GuardianEntity) {
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
        this.wait = 0;
    }

    @Override
    public void startExecuting() {
        this.start = true;
    }

    @Override
    public void tick() {
        if(this.start) {
            if (!this.living.isPassenger()) {
                GuardianEntity boat = EntityType.GUARDIAN.create(this.living.world);
                BlockPos pos = this.living.getPosition();
                boat.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.living.rotationYaw, this.living.rotationPitch);
                if (this.living.world.hasNoCollisions(boat)) {
                    ((MobEntityMixin) boat).setDeathLootTable(EMPTY);
                    boat.getPersistentData().putBoolean(ImprovedMobs.waterRiding, true);
                    this.living.world.addEntity(boat);
                    this.living.startRiding(boat);
                }
            }
            this.start = false;
        }
        Entity entity = this.living.getRidingEntity();
        if (!(entity instanceof GuardianEntity) || !entity.isAlive())
            return;
        this.living.addPotionEffect(new EffectInstance(Effects.WATER_BREATHING, 10, 1, false, false));
        if (this.nearShore(entity, 0)) {
            this.jumpingTick = 20;
            Vector3d facing = entity.getLookVec().scale(0.5).add(entity.getMotion());
            entity.setMotion(new Vector3d(facing.x, 1, facing.z));
        }
        if (this.jumpingTick-- > 0) {
            Vector3d facing = entity.getLookVec().scale(0.5);
            entity.setMotion(new Vector3d(facing.x, entity.getMotion().y, facing.z));
        }
        if (this.isOnLand(entity)) {
            this.living.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 2, 5, false, false));
            this.living.getNavigator().clearPath();
            this.living.stopRiding();
        }
    }

    private boolean isOnLand(Entity riding) {
        if (riding.world.getBlockState(riding.getPosition()).getMaterial() != Material.WATER) {
            return riding.world.getBlockState(riding.getPosition().down()).getMaterial().isSolid();
        }
        return false;
    }

    private boolean nearShore(Entity riding, int cliffSize) {
        if (cliffSize < 3) {
            BlockPos pos = riding.getPosition().offset(riding.getHorizontalFacing()).up(cliffSize);
            if (riding.world.getBlockState(pos).getMaterial().isSolid() && !riding.world.getBlockState(pos.up()).getMaterial().blocksMovement())
                return true;
            else
                return this.nearShore(riding, cliffSize + 1);
        }
        return false;
    }
}
