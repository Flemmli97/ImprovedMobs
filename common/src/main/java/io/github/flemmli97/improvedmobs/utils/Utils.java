package io.github.flemmli97.improvedmobs.utils;

import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.EnchantCalcConf;
import io.github.flemmli97.improvedmobs.config.EquipmentList;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.tenshilib.RegistryHelper;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Function;

public class Utils {

    public static final Function<Entity, ResourceLocation> entityID = e -> RegistryHelper.entities().getIDFrom(e.getType());
    public static final Function<Item, ResourceLocation> itemID = e -> RegistryHelper.items().getIDFrom(e);

    public static <T> boolean isInList(T entry, List<? extends String> list, boolean reverse, Function<T, ResourceLocation> mapper) {
        if (reverse)
            return !isInList(entry, list, false, mapper);
        ResourceLocation res = mapper.apply(entry);
        return list.contains(res.getPath()) || list.contains(res.toString());
    }

    public static boolean canHarvest(BlockState block, ItemStack item) {
        return item.isCorrectToolForDrops(block) || !block.requiresCorrectToolForDrops();
    }

    public static void equipArmor(Mob living) {
        if (Config.CommonConfig.baseEquipChance != 0) {
            float time = DifficultyData.getDifficulty(living.level, living) * Config.CommonConfig.diffEquipAdd * 0.01F;
            if (living.getRandom().nextFloat() < (Config.CommonConfig.baseEquipChance + time)) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.HAND)
                        continue;
                    boolean shouldAdd = slot == EquipmentSlot.HEAD || (Config.CommonConfig.baseEquipChanceAdd != 0 && living.getRandom().nextFloat() < (Config.CommonConfig.baseEquipChanceAdd + time));
                    if (shouldAdd && living.getItemBySlot(slot).isEmpty()) {
                        ItemStack equip = EquipmentList.getEquip(living, slot);
                        if (!equip.isEmpty()) {
                            if (!Config.CommonConfig.shouldDropEquip)
                                living.setDropChance(slot, -100);
                            living.setItemSlot(slot, equip);
                        }
                    }
                }
            }
        }
    }

    public static void equipHeld(Mob living) {
        float add = DifficultyData.getDifficulty(living.level, living) * Config.CommonConfig.diffWeaponChance * 0.01F;
        if (Config.CommonConfig.baseWeaponChance != 0 && living.getRandom().nextFloat() < (Config.CommonConfig.baseWeaponChance + add)) {
            if (living.getMainHandItem().isEmpty()) {
                ItemStack stack = EquipmentList.getEquip(living, EquipmentSlot.MAINHAND);
                if (!Config.CommonConfig.shouldDropEquip)
                    living.setDropChance(EquipmentSlot.MAINHAND, -100);
                living.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }
        }
        add = DifficultyData.getDifficulty(living.level, living) * Config.CommonConfig.diffItemChanceAdd * 0.01F;
        if (Config.CommonConfig.baseItemChance != 0 && living.getRandom().nextFloat() < (Config.CommonConfig.baseItemChance + add)) {
            if (living.getOffhandItem().isEmpty()) {
                ItemStack stack = EquipmentList.getEquip(living, EquipmentSlot.OFFHAND);
                if (!Config.CommonConfig.shouldDropEquip)
                    living.setDropChance(EquipmentSlot.OFFHAND, -100);
                living.setItemSlot(EquipmentSlot.OFFHAND, stack);
            }
        }
    }

    public static void enchantGear(Mob living) {
        float diff = DifficultyData.getDifficulty(living.level, living);
        EnchantCalcConf.Value val = Config.CommonConfig.enchantCalc.get(diff);
        if (val.max == 0)
            return;
        for (EquipmentSlot entityequipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = living.getItemBySlot(entityequipmentslot);
            if (itemstack.isEnchanted())
                continue;
            if (!itemstack.isEmpty() && living.getRandom().nextFloat() < (Config.CommonConfig.baseEnchantChance + (diff * Config.CommonConfig.diffEnchantAdd * 0.01F))) {
                EnchantmentHelper.enchantItem(living.getRandom(), itemstack, Mth.nextInt(living.getRandom(), val.min, val.max), true);
            }
        }
    }

    public static float getBlockStrength(Mob entityLiving, BlockState state, Level world, BlockPos pos) {
        float hardness = world.getBlockState(pos).getDestroySpeed(world, pos);
        if (hardness < 0) {
            return 0.0F;
        }
        ItemStack main = entityLiving.getMainHandItem();
        ItemStack off = entityLiving.getOffhandItem();
        if (canHarvest(state, main)) {
            float speed = getBreakSpeed(entityLiving, main, state);
            if (canHarvest(state, off)) {
                float offSpeed = getBreakSpeed(entityLiving, off, state);
                if (offSpeed > speed)
                    speed = offSpeed;
            }
            return speed / hardness / 30F;
        } else if (canHarvest(state, off)) {
            return getBreakSpeed(entityLiving, off, state) / hardness / 30F;
        } else {
            return getBreakSpeed(entityLiving, main, state) / hardness / 100F;
        }
    }

    public static float getBreakSpeed(Mob entity, ItemStack stack, BlockState state) {
        float f = stack.getDestroySpeed(state);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getBlockEfficiency(entity);
            ItemStack itemstack = entity.getMainHandItem();
            if (i > 0 && !itemstack.isEmpty())
                f += i * i + 1;
        }
        if (MobEffectUtil.hasDigSpeed(entity))
            f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(entity) + 1) * 0.2F;
        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            switch (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> f *= 0.3F;
                case 1 -> f *= 0.09F;
                case 2 -> f *= 0.0027F;
                default -> f *= 8.1E-4F;
            }
        }
        if (entity.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity))
            f /= 5.0F;
        if (!entity.isOnGround())
            f /= 5.0F;
        return f;
    }

    public static void modifyAttr(Mob living, Attribute att, double value, double max, boolean multiply) {
        AttributeInstance inst = living.getAttribute(att);
        if (inst == null)
            return;
        double oldValue = inst.getBaseValue();
        value *= DifficultyData.getDifficulty(living.level, living);
        if (multiply) {
            value = Math.min(value, max - 1);
            value = oldValue * (1 + value);
            if (att == Attributes.MAX_HEALTH)
                value = Config.CommonConfig.roundHP > 0 ? MathUtils.roundTo(value, Config.CommonConfig.roundHP) : value;
        } else {
            value = Math.min(value, max);
            value = oldValue + value;
        }
        inst.setBaseValue(value);
        //ForgeMod.SWIM_SPEED
    }
}