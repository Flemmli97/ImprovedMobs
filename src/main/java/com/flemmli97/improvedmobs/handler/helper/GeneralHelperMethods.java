package com.flemmli97.improvedmobs.handler.helper;

import com.flemmli97.improvedmobs.entity.EntityGuardianBoat;
import com.flemmli97.improvedmobs.handler.DifficultyData;
import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.config.EquipmentList;
import com.flemmli97.tenshilib.common.javahelper.MathUtils;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GeneralHelperMethods {

	public static boolean isMobInList(EntityLiving living, String[] list, boolean reverse) {
		if(living instanceof EntityGuardianBoat)
			return !reverse;
		if(reverse)
			return !isMobInList(living, list, false);
		for(int i = 0; i < list.length; i++){
			ResourceLocation res = EntityList.getKey(living);
			if(list[i].startsWith("@")){
				return res != null && res.getResourceDomain().equals(list[i].substring(1));
			}
			if(res != null && res.toString().equals(list[i])){
				return true;
			}
		}
		return false;
	}

	public static boolean canHarvest(IBlockState block, ItemStack item) {
		return (item != null && (item.getItem().canHarvestBlock(block, item))) || block.getMaterial().isToolNotRequired();
	}

	public static void equipArmor(EntityLiving living) {
		if(ConfigHandler.baseEquipChance != 0){
			float time = DifficultyData.getDifficulty(living.world, living) * ConfigHandler.diffEquipAdd * 0.01F;
			if(living.getRNG().nextFloat() < (ConfigHandler.baseEquipChance + time)){
				for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()){
					if(slot.getSlotType() == EntityEquipmentSlot.Type.HAND)
						continue;
					boolean shouldAdd = slot == EntityEquipmentSlot.HEAD || (ConfigHandler.baseEquipChanceAdd != 0 && living.getRNG().nextFloat() < (ConfigHandler.baseEquipChanceAdd + time));
					if(shouldAdd && living.getItemStackFromSlot(slot).isEmpty()){
						ItemStack equip = EquipmentList.getEquip(living, slot);
						if(!equip.isEmpty()){
							if(!ConfigHandler.shouldDropEquip)
								equip.addEnchantment(Enchantments.VANISHING_CURSE, 1);
							living.setItemStackToSlot(slot, equip);
						}
					}
				}
			}
		}
	}

	public static void equipHeld(EntityLiving living) {
		float add = DifficultyData.getDifficulty(living.world, living) * ConfigHandler.diffWeaponChance * 0.01F;
		if(ConfigHandler.baseWeaponChance != 0 && living.getRNG().nextFloat() < (ConfigHandler.baseWeaponChance + add)){
			if(living.getHeldItemMainhand().isEmpty()){
				ItemStack stack = EquipmentList.getEquip(living, EntityEquipmentSlot.MAINHAND);
				if(!ConfigHandler.shouldDropEquip)
					stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
				living.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
			}
		}
		add = DifficultyData.getDifficulty(living.world, living) * ConfigHandler.diffItemChanceAdd * 0.01F;
		if(ConfigHandler.baseItemChance != 0 && living.getRNG().nextFloat() < (ConfigHandler.baseItemChance + add)){
			if(living.getHeldItemOffhand().isEmpty()){
				ItemStack stack = EquipmentList.getEquip(living, EntityEquipmentSlot.OFFHAND);
				if(!ConfigHandler.shouldDropEquip)
					stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
				living.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
			}
		}
	}

	public static void enchantGear(EntityLiving living) {
		for(EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()){
			ItemStack itemstack = living.getItemStackFromSlot(entityequipmentslot);
			if(!itemstack.isEmpty() && living.getRNG().nextFloat() < (ConfigHandler.baseEnchantChance + (DifficultyData.getDifficulty(living.world, living) * ConfigHandler.diffEnchantAdd * 0.01F))){
				EnchantmentHelper.addRandomEnchantment(living.getRNG(), itemstack, 5 + living.getRNG().nextInt(25), true);
			}
		}
	}

	public static float getBreakSpeed(EntityLiving entityLiving, ItemStack stack, IBlockState state) {
		float f = (stack == null ? 1.0F : stack.getItem().getDestroySpeed(stack, state));

		if(f > 1.0F){
			int i = EnchantmentHelper.getEfficiencyModifier(entityLiving);

			if(i > 0 && stack != null){
				float f1 = (float) (i * i + 1);
				boolean canHarvest = stack.canHarvestBlock(state);

				if(!canHarvest && f <= 1.0F){
					f += f1 * 0.08F;
				}else{
					f += f1;
				}
			}
		}

		if(entityLiving.isPotionActive(Potion.getPotionFromResourceLocation("minecraft:haste"))){
			f *= 1.0F + (float) (entityLiving.getActivePotionEffect(Potion.getPotionFromResourceLocation("minecraft:haste")).getAmplifier() + 1) * 0.2F;
		}

		if(entityLiving.isPotionActive(Potion.getPotionFromResourceLocation("minecraft:mining_fatigue"))){
			f *= 1.0F - (float) (entityLiving.getActivePotionEffect(Potion.getPotionFromResourceLocation("minecraft:mining_fatigue")).getAmplifier() + 1) * 0.2F;
		}

		if(entityLiving.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(entityLiving)){
			f /= 5.0F;
		}

		if(!entityLiving.onGround){
			f /= 5.0F;
		}

		return (f < 0 ? 0 : f);
	}

	public static float getBlockStrength(EntityLiving entityLiving, IBlockState state, World world, BlockPos pos) {
		float hardness = world.getBlockState(pos).getBlockHardness(world, pos);
		if(hardness < 0.0F){
			return 0.0F;
		}

		ItemStack main = entityLiving.getHeldItemMainhand();
		ItemStack off = entityLiving.getHeldItemOffhand();
		if(canHarvest(state, main)){
			return getBreakSpeed(entityLiving, main, state) / hardness / 30F;
		}else if(canHarvest(state, off)){
			return getBreakSpeed(entityLiving, off, state) / hardness / 30F;
		}else{
			return getBreakSpeed(entityLiving, main, state) / hardness / 100F;
		}
	}

	public static void modifyAttr(EntityLiving living, IAttribute att, double value, double max, boolean multiply) {
		IAttributeInstance inst = living.getAttributeMap().getAttributeInstance(att);
		if(inst == null)
			return;
		double oldValue = inst.getBaseValue();
		value *= DifficultyData.getDifficulty(living.world, living);
		if(multiply){
			value = Math.min(value, max - 1);
			value = oldValue * (1 + value);
			if(att == SharedMonsterAttributes.MAX_HEALTH)
				value = ConfigHandler.roundHP > 0 ? MathUtils.roundTo(value, ConfigHandler.roundHP) : value;
			inst.setBaseValue(value);
		}else{
			value = Math.min(value, max);
			value = oldValue + value;
			inst.setBaseValue(value);
		}
	}
}
