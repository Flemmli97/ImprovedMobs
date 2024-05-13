package io.github.flemmli97.improvedmobs.mixinhelper;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Consumer;

public interface IGoalModifier {

    <T extends Goal> void modifyGoal(Class<T> clss, Consumer<T> cons);
}
