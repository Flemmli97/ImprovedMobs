package io.github.flemmli97.improvedmobs.api.item.impl.move;

import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class MoveTillMover implements MoveHandler {

    private final Mob mob;
    private final double range;
    private int seeTime;

    public MoveTillMover(Mob mob, double range) {
        this.mob = mob;
        this.range = MoveHandler.defaultRangeOf(mob, range);
    }

    @Override
    public void move(LivingEntity target, boolean canSee) {
        double dist = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (canSee)
            ++this.seeTime;
        else
            this.seeTime = 0;
        if (dist > this.range || this.seeTime < 5) {
            this.mob.getNavigation().moveTo(target, 1);
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }
}
