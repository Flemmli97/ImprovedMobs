package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.KeepDistanceMover;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.function.Function;

public class CrossbowHandler implements ItemUseHandler {

    @Override
    public void start(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        entity.startUsingItem(hand);
    }

    @Override
    public void onPrepare(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        int i = entity.getTicksUsingItem();
        ItemStack itemstack = entity.getUseItem();
        if (i >= CrossbowItem.getChargeDuration(itemstack, entity)) {
            entity.releaseUsingItem();
        }
    }

    @Override
    public int use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        if (stack.getItem() instanceof CrossbowItem crossbow) {
            ChargedProjectiles projectile = stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            float vel = projectile.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
            crossbow.performShooting(entity.level(), entity, hand, stack, vel, 14 - entity.level().getDifficulty().getId() * 4, target);
        }
        return 30;
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int attackDelay(LivingEntity entity, ItemStack stack) {
        return CrossbowItem.getChargeDuration(stack, entity) + 20 + entity.getRandom().nextInt(20);
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof CrossbowItem;
    }

    @Override
    public Function<Mob, MoveHandler> movementType() {
        return mob -> new KeepDistanceMover(mob, 5, 9);
    }
}
