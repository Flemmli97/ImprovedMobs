package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.StrafingMover;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class BowHandler implements ItemUseHandler {

    @Override
    public void start(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        entity.startUsingItem(hand);
    }

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        float distanceFactor = BowItem.getPowerForTime(entity.getTicksUsingItem());
        entity.stopUsingItem();
        ItemStack weapon = entity.getItemInHand(hand);
        ItemStack ammo = entity.getProjectile(weapon);
        AbstractArrow arrow = CrossPlatformStuff.INSTANCE.customBowArrow(weapon, ammo, ProjectileUtil.getMobArrow(entity, ammo, distanceFactor, weapon));
        double d0 = target.getX() - entity.getX();
        double d1 = target.getY(0.33) - arrow.getY();
        double d2 = target.getZ() - entity.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * 0.2, d2, 1.6f, 14 - entity.level().getDifficulty().getId() * 4);
        entity.playSound(SoundEvents.SKELETON_SHOOT, 1, 1 / (entity.getRandom().nextFloat() * 0.4f + 0.8f));
        entity.level().addFreshEntity(arrow);
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return 30;
    }

    @Override
    public int attackDelay(LivingEntity entity, ItemStack stack) {
        return 20;
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof BowItem;
    }

    @Override
    public Function<Mob, MoveHandler> movementType() {
        return StrafingMover::new;
    }

    @Override
    public EquipmentSlot defaultedSlot() {
        return EquipmentSlot.OFFHAND;
    }
}
