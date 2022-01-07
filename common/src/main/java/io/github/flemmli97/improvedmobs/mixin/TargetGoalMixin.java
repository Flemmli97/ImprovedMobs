package io.github.flemmli97.improvedmobs.mixin;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TargetGoal.class)
public interface TargetGoalMixin {

    @Mutable
    @Accessor("mustSee")
    void setShouldCheckSight(boolean check);
}
