package io.github.flemmli97.improvedmobs.api.item.impl.integration;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.StrafingMover;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class SupplementariesBomb implements ItemUseHandler {

    @Override
    public boolean canUse(LivingEntity entity, ItemStack stack, boolean attempt) {
        if (attempt && entity instanceof Mob mob && mob.getTarget() != null) {
            return entity.position().distanceToSqr(mob.getTarget().position()) > 25;
        }
        return true;
    }

    @Override
    public void use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        if (!entity.level().isClientSide) {
            double dis = entity.position().distanceTo(target.position()) - 5;
            if (dis <= 0)
                return;
            ItemStack stack = entity.getItemInHand(hand);
            BombEntity.BombType type = stack.getItem() instanceof BombItem bombItem ? bombItem.getType() : BombEntity.BombType.NORMAL;
            Projectile bomb = new BombEntity(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), type);
            bomb.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), -10, 0.8f + (float) (dis * 0.02), 1.0F);
            EntityFlags.get(bomb).isThrownEntity = true;
            entity.level().addFreshEntity(bomb);
        }
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public int cooldown(LivingEntity entity) {
        return 55 + entity.getRandom().nextInt(25);
    }

    @Override
    public int attackDelay(LivingEntity entity, ItemStack stack) {
        return 0;
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof BombItem;
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
