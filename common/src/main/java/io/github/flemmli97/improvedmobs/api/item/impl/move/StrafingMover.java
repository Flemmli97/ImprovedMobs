package io.github.flemmli97.improvedmobs.api.item.impl.move;

import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class StrafingMover implements MoveHandler {

    private final Mob mob;
    private final double maxAttackDistance;

    private int seeTime;
    private boolean strafingClockwise, strafingBackwards;
    private int strafingTime = -1;

    public StrafingMover(Mob mob) {
        this(mob, 12);
    }

    public StrafingMover(Mob mob, double dist) {
        this.mob = mob;
        this.maxAttackDistance = MoveHandler.defaultRangeOf(mob, dist);
    }

    @Override
    public void move(LivingEntity target, boolean canSee) {
        double dist = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean flag1 = this.seeTime > 0;
        if (canSee != flag1)
            this.seeTime = 0;
        if (canSee)
            ++this.seeTime;
        else
            --this.seeTime;
        if (dist <= this.maxAttackDistance && this.seeTime >= 20) {
            this.mob.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.mob.getNavigation().moveTo(target, 1);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.mob.getRandom().nextFloat() < 0.3D)
                this.strafingClockwise = !this.strafingClockwise;

            if (this.mob.getRandom().nextFloat() < 0.3D)
                this.strafingBackwards = !this.strafingBackwards;

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (dist > this.maxAttackDistance * 0.75)
                this.strafingBackwards = false;
            else if (dist < this.maxAttackDistance * 0.25)
                this.strafingBackwards = true;

            this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.mob.lookAt(target, 30.0F, 30.0F);
        } else {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }
}
