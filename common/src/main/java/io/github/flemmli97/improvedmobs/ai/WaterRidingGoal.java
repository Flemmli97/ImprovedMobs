package io.github.flemmli97.improvedmobs.ai;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.mixin.MobEntityMixin;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

public class WaterRidingGoal extends Goal {

    public static final ResourceLocation EMPTY = new ResourceLocation(ImprovedMobs.MODID, "empty");
    protected final Mob living;
    private int wait = 0;
    private int jumpingTick;
    private boolean start;

    public WaterRidingGoal(Mob living) {
        this.living = living;
    }

    @Override
    public boolean canUse() {
        if (this.living.getVehicle() instanceof Guardian) {
            return true;
        }
        if (this.living.isInWater() && !this.living.isPassenger() && this.living.getTarget() != null) {
            if (this.wait == 80) {
                this.wait = 0;
                return true;
            }
            this.wait++;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.living.getVehicle() instanceof Guardian) {
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
                Guardian boat = EntityType.GUARDIAN.create(this.living.level);
                BlockPos pos = this.living.blockPosition();
                boat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.living.getYRot(), this.living.getXRot());
                if (this.living.level.noCollision(boat)) {
                    ((MobEntityMixin) boat).setDeathLootTable(EMPTY);
                    EntityFlags.get(boat).rideSummon = true;
                    this.living.level.addFreshEntity(boat);
                    this.living.startRiding(boat);
                }
            }
            this.start = false;
        }
        Entity entity = this.living.getVehicle();
        if (!(entity instanceof Guardian) || !entity.isAlive())
            return;
        this.living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 10, 1, false, false));
        if (this.nearShore(entity, 0)) {
            this.jumpingTick = 20;
            Vec3 facing = entity.getLookAngle().scale(0.5).add(entity.getDeltaMovement());
            entity.setDeltaMovement(new Vec3(facing.x, 1, facing.z));
        }
        if (this.jumpingTick-- > 0) {
            Vec3 facing = entity.getLookAngle().scale(0.5);
            entity.setDeltaMovement(new Vec3(facing.x, entity.getDeltaMovement().y, facing.z));
        }
        if (this.isOnLand(entity)) {
            this.living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2, 5, false, false));
            this.living.getNavigation().stop();
            this.living.stopRiding();
        }
    }

    private boolean isOnLand(Entity riding) {
        if (riding.level.getBlockState(riding.blockPosition()).getMaterial() != Material.WATER) {
            return riding.level.getBlockState(riding.blockPosition().below()).getMaterial().isSolid();
        }
        return false;
    }

    private boolean nearShore(Entity riding, int cliffSize) {
        if (cliffSize < 3) {
            BlockPos pos = riding.blockPosition().relative(riding.getDirection()).above(cliffSize);
            if (riding.level.getBlockState(pos).getMaterial().isSolid() && !riding.level.getBlockState(pos.above()).getMaterial().blocksMotion())
                return true;
            else
                return this.nearShore(riding, cliffSize + 1);
        }
        return false;
    }
}
