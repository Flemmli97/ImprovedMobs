package com.flemmli97.improvedmobs.mixin;

import net.minecraft.entity.ai.goal.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TargetGoal.class)
public interface TargetGoalMixin {

    @Accessor
    void setShouldCheckSight(boolean check);
}
