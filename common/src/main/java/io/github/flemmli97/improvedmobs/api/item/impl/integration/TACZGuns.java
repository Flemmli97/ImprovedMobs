package io.github.flemmli97.improvedmobs.api.item.impl.integration;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.sound.SoundManager;
import com.tacz.guns.util.AttachmentDataUtils;
import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.move.MoveTillMover;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.Function;

public class TACZGuns implements ItemUseHandler {

    public static final Map<String, GunTabType> TYPE_LOOKUP;

    static {
        Gson gson = new Gson();
        ImmutableMap.Builder<String, GunTabType> builder = ImmutableMap.builder();
        for (GunTabType type : GunTabType.values()) {
            builder.put(gson.toJson(type).replace("\"", ""), type);
        }
        TYPE_LOOKUP = builder.build();
    }

    @Override
    public int use(LivingEntity entity, LivingEntity target, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        AbstractGunItem gun = (AbstractGunItem) stack.getItem();
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);
        if (index == null)
            return 100;
        if (gun.getCurrentAmmoCount(stack) <= 0) {
            // Simulate reloading. The mob won't have ammo so we set it directly
            int maxAmmoCount = Math.min(15, AttachmentDataUtils.getAmmoCountWithAttachment(stack, index.getGunData()));
            gun.setCurrentAmmoCount(entity.getItemInHand(hand), maxAmmoCount);
            float durationSec = Math.max(index.getGunData().getReloadData().getFeed().getEmptyTime(), index.getGunData().getReloadData().getCooldown().getEmptyTime());
            SoundManager.sendSoundToNearby(entity, 8, gun.getGunId(stack), gun.getGunDisplayId(stack),
                    SoundManager.RELOAD_EMPTY_SOUND, 0.8f, 0.9f + entity.getRandom().nextFloat() * 0.2f);
            return (int) Math.ceil(durationSec * 20.) + 20;
        }
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        if (entity.hasLineOfSight(target)) {
            IGunOperator operator = IGunOperator.fromLivingEntity(entity);
            ShootResult result = operator.shoot(() -> entity.getViewXRot(1), () -> entity.getViewYRot(1));
            switch (result) {
                case NOT_DRAW -> {
                    operator.draw(() -> entity.getItemInHand(hand));
                    return (int) Math.ceil(index.getGunData().getDrawTime() * 20.);
                }
                case NEED_BOLT -> {
                    operator.bolt();
                    return (int) Math.ceil(index.getGunData().getBoltFeedTime() * 20.);
                }
                case SUCCESS -> {
                    FireMode fireMode = gun.getFireMode(stack);
                    if (fireMode == FireMode.BURST) {
                        return (int) Math.ceil(index.getGunData().getBurstShootInterval() * 20.) + this.additionalCooldownFor(index.getType());
                    }
                    return (int) Math.ceil(index.getGunData().getShootInterval(entity, fireMode, stack) * 20. / 1000.) + this.additionalCooldownFor(index.getType());
                }
            }
        }
        return 100;
    }

    private int additionalCooldownFor(String gunType) {
        GunTabType type = TYPE_LOOKUP.get(gunType);
        if (type == null)
            return 5;
        return switch (type) {
            case MG -> 0;
            case RIFLE, SMG -> 1;
            case PISTOL -> 2;
            case SHOTGUN -> 6;
            case RPG -> 8;
            case SNIPER -> 11;
        };
    }

    @Override
    public PreferredHand preferredHand() {
        return PreferredHand.MAINHAND;
    }

    @Override
    public boolean matches(Item item) {
        return item instanceof AbstractGunItem;
    }

    @Override
    public Function<Mob, MoveHandler> movementType() {
        return m -> new MoveTillMover(m, 12);
    }

    @Override
    public EquipmentSlot defaultedSlot() {
        return null;
    }
}
