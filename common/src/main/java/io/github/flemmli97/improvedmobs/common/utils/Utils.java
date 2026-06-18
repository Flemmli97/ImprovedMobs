package io.github.flemmli97.improvedmobs.common.utils;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.config.equipment.EquipmentList;
import io.github.flemmli97.improvedmobs.common.config.values.StepExpressionConfig;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Utils {

    public static final Function<Entity, ResourceLocation> ENTITY_ID = e -> BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
    public static final ResourceLocation ATTRIBUTE_ID = ImprovedMobs.modRes("attribute_modifiers");

    public static <T> boolean isInList(T entry, List<? extends String> list, boolean reverse, Function<T, ResourceLocation> mapper) {
        if (reverse)
            return !isInList(entry, list, false, mapper);
        ResourceLocation res = mapper.apply(entry);
        return list.contains(res.getPath()) || list.contains(res.toString());
    }

    public static boolean canBreakBlocks(Mob mob) {
        if (mob.getTarget() == null && !Config.CommonConfig.idleBreak)
            return false;
        return EntityFlags.get(mob).canBreakBlocks == EntityFlags.FlagType.TRUE
                && mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public static boolean canClimb(Mob mob) {
        return EntityFlags.get(mob).ladderClimber;
    }

    public static boolean canBreakState(LivingEntity entity, BlockState state, BlockPos pos) {
        return Config.CommonConfig.breakableBlocks.canBreak(state, pos, entity.level(), entity, CollisionContext.of(entity))
                && (Utils.canHarvest(state, entity.getMainHandItem()) || Utils.canHarvest(state, entity.getOffhandItem()));
    }

    public static boolean canHarvest(BlockState block, ItemStack item) {
        if (Config.CommonConfig.ignoreHarvestLevel)
            return true;
        return item.isCorrectToolForDrops(block) || !block.requiresCorrectToolForDrops();
    }

    public static void equipArmor(Mob living, double difficulty, VariableMap map) {
        if (living.getRandom().nextFloat() < Config.CommonConfig.equipmentChance.get(map)) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HAND)
                    continue;
                boolean shouldAdd = slot == EquipmentSlot.HEAD || living.getRandom().nextFloat() < Config.CommonConfig.additionalEquipmentChance.get(map);
                if (shouldAdd && living.getItemBySlot(slot).isEmpty()) {
                    ItemStack equip = EquipmentList.getEquipment(living, slot, difficulty);
                    if (living.getRandom().nextFloat() < Config.CommonConfig.randomTrimChance.get(map)) {
                        RegistryAccess registryAccess = living.getServer().registryAccess();
                        Optional<Holder.Reference<TrimMaterial>> trim = registryAccess.registry(Registries.TRIM_MATERIAL).flatMap(r -> r.getRandom(living.getRandom()));
                        Optional<Holder.Reference<TrimPattern>> pattern = living.getServer().registryAccess().registry(Registries.TRIM_PATTERN).flatMap(r -> r.getRandom(living.getRandom()));
                        if (trim.isPresent() && pattern.isPresent()) {
                            equip.set(DataComponents.TRIM, new ArmorTrim(trim.get(), pattern.get()));
                        }
                    }
                    if (!equip.isEmpty()) {
                        living.setDropChance(slot, (float) Config.CommonConfig.dropChance.get(map));
                        living.setItemSlot(slot, equip);
                    }
                }
            }
        }
    }

    public static void equipHeld(Mob living, double difficulty, VariableMap map) {
        if (living.getRandom().nextFloat() < Config.CommonConfig.mainHandChance.get(map)) {
            if (living.getMainHandItem().isEmpty()) {
                ItemStack stack = EquipmentList.getEquipment(living, EquipmentSlot.MAINHAND, difficulty);
                living.setDropChance(EquipmentSlot.MAINHAND, (float) Config.CommonConfig.dropChance.get(map));
                living.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }
        }
        // Cause bartering they throw it out immediately
        if (living instanceof AbstractPiglin)
            return;
        if (living.getRandom().nextFloat() < Config.CommonConfig.offHandChance.get(map)) {
            if (living.getOffhandItem().isEmpty()) {
                ItemStack stack = EquipmentList.getEquipment(living, EquipmentSlot.OFFHAND, difficulty);
                living.setDropChance(EquipmentSlot.OFFHAND, (float) Config.CommonConfig.dropChance.get(map));
                living.setItemSlot(EquipmentSlot.OFFHAND, stack);
            }
        }
    }

    public static void enchantGear(Mob living, double difficulty, VariableMap map) {
        StepExpressionConfig.Value val = Config.CommonConfig.enchantCalc.get(difficulty);
        int level = (int) val.expression().get(map);
        if (level == 0)
            return;
        for (EquipmentSlot entityequipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = living.getItemBySlot(entityequipmentslot);
            if (itemstack.isEnchanted())
                continue;
            if (!itemstack.isEmpty() && living.getRandom().nextFloat() < Config.CommonConfig.enchantChance.get(map)) {
                RegistryAccess registryAccess = living.registryAccess();
                EnchantmentHelper.enchantItem(living.getRandom(), itemstack, level,
                        registryAccess.registryOrThrow(Registries.ENCHANTMENT).holders().filter(r ->
                                        Config.CommonConfig.enchantWhitelist == Config.CommonConfig.enchantBlacklist.contains(r.key().location().toString()))
                                .map(r -> r));
            }
        }
    }

    public static float getBlockStrength(Mob entityLiving, BlockState state, Level level, BlockPos pos) {
        float hardness = level.getBlockState(pos).getDestroySpeed(level, pos);
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
        if (f > 1.0f && entity.getAttributes().hasAttribute(Attributes.MINING_EFFICIENCY)) {
            f += (float) entity.getAttributeValue(Attributes.MINING_EFFICIENCY);
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
        if (entity.isEyeInFluid(FluidTags.WATER) && entity.getAttribute(Attributes.SUBMERGED_MINING_SPEED) != null) {
            f *= (float) entity.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }
        if (!entity.onGround())
            f /= 5.0F;
        return f;
    }
}