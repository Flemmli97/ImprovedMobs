package io.github.flemmli97.improvedmobs.api.item.impl.move;

import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class KeepDistanceMover implements MoveHandler {

    private final Mob mob;
    private final double maxAttackDistance;
    private int seeTime;

    public KeepDistanceMover(Mob mob) {
        this(mob, 12);
    }

    public KeepDistanceMover(Mob mob, double dist) {
        this.mob = mob;
        this.maxAttackDistance = MoveHandler.defaultRangeOf(mob, dist);
    }

    @Override
    public void move(LivingEntity target, boolean canSee) {
        double dist = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (canSee)
            ++this.seeTime;
        else
            this.seeTime = 0;
        if (dist > this.maxAttackDistance || this.seeTime < 5)
            this.mob.getNavigation().moveTo(target, 1);
        else if (dist <= this.maxAttackDistance * 0.3 && this.mob instanceof PathfinderMob pathfinderMob) {
            Vec3 posAway = DefaultRandomPos.getPosAway(pathfinderMob, 7, 5, target.position());
            if (posAway != null) {
                this.mob.getNavigation().moveTo(posAway.x(), posAway.y(), posAway.z(), 1);
            }
        } else {
            this.mob.getNavigation().stop();
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }
}
