package io.github.flemmli97.improvedmobs.api.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ItemUseHandler {

    /**
     * Called when the item starts getting used. Setup whatever is needed here. E.g. set the item in use for the entity
     */
    default void start(LivingEntity entity, LivingEntity target, InteractionHand hand) {
    }

    /**
     * Called during the time between #start and #use
     */
    default void onPrepare(LivingEntity entity, LivingEntity target, InteractionHand hand) {
    }

    /**
     * Delay between #startUse and #use
     */
    default int attackDelay(LivingEntity entity, ItemStack stack) {
        return 0;
    }

    /**
     * Called when the item is actually used
     * The process is as follows:
     * 1. #startUse is called
     * 2. #useDelay ticks is waited
     * 3. #use is called
     */
    void use(LivingEntity entity, LivingEntity target, InteractionHand hand);

    /**
     * Whether the entity can use this item in its current state.
     * E.g. it shouldn't use it if some condition is met
     */
    default boolean canUse(LivingEntity entity, ItemStack stack) {
        return true;
    }

    /**
     * The preferred hand for this item.
     * Item needs to be in this hand to be able to be used
     */
    PreferredHand preferredHand();

    int cooldown(LivingEntity entity);

    /**
     * Whether this handler matches the given item. Used during setup for calculating quick lookups
     */
    boolean matches(Item item);

    /**
     * How the entity moves with this item. Return null to not have any special movements
     */
    @Nullable
    default Function<Mob, MoveHandler> movementType() {
        return null;
    }

    /**
     * This is only used during equipment weight initialization to determine what slot this handler should count as
     */
    default EquipmentSlot defaultedSlot() {
        return this.preferredHand().slot;
    }

    enum PreferredHand {

        MAINHAND(EquipmentSlot.MAINHAND),
        OFFHAND(EquipmentSlot.OFFHAND),
        ANY(EquipmentSlot.MAINHAND);

        public final EquipmentSlot slot;

        PreferredHand(EquipmentSlot slot) {
            this.slot = slot;
        }
    }
}
