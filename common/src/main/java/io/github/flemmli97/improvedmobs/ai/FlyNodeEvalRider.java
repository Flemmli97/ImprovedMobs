package io.github.flemmli97.improvedmobs.ai;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;

/**
 * A FlyNodeEvaluator made for the riding ai
 */
public class FlyNodeEvalRider extends FlyNodeEvaluator {

    @Override
    public void prepare(PathNavigationRegion level, Mob mob) {
        super.prepare(level, mob);
        double heightInc = EntityType.PHANTOM.getDimensions().height * 0.35 + mob.getMyRidingOffset();
        this.entityHeight += Math.ceil(heightInc);
    }
}
