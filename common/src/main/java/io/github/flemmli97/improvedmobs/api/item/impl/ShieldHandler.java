package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

public class ShieldHandler implements ItemUseHandler {

    @Override
    public void start(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        entity.startUsingItem(hand);
    }

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        entity.stopUsingItem();
    }

    @Override
    public boolean canUse(LivingEntity entity, ItemStack stack) {
        return ItemUseHandler.super.canUse(entity, stack);
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.OFFHAND;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return 60 + entity.getRandom().nextInt(20);
    }

    @Override
    public int attackDelay(LivingEntity entity, ItemStack stack) {
        return 75;
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof ShieldItem;
    }
}
