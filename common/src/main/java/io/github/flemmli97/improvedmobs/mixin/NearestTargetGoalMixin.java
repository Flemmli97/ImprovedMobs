package io.github.flemmli97.improvedmobs.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NearestAttackableTargetGoal.class)
public interface NearestTargetGoalMixin<T extends LivingEntity> {

    @Accessor("targetConditions")
    TargetingConditions getTargetEntitySelector();

    @Accessor("targetType")
    Class<T> targetTypeClss();

}
