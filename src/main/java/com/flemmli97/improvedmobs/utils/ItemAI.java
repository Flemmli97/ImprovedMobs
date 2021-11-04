package com.flemmli97.improvedmobs.utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface ItemAI {
    void attack(MobEntity entity, LivingEntity target, Hand hand);

    int cooldown();

    ItemType type();

    UsableHand prefHand();

    default boolean useHand() {
        return false;
    }

    default int maxUseCount(MobEntity entity, Hand hand) {
        return 20;
    }

    default void onReset(MobEntity entity, Hand hand) {
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
