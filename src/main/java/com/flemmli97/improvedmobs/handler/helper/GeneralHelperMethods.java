package com.flemmli97.improvedmobs.handler.helper;

import java.util.List;

import com.flemmli97.improvedmobs.entity.EntityGuardianBoat;
import com.flemmli97.improvedmobs.handler.ConfigHandler;
import com.flemmli97.improvedmobs.handler.DifficultyData;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GeneralHelperMethods {
	
	public static boolean isMobInList(EntityLiving living, String[] list)
	{
		if(living instanceof EntityGuardianBoat)
			return true;
		for(int i = 0;i< list.length;i++)
		{
			String classPath=null;
			if(list[i].endsWith("*"))
			{
				classPath = list[i].substring(0, list[i].length()-1) + living.getClass().getSimpleName();
			}
			if((classPath!=null && living.getClass().getName().equals(classPath)) || 
					living.getClass().getName().equals("net.minecraft.entity.monster."+list[i]) || 
					living.getClass().getName().equals("net.minecraft.entity.passive."+list[i]) ||
					(EntityList.getEntityString(living)!=null && EntityList.getEntityString(living).equals(list[i])) ||
					living.getClass().getName().equals(list[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isBlockBreakable(Block block, List<String> list)
	{
		if(ConfigHandler.blockAsBlacklist)
		{
			if(!list.contains(block.getRegistryName().toString()))
				return true;
			for(ConfigHandler.BlockClassPredicate pred : ConfigHandler.breakListClass)
			{
				if(!pred.matches(block))
					return true;
			}
		}
		else
		{
			for(ConfigHandler.BlockClassPredicate pred : ConfigHandler.breakListClass)
			{
				if(pred.matches(block))
					return true;
			}
			if(ConfigHandler.breakListNames.contains(block.getRegistryName().toString()))
				return true;
		}
		return false;
	}
	
    
    public static boolean canHarvest(IBlockState block, ItemStack item)
    {
		return item != null && (item.getItem().canHarvestBlock(block, item)|| block.getMaterial().isToolNotRequired());
    }
	
	public static float calculateArmorRarityChance(ItemStack stack)
    {
    	ItemArmor armor = (ItemArmor) stack.getItem();
    	float fullProt = armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.HEAD)+armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.CHEST)
    			+armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.LEGS)+armor.getArmorMaterial().getDamageReductionAmount(EntityEquipmentSlot.FEET);

    	float averageDurability = (armor.getArmorMaterial().getDurability(EntityEquipmentSlot.HEAD)+armor.getArmorMaterial().getDurability(EntityEquipmentSlot.CHEST)
    			+armor.getArmorMaterial().getDurability(EntityEquipmentSlot.LEGS)+armor.getArmorMaterial().getDurability(EntityEquipmentSlot.FEET))/4.0F;
    	if(averageDurability<0)
    		averageDurability=0;
    	float ench = armor.getItemEnchantability();
    	float rep = armor.isRepairable() ? 1.1F:0.9F;
    	float vanillaMulti = (armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.LEATHER)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.GOLD)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.CHAIN)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.IRON)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.DIAMOND)) ? 1.2F:1.0F;
    	
    	float rarity = ((float)Math.pow(averageDurability, 1/2.864)*2.0F + fullProt*3.74F + ench*0.7F)/(rep*vanillaMulti);
    	if(rarity>=100.0F)
    		rarity=100.0F;
    	return 1.0F-(rarity/100.0F);
    }
    
    /**armortype: 0 = helmet, 1 = chest, 2 = leggs, 3 = boots;  slot: equipmentstot: armortype in reverse order*/
	public static void tryEquipArmor(EntityMob living)
    {
		float time = DifficultyData.get(living.world).getDifficulty()*ConfigHandler.diffEquipAdd*0.01F;
		int maxTries = 15;
		if(living.getRNG().nextFloat() < (ConfigHandler.baseEquipChance+time) )
		{
    		ItemStack helmet = ItemStack.EMPTY;
    		int triesHelmet = 0;
			boolean helmetChance = false;
			while(!helmetChance && triesHelmet < maxTries)
			{
		        helmet = ConfigHandler.getEquipment(3);
		        helmetChance = living.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(helmet)+time);
		        if(helmetChance)
		        {
			        living.setItemStackToSlot(EntityEquipmentSlot.HEAD, helmet);
			        if(!ConfigHandler.shouldDropEquip)
		    			living.setDropChance(EntityEquipmentSlot.HEAD, -2);
			    	break;
		        }
		        triesHelmet++;
			}
	        if(ConfigHandler.baseEquipChanceAdd!=0 &&living.getRNG().nextFloat() < (ConfigHandler.baseEquipChanceAdd+time) )
			{
		    	ItemStack chest = ItemStack.EMPTY;
		    	int tries = 0;
				boolean chance = false;
				while(!chance && tries < maxTries)
				{
			        chest = ConfigHandler.getEquipment(2);
			        chance = living.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(chest)+time);
			        if(chance)
			        {
			        	living.setItemStackToSlot(EntityEquipmentSlot.CHEST, chest);
			        	if(!ConfigHandler.shouldDropEquip)
			    			living.setDropChance(EntityEquipmentSlot.CHEST, -2);
			        	break;
			        }
			        tries++;
				}
			}
	        if(ConfigHandler.baseEquipChanceAdd!=0&&living.getRNG().nextFloat() < (ConfigHandler.baseEquipChanceAdd+time) )
			{
		    	ItemStack legs = ItemStack.EMPTY;
		    	int tries = 0;
				boolean chance = false;
				while(!chance && tries < maxTries)
				{
			        legs = ConfigHandler.getEquipment(1);
			        chance = living.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(legs)+time);
			        if(chance)
			        {
			        	living.setItemStackToSlot(EntityEquipmentSlot.LEGS, legs);
			        	if(!ConfigHandler.shouldDropEquip)
			    			living.setDropChance(EntityEquipmentSlot.LEGS, -2);
			        	break;
			        }
			        tries++;
				}
			}
	        if(ConfigHandler.baseEquipChanceAdd!=0&& living.getRNG().nextFloat() < (ConfigHandler.baseEquipChanceAdd+time) )
			{
		    	ItemStack feet = ItemStack.EMPTY;
		    	int tries = 0;
				boolean chance = false;
				while(!chance && tries < maxTries)
				{
			        feet = ConfigHandler.getEquipment(0);
			        chance = living.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(feet)+time);
			        if(chance)
			        {
			        	living.setItemStackToSlot(EntityEquipmentSlot.FEET, feet);
			        	if(!ConfigHandler.shouldDropEquip)
			    			living.setDropChance(EntityEquipmentSlot.FEET, -2);
			        	break;
			        }
			        tries++;
				}
			}
		}
    }
    
    public static void enchantGear(EntityMob mob)
    {
    		for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values())
        {
	            if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR)
	            {
	            ItemStack itemstack = mob.getItemStackFromSlot(entityequipmentslot);
	
	            if (itemstack != null && mob.getRNG().nextFloat() < (ConfigHandler.baseEnchantChance+(DifficultyData.get(mob.world).getDifficulty()*ConfigHandler.diffEnchantAdd*0.01F)))
	            {
	                EnchantmentHelper.addRandomEnchantment(mob.getRNG(), itemstack, 5 + mob.getRNG().nextInt(25), true);
	            }
	        }
        }
    }
    
    
    public static float getBreakSpeed(EntityLiving entityLiving, ItemStack stack, IBlockState state)
    {
        float f = (stack == null ? 1.0F : stack.getItem().getStrVsBlock(stack, state));

        if (f > 1.0F)
        {
            int i = EnchantmentHelper.getEfficiencyModifier(entityLiving);

            if (i > 0 && stack != null)
            {
                float f1 = (float)(i * i + 1);
                boolean canHarvest = stack.canHarvestBlock(state);

                if (!canHarvest && f <= 1.0F)
                {
                    f += f1 * 0.08F;
                }
                else
                {
                    f += f1;
                }
            }
        }

        if (entityLiving.isPotionActive(Potion.getPotionFromResourceLocation("minecraft:haste")))
        {
            f *= 1.0F + (float)(entityLiving.getActivePotionEffect(Potion.getPotionFromResourceLocation("minecraft:haste")).getAmplifier() + 1) * 0.2F;
        }

        if (entityLiving.isPotionActive(Potion.getPotionFromResourceLocation("minecraft:mining_fatigue")))
        {
            f *= 1.0F - (float)(entityLiving.getActivePotionEffect(Potion.getPotionFromResourceLocation("minecraft:mining_fatigue")).getAmplifier() + 1) * 0.2F;
        }

        if (entityLiving.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(entityLiving))
        {
            f /= 5.0F;
        }

        if (!entityLiving.onGround)
        {
            f /= 5.0F;
        }
        
        return (f < 0 ? 0 : f);
    }

    public static float getBlockStrength(EntityLiving entityLiving, IBlockState state, World world, BlockPos pos)
    {
        float hardness = world.getBlockState(pos).getBlockHardness(world, pos);
        
        if (hardness < 0.0F)
        {
            return 0.0F;
        }
        
		ItemStack main = entityLiving.getHeldItemMainhand();
		ItemStack off = entityLiving.getHeldItemOffhand();

        if (!canHarvest(state, main))
        {
            return getBreakSpeed(entityLiving, main, state) / hardness / 100F;
        }
        else if (!canHarvest(state, off))
        {
            return getBreakSpeed(entityLiving, off, state) / hardness / 100F;
        }
        else
        {
            return getBreakSpeed(entityLiving, main, state) / hardness / 30F;
        }
    }
    
    public static void equipItem(EntityMob mob)
    {    		
    		if(mob.getRNG().nextFloat() < (ConfigHandler.baseItemChance) && mob.getRNG().nextFloat()<0.8)
    		{
        		int itemRand = mob.getRNG().nextInt(8);
        		ItemStack stack = ItemStack.EMPTY;
        		EntityEquipmentSlot hand = EntityEquipmentSlot.MAINHAND;
        		switch(itemRand)
        		{
        			case 0:
        				hand=EntityEquipmentSlot.OFFHAND;
        				stack=new ItemStack(Items.LAVA_BUCKET);
        				break;
        			case 1:
        				hand=EntityEquipmentSlot.OFFHAND;
        				stack=new ItemStack(Items.SNOWBALL);
        				break;
        			case 2:
        				hand=EntityEquipmentSlot.OFFHAND;
        				stack=new ItemStack(Items.ENDER_PEARL);
        				break;
        			case 3:
        				hand=EntityEquipmentSlot.MAINHAND;
        				stack=new ItemStack(Items.BOW);
        				break;
        			case 4:
        				hand=EntityEquipmentSlot.OFFHAND;
        				stack=new ItemStack(ItemBlock.getItemFromBlock(Blocks.TNT));
        				break;
        			case 5:
        				hand=EntityEquipmentSlot.OFFHAND;
        				stack=new ItemStack(Items.FLINT_AND_STEEL);
        				break;
        			case 6:
        				hand=EntityEquipmentSlot.OFFHAND;
        				stack=PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), PotionTypes.HARMING);
        				break;
        			case 7:
        				hand=EntityEquipmentSlot.OFFHAND;
        				stack=new ItemStack(Items.ENCHANTED_BOOK);
        				break;
        		}
        		if(!itemInList(stack.getItem(), ConfigHandler.itemUseBlackList))
        			mob.setItemStackToSlot(hand, stack);
    		}
    }
    
    public static boolean itemInList(Item item, String[] list)
    {
    	for(String s : list)
    		if(item.getRegistryName().toString().equals(s))
    			return true;
    	return false;
    }

    public static void modifyAttr(EntityMob mob, IAttribute att, double value, double max, boolean multiply)
    {
    		double oldValue = mob.getAttributeMap().getAttributeInstance(att).getBaseValue();
    		value *= DifficultyData.get(mob.world).getDifficulty();
    		if(multiply)
    		{
    			value = Math.min(value, max-1);
    			value = oldValue*(1+value);
    			mob.getAttributeMap().getAttributeInstance(att).setBaseValue(value);
    		}
    		else
    		{
        		value = Math.min(value, max);
    			value = oldValue+value;
    			mob.getAttributeMap().getAttributeInstance(att).setBaseValue(value);;
    		}
    }
}
