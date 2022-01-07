package io.github.flemmli97.improvedmobs.mixinhelper;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IGoalModifier {

    void goalRemovePredicate(Predicate<Goal> goal);

    <T extends Goal> void modifyGoal(Class<T> clss, Consumer<T> cons);
}
