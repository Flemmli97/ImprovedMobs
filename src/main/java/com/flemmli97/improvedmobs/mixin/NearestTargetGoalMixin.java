package com.flemmli97.improvedmobs.mixin;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NearestAttackableTargetGoal.class)
public interface NearestTargetGoalMixin<T extends LivingEntity> {

    @Accessor
    EntityPredicate getTargetEntitySelector();

    @Accessor("targetClass")
    Class<T> targetTypeClss();
}
