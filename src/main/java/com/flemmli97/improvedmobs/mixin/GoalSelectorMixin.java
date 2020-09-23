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

@Mixin(GoalSelector.class)
public class GoalSelectorMixin implements IGoalModifier {

    @Shadow
    Set<PrioritizedGoal> goals;

    @Override
    public boolean goalRemovePredicate(Predicate<Goal> goal) {
        this.goals.stream().filter((prio) -> goal.test(prio.getGoal())).filter(PrioritizedGoal::isRunning).forEach(PrioritizedGoal::resetTask);
        return this.goals.removeIf((prio) -> goal.test(prio.getGoal()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Goal> void modifyGoal(Class<T> clss, Consumer<T> cons) {
        this.goals.stream().filter(prio -> clss.isInstance(prio.getGoal())).forEach(prio -> cons.accept((T) prio.getGoal()));
    }
}
