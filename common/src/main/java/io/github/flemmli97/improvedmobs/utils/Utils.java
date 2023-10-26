package io.github.flemmli97.improvedmobs.utils;

import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.EnchantCalcConf;
import io.github.flemmli97.improvedmobs.config.EquipmentList;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

public class Utils {

    public static final Function<Entity, ResourceLocation> entityID = e -> PlatformUtils.INSTANCE.entities().getIDFrom(e.getType());
    public static final Function<Item, ResourceLocation> itemID = e -> PlatformUtils.INSTANCE.items().getIDFrom(e);
    public static final UUID attMod = UUID.fromString("7c7e5c2d-1eb0-434a-858f-3ab81f52832c");

    public static <T> boolean isInList(T entry, List<? extends String> list, boolean reverse, Function<T, ResourceLocation> mapper) {
        if (reverse)
            return !isInList(entry, list, false, mapper);
        ResourceLocation res = mapper.apply(entry);
        return list.contains(res.getPath()) || list.contains(res.toString());
    }

    public static boolean canHarvest(BlockState block, ItemStack item) {
        return item.isCorrectToolForDrops(block) || !block.requiresCorrectToolForDrops();
    }

    public static void equipArmor(Mob living, float difficulty) {
        if (Config.CommonConfig.baseEquipChance != 0) {
            float time = difficulty * Config.CommonConfig.diffEquipAdd * 0.01F;
            if (living.getRandom().nextFloat() < (Config.CommonConfig.baseEquipChance + time)) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.HAND)
                        continue;
                    boolean shouldAdd = slot == EquipmentSlot.HEAD || (Config.CommonConfig.baseEquipChanceAdd != 0 && living.getRandom().nextFloat() < (Config.CommonConfig.baseEquipChanceAdd + time));
                    if (shouldAdd && living.getItemBySlot(slot).isEmpty()) {
                        ItemStack equip = EquipmentList.getEquip(living, slot, difficulty);
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

    public static void equipHeld(Mob living, float difficulty) {
        float add = difficulty * Config.CommonConfig.diffWeaponChance * 0.01F;
        if (Config.CommonConfig.baseWeaponChance != 0 && living.getRandom().nextFloat() < (Config.CommonConfig.baseWeaponChance + add)) {
            if (living.getMainHandItem().isEmpty()) {
                ItemStack stack = EquipmentList.getEquip(living, EquipmentSlot.MAINHAND, difficulty);
                if (!Config.CommonConfig.shouldDropEquip)
                    living.setDropChance(EquipmentSlot.MAINHAND, -100);
                living.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }
        }
        // Cause bartering they throw it out immediately
        if (living instanceof AbstractPiglin)
            return;
        add = difficulty * Config.CommonConfig.diffItemChanceAdd * 0.01F;
        if (Config.CommonConfig.baseItemChance != 0 && living.getRandom().nextFloat() < (Config.CommonConfig.baseItemChance + add)) {
            if (living.getOffhandItem().isEmpty()) {
                ItemStack stack = EquipmentList.getEquip(living, EquipmentSlot.OFFHAND, difficulty);
                if (!Config.CommonConfig.shouldDropEquip)
                    living.setDropChance(EquipmentSlot.OFFHAND, -100);
                living.setItemSlot(EquipmentSlot.OFFHAND, stack);
            }
        }
    }

    public static void enchantGear(Mob living, float difficulty) {
        EnchantCalcConf.Value val = Config.CommonConfig.enchantCalc.get(difficulty);
        if (val.max == 0)
            return;
        for (EquipmentSlot entityequipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = living.getItemBySlot(entityequipmentslot);
            if (itemstack.isEnchanted())
                continue;
            if (!itemstack.isEmpty() && living.getRandom().nextFloat() < (Config.CommonConfig.baseEnchantChance + (difficulty * Config.CommonConfig.diffEnchantAdd * 0.01F))) {
                List<EnchantmentInstance> enchants = EnchantmentHelper.selectEnchantment(living.getRandom(), itemstack, Mth.nextInt(living.getRandom(), val.min, val.max), true);
                enchants.forEach(e -> {
                    ResourceLocation res = BuiltInRegistries.ENCHANTMENT.getKey(e.enchantment);
                    if (res != null) {
                        if ((Config.CommonConfig.enchantWhitelist && Config.CommonConfig.enchantBlacklist.contains(res.toString()))
                                || Config.CommonConfig.enchantBlacklist.contains(res.toString())) {
                            itemstack.enchant(e.enchantment, e.level);
                        }
                    }
                });
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

    public static void modifyAttr(Mob living, Attribute att, double value, double max, float difficulty, boolean multiply) {
        AttributeInstance inst = living.getAttribute(att);
        if (inst == null || inst.getModifier(attMod) != null)
            return;
        double oldValue = inst.getBaseValue();
        value *= difficulty;
        if (multiply) {
            value = max <= 0 ? value : Math.min(value, max - 1);
            value = oldValue * value;
            if (att == Attributes.MAX_HEALTH)
                value = Config.CommonConfig.roundHP > 0 ? MathUtils.roundTo(value, Config.CommonConfig.roundHP) : value;
        } else {
            value = max <= 0 ? value : Math.min(value, max);
        }
        inst.addPermanentModifier(new AttributeModifier(attMod, "im_modifier", value, AttributeModifier.Operation.ADDITION));
    }

    public static void modifyScale(Mob living, float min, float max) {
        var random = new Random();
        //float minRange= 0.6F,maxRange= 2.0F;
        ScaleTypes.BASE.getScaleData(living).setScaleTickDelay(20);
        ScaleTypes.BASE.getScaleData(living).setTargetScale(random.nextFloat(min, max));
    }
}