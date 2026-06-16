package io.github.flemmli97.improvedmobs.api.item.impl;

import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.KeepDistanceMover;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class WindChargeHandler implements ItemUseHandler {

    @Override
    public int use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        Vec3 dir = target.position().subtract(entity.position()).normalize();
        dir = new Vec3(dir.x() + entity.getRandom().triangle(0, 0.02), dir.y() + entity.getRandom().triangle(0, 0.02), dir.z() + entity.getRandom().triangle(0, 0.02)).scale(0.55);
        WindCharge windCharge = new WindCharge(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), dir);
        windCharge.setDeltaMovement(dir);
        EntityFlags.get(windCharge).isThrownEntity = true;
        entity.playSound(SoundEvents.WIND_CHARGE_THROW, 1, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
        entity.level().addFreshEntity(windCharge);
        return 65 + entity.getRandom().nextInt(30);
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.ANY;
    }

    @Override
    public boolean matches(Item item) {
        return item == Items.WIND_CHARGE;
    }

    @Override
    public Function<Mob, MoveHandler> movementType() {
        return KeepDistanceMover::new;
    }

    @Override
    public EquipmentSlot defaultedSlot() {
        return EquipmentSlot.OFFHAND;
    }
}
