package io.github.flemmli97.improvedmobs.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface MoveHandler {

    static double defaultRangeOf(Mob mob, double dist) {
        double follow = dist;
        if (mob.getAttribute(Attributes.FOLLOW_RANGE) != null)
            follow = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        double maxDistance = Math.min(follow - 3, dist);
        return maxDistance * maxDistance;
    }

    void move(LivingEntity target, boolean canSee);
}
