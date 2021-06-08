package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ai.IGoalModifier;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(GoalSelector.class)
public abstract class GoalSelectorMixin implements IGoalModifier {

    @Shadow
    Set<PrioritizedGoal> goals;

    @Override
    public void goalRemovePredicate(Predicate<Goal> goal) {
        Set<PrioritizedGoal> toRemove = this.goals.stream().filter((prio) -> goal.test(prio.getGoal())).collect(Collectors.toSet());
        toRemove.forEach(prio -> this.removeGoal(prio.getGoal()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Goal> void modifyGoal(Class<T> clss, Consumer<T> cons) {
        this.goals.stream().filter(prio -> clss.isInstance(prio.getGoal())).forEach(prio -> cons.accept((T) prio.getGoal()));
    }

    @Shadow
    public abstract void removeGoal(Goal goal);
}
