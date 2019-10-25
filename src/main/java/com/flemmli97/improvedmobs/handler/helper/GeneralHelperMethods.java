package com.flemmli97.improvedmobs.handler.helper;

import com.flemmli97.improvedmobs.entity.EntityGuardianBoat;
import com.flemmli97.improvedmobs.handler.DifficultyData;
import com.flemmli97.improvedmobs.handler.config.ConfigHandler;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GeneralHelperMethods {
	
	public static boolean isMobInList(EntityLiving living, String[] list, boolean reverse)
	{
		if(living instanceof EntityGuardianBoat)
			return true;
		if(reverse)
			return !isMobInList(living, list, false);
		for(int i = 0;i< list.length;i++)
		{
			String classPath=null;
			if(list[i].startsWith("@"))
			{
				return EntityList.getKey(living).getResourceDomain().equals(list[i].substring(1));
			}
			if(list[i].endsWith("*"))
			{
				classPath = list[i].substring(0, list[i].length()-1) + living.getClass().getSimpleName();
			}
			ResourceLocation res;
			if((classPath!=null && living.getClass().getName().equals(classPath)) || 
					((res = EntityList.getKey(living))!=null && res.toString().equals(list[i])))
					{
						return true;
					}
		}
		return false;
	}
    
    public static boolean canHarvest(IBlockState block, ItemStack item)
    {
    	return (item != null && (item.getItem().canHarvestBlock(block, item))) || block.getMaterial().isToolNotRequired();
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
    public static void tryEquipArmor(EntityMob mob)
    {
		float time = DifficultyData.getDifficulty(mob.world, mob)*ConfigHandler.diffEquipAdd*0.01F;
		int maxTries = 15;
		if(mob.getRNG().nextFloat() < (ConfigHandler.baseEquipChance+time) )
		{
    		ItemStack helmet = ItemStack.EMPTY;
    		int triesHelmet = 0;
			boolean helmetChance = false;
			while(!helmetChance && triesHelmet < maxTries)
			{
		        helmet = ConfigHandler.getEquipment(EntityEquipmentSlot.HEAD);
		        helmetChance = !helmet.isEmpty()&&mob.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(helmet)+time);
		        if(helmetChance)
		        {
		        	if(!ConfigHandler.shouldDropEquip)
		        		helmet.addEnchantment(Enchantments.VANISHING_CURSE, 1);
			        mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, helmet);
			    	break;
		        }
		        triesHelmet++;
			}
	        if(ConfigHandler.baseEquipChanceAdd!=0 &&mob.getRNG().nextFloat() < (ConfigHandler.baseEquipChanceAdd+time) )
			{
		    	ItemStack chest = ItemStack.EMPTY;
		    	int tries = 0;
				boolean chance = false;
				while(!chance && tries < maxTries)
				{
			        chest = ConfigHandler.getEquipment(EntityEquipmentSlot.CHEST);
			        chance = chest.isEmpty()&&mob.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(chest)+time);
			        if(chance)
			        {
			        	if(!ConfigHandler.shouldDropEquip)
			        		chest.addEnchantment(Enchantments.VANISHING_CURSE, 1);
			        	mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, chest);
			        	break;
			        }
			        tries++;
				}
			}
	        if(ConfigHandler.baseEquipChanceAdd!=0&&mob.getRNG().nextFloat() < (ConfigHandler.baseEquipChanceAdd+time) )
			{
		    	ItemStack legs = ItemStack.EMPTY;
		    	int tries = 0;
				boolean chance = false;
				while(!chance && tries < maxTries)
				{
			        legs = ConfigHandler.getEquipment(EntityEquipmentSlot.LEGS);
			        chance = legs.isEmpty()&&mob.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(legs)+time);
			        if(chance)
			        {
			        	if(!ConfigHandler.shouldDropEquip)
			        		legs.addEnchantment(Enchantments.VANISHING_CURSE, 1);
			        	mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, legs);
			        	break;
			        }
			        tries++;
				}
			}
	        if(ConfigHandler.baseEquipChanceAdd!=0&& mob.getRNG().nextFloat() < (ConfigHandler.baseEquipChanceAdd+time) )
			{
		    	ItemStack feet = ItemStack.EMPTY;
		    	int tries = 0;
				boolean chance = false;
				while(!chance && tries < maxTries)
				{
			        feet = ConfigHandler.getEquipment(EntityEquipmentSlot.FEET);
			        chance = feet.isEmpty()&&mob.getRNG().nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(feet)+time);
			        if(chance)
			        {
			        	if(!ConfigHandler.shouldDropEquip)
			        		feet.addEnchantment(Enchantments.VANISHING_CURSE, 1);
			        	mob.setItemStackToSlot(EntityEquipmentSlot.FEET, feet);
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
	            if (!itemstack.isEmpty() && mob.getRNG().nextFloat() < (ConfigHandler.baseEnchantChance+(DifficultyData.getDifficulty(mob.world, mob)*ConfigHandler.diffEnchantAdd*0.01F)))
	            {
	                EnchantmentHelper.addRandomEnchantment(mob.getRNG(), itemstack, 5 + mob.getRNG().nextInt(25), true);
	            }
            }
        }
    } 
    
    public static float getBreakSpeed(EntityLiving entityLiving, ItemStack stack, IBlockState state)
    {
        float f = (stack == null ? 1.0F : stack.getItem().getDestroySpeed(stack, state));

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
        if (canHarvest(state, main))
        {
            return getBreakSpeed(entityLiving, main, state) / hardness / 30F;
        }
        else if(canHarvest(state, off))
        {
            return getBreakSpeed(entityLiving, off, state) / hardness / 30F;
        }
        else
        {
            return getBreakSpeed(entityLiving, main, state) / hardness / 100F;
        }
    }

    public static void equipItem(EntityMob mob)
    {    		
		if(mob.getRNG().nextFloat() < (ConfigHandler.baseItemChance))
		{
    		int itemRand = mob.getRNG().nextInt(8);
    		ItemStack stack = ItemStack.EMPTY;
    		switch(itemRand)
    		{
    			case 0:
    				stack=new ItemStack(Items.LAVA_BUCKET);
    				break;
    			case 1:
    				stack=new ItemStack(Items.SNOWBALL);
    				break;
    			case 2:
    				stack=new ItemStack(Items.ENDER_PEARL);
    				break;
    			case 3:
    				stack=new ItemStack(Items.SHIELD);
    				break;
    			case 4:
    				stack=new ItemStack(ItemBlock.getItemFromBlock(Blocks.TNT));
    				break;
    			case 5:
    				stack=new ItemStack(Items.FLINT_AND_STEEL);
    				break;
    			case 6:
    				stack=PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), PotionTypes.HARMING);
    				break;
    			case 7:
    				stack=new ItemStack(Items.ENCHANTED_BOOK);
    				break;
    		}
    		if(!itemInList(stack.getItem(), ConfigHandler.itemUseBlackList) && mob.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).isEmpty())
    		{
    			if(!ConfigHandler.shouldDropEquip)
    				stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
    			mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
    		}
		}
    }
    
    public static void equipWeapon(EntityMob mob)
    {
    	if(mob.getRNG().nextFloat() < (ConfigHandler.baseWeaponChance+(DifficultyData.getDifficulty(mob.world, mob)*ConfigHandler.diffWeaponChance*0.01F)))
		{
    		if(mob.getHeldItemMainhand().isEmpty())
    		{
    			ItemStack stack = ConfigHandler.randomWeapon();
    			if(!ConfigHandler.shouldDropEquip)
    				stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
    			mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
    		}
		}
    }
    
    public static boolean itemInList(Item item, String[] list)
    {
    	for(String s : list)
    		if(item.getRegistryName().toString().equals(s) || (s.startsWith("@") && item.getRegistryName().getResourceDomain().equals(s.substring(1))))
    			return true;
    	return false;
    }
    
    public static void modifyAttr(EntityMob mob, IAttribute att, double value, double max, boolean multiply)
    {
		double oldValue = mob.getAttributeMap().getAttributeInstance(att).getBaseValue();
		value *= DifficultyData.getDifficulty(mob.world, mob);
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
