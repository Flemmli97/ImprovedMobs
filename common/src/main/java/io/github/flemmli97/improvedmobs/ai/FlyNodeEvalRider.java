package io.github.flemmli97.improvedmobs.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.phys.Vec3;

/**
 * A FlyNodeEvaluator made for the riding ai
 */
public class FlyNodeEvalRider extends FlyNodeEvaluator {

    @Override
    public void prepare(PathNavigationRegion level, Mob mob) {
        super.prepare(level, mob);
        this.entityHeight += Mth.ceil(this.getPassengerHeightOffset());
    }

    private double getPassengerHeightOffset() {
        Vec3 offset = EntityType.PHANTOM.getDimensions().attachments().getClamped(EntityAttachment.PASSENGER, 0, 0);
        return offset.y();
    }
}
