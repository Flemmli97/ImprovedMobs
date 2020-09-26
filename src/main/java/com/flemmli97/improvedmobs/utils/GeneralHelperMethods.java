package com.flemmli97.improvedmobs.utils;

import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.config.EquipmentList;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class GeneralHelperMethods {

    public static boolean isMobInList(MobEntity living, List<? extends String> list, boolean reverse) {
        if (reverse)
            return !isMobInList(living, list, false);
        for (String s : list) {
            ResourceLocation res = living.getType().getRegistryName();
            if (s.startsWith("@")) {
                return res != null && res.getNamespace().equals(s.substring(1));
            }
            if (res != null && res.toString().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canHarvest(BlockState block, ItemStack item) {
        return item.canHarvestBlock(block) || !block.isToolRequired();
    }

    public static void equipArmor(MobEntity living) {
        if (Config.CommonConfig.baseEquipChance != 0) {
            float time = DifficultyData.getDifficulty(living.world, living) * Config.CommonConfig.diffEquipAdd * 0.01F;
            if (living.getRNG().nextFloat() < (Config.CommonConfig.baseEquipChance + time)) {
                for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                    if (slot.getSlotType() == EquipmentSlotType.Group.HAND)
                        continue;
                    boolean shouldAdd = slot == EquipmentSlotType.HEAD || (Config.CommonConfig.baseEquipChanceAdd != 0 && living.getRNG().nextFloat() < (Config.CommonConfig.baseEquipChanceAdd + time));
                    if (shouldAdd && living.getItemStackFromSlot(slot).isEmpty()) {
                        ItemStack equip = EquipmentList.getEquip(living, slot);
                        if (!equip.isEmpty()) {
                            if (!Config.CommonConfig.shouldDropEquip)
                                equip.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                            living.setItemStackToSlot(slot, equip);
                        }
                    }
                }
            }
        }
    }

    public static void equipHeld(MobEntity living) {
        float add = DifficultyData.getDifficulty(living.world, living) * Config.CommonConfig.diffWeaponChance * 0.01F;
        if (Config.CommonConfig.baseWeaponChance != 0 && living.getRNG().nextFloat() < (Config.CommonConfig.baseWeaponChance + add)) {
            if (living.getHeldItemMainhand().isEmpty()) {
                ItemStack stack = EquipmentList.getEquip(living, EquipmentSlotType.MAINHAND);
                if (!Config.CommonConfig.shouldDropEquip)
                    stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                living.setItemStackToSlot(EquipmentSlotType.MAINHAND, stack);
            }
        }
        add = DifficultyData.getDifficulty(living.world, living) * Config.CommonConfig.diffItemChanceAdd * 0.01F;
        if (Config.CommonConfig.baseItemChance != 0 && living.getRNG().nextFloat() < (Config.CommonConfig.baseItemChance + add)) {
            if (living.getHeldItemOffhand().isEmpty()) {
                ItemStack stack = EquipmentList.getEquip(living, EquipmentSlotType.OFFHAND);
                if (!Config.CommonConfig.shouldDropEquip)
                    stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                living.setItemStackToSlot(EquipmentSlotType.OFFHAND, stack);
            }
        }
    }

    public static void enchantGear(MobEntity living) {
        for (EquipmentSlotType entityequipmentslot : EquipmentSlotType.values()) {
            ItemStack itemstack = living.getItemStackFromSlot(entityequipmentslot);
            if (!itemstack.isEmpty() && living.getRNG().nextFloat() < (Config.CommonConfig.baseEnchantChance + (DifficultyData.getDifficulty(living.world, living) * Config.CommonConfig.diffEnchantAdd * 0.01F))) {
                EnchantmentHelper.addRandomEnchantment(living.getRNG(), itemstack, 5 + living.getRNG().nextInt(25), true);
            }
        }
    }

    public static float getBreakSpeed(MobEntity entity, ItemStack stack, BlockState state) {
        float f = stack.getDestroySpeed(state);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiencyModifier(entity);
            ItemStack itemstack = entity.getHeldItemMainhand();
            if (i > 0 && !itemstack.isEmpty())
                f += i * i + 1;
        }
        if (EffectUtils.hasMiningSpeedup(entity))
            f *= 1.0F + (EffectUtils.getMiningSpeedup(entity) + 1) * 0.2F;
        if (entity.isPotionActive(Effects.MINING_FATIGUE)) {
            switch (entity.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    f *= 0.3F;
                    break;
                case 1:
                    f *= 0.09F;
                    break;
                case 2:
                    f *= 0.0027F;
                    break;
                case 3:
                default:
                    f *= 8.1E-4F;
            }
        }
        if (entity.areEyesInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity))
            f /= 5.0F;
        if (!entity.isOnGround())
            f /= 5.0F;
        return f;
    }

    public static float getBlockStrength(MobEntity entityLiving, BlockState state, World world, BlockPos pos) {
        float hardness = world.getBlockState(pos).getBlockHardness(world, pos);
        if (hardness == -1) {
            return 0.0F;
        }
        ItemStack main = entityLiving.getHeldItemMainhand();
        ItemStack off = entityLiving.getHeldItemOffhand();
        if (canHarvest(state, main)) {
            return getBreakSpeed(entityLiving, main, state) / hardness / 30F;
        } else if (canHarvest(state, off)) {
            return getBreakSpeed(entityLiving, off, state) / hardness / 30F;
        } else {
            return getBreakSpeed(entityLiving, main, state) / hardness / 100F;
        }
    }

    public static void modifyAttr(MobEntity living, Attribute att, double value, double max, boolean multiply) {
        ModifiableAttributeInstance inst = living.getAttribute(att);
        if (inst == null)
            return;
        double oldValue = inst.getBaseValue();
        value *= DifficultyData.getDifficulty(living.world, living);
        if (multiply) {
            value = Math.min(value, max - 1);
            value = oldValue * (1 + value);
            if (att == Attributes.GENERIC_MAX_HEALTH)
                value = Config.CommonConfig.roundHP > 0 ? MathUtils.roundTo(value, Config.CommonConfig.roundHP) : value;
        } else {
            value = Math.min(value, max);
            value = oldValue + value;
        }
        inst.setBaseValue(value);
    }
}