package io.github.flemmli97.improvedmobs.ai.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public interface ItemAI {
    void attack(Mob entity, LivingEntity target, InteractionHand hand);

    int cooldown();

    ItemType type();

    UsableHand prefHand();

    default boolean useHand() {
        return false;
    }

    default int maxUseCount(Mob entity, InteractionHand hand) {
        return 20;
    }

    default void onReset(Mob entity, InteractionHand hand) {
    }

    default boolean applies(ItemStack stack) {
        return true;
    }

    default boolean isIncompatibleWith(LivingEntity entity, ItemStack stack) {
        return false;
    }

    enum ItemType {
        NONSTRAFINGITEM,
        STRAFINGITEM,
        STANDING
    }

    enum UsableHand {
        MAIN,
        OFF,
        BOTH
    }
}
