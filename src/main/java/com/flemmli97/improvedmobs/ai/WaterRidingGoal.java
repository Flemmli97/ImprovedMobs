package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.GuardianEntity;

public class WaterRidingGoal extends Goal {

    MobEntity living;
    int wait = 0;

    public WaterRidingGoal(MobEntity living) {
        this.living = living;
    }

    @Override
    public boolean shouldExecute() {
        if (this.living.isInWater() && !this.living.isPassenger() && this.living.getAttackTarget() != null) {
            if (this.wait == 40)
                return true;
            if (this.wait < 40)
                this.wait++;
            else
                this.wait = 0;
        }
        return false;
    }

    @Override
    public void startExecuting() {

        GuardianEntity boat = EntityType.GUARDIAN.create(this.living.world);
        boat.setLocationAndAngles(this.living.getX(), this.living.getY(), this.living.getZ(), this.living.rotationYaw, this.living.rotationPitch);
        boat.getPersistentData().putBoolean(ImprovedMobs.ridingGuardian, true);
        this.living.world.addEntity(boat);
        this.living.startRiding(boat);
        this.wait = 0;
    }
}
