package com.flemmli97.improvedmobs.handler.helper;

import com.flemmli97.improvedmobs.entity.EntityGuardianBoat;
import com.flemmli97.improvedmobs.handler.ConfigHandler;

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
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
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
					EntityList.getEntityString(living).equals(list[i]) ||
					living.getClass().getName().equals(list[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isBlockBreakable(Block block, String[] list)
	{
		for(int i = 0;i< list.length;i++)
		{
			if(block.getRegistryName().toString().equals(list[i]))
			{
				return true;
			}
		}
		return false;
	}
	
    public static boolean canHarvest(IBlockState block, ItemStack item)
    {
    		boolean nerfedPick = !Items.IRON_PICKAXE.canHarvestBlock(Blocks.STONE.getDefaultState(), new ItemStack(Items.IRON_PICKAXE));
    		return (item != null && (item.getItem().canHarvestBlock(block, item) || (item.getItem() instanceof ItemPickaxe && nerfedPick && block.getMaterial() == Material.ROCK))) || block.getMaterial().isToolNotRequired();
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
    		float rep = armor.isRepairable() ? 1.0F:0.9F;
    		float vanillaMulti = (armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.LEATHER)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.GOLD)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.CHAIN)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.IRON)||armor.getArmorMaterial() == (ItemArmor.ArmorMaterial.DIAMOND)) ? 1.2F:1.0F;
    	
    	float rarity = ((float)Math.pow(averageDurability, 1/2.864)*2.0F + fullProt*3.74F + ench*0.7F)/(rep*vanillaMulti);
    	if(rarity>=100.0)
    		rarity=100.0F;
    	return 1.0F-(rarity/100.0F);
    }
    
    /**armortype: 0 = helmet, 1 = chest, 2 = leggs, 3 = boots;  slot: equipmentstot: armortype in reverse order*/
    public static void tryEquipArmor(EntityMob living)
    {
		double time = getDifficultyAddition(living);
		if(living.worldObj.rand.nextFloat() < (ConfigHandler.equipChance+time) )
		{
	    		ItemStack helmet = ConfigHandler.getArmor(3);
	    		int triesHelmet = 0;
			boolean helmetChance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(helmet)+time);
			while(!helmetChance && triesHelmet < 3)
			{
		        helmet = ConfigHandler.getArmor(3);
		        if(!GeneralHelperMethods.armorItemList(helmet, ConfigHandler.armorBlacklist))
		        {
			        helmetChance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(helmet)+time);
			        triesHelmet++;
			        if(helmetChance)
				        living.setItemStackToSlot(EntityEquipmentSlot.HEAD, helmet);
		        }
			}
	        if(ConfigHandler.addChance!=0 &&living.worldObj.rand.nextFloat() < (ConfigHandler.addChance+time) )
			{
		    	ItemStack chest = ConfigHandler.getArmor(2);
		    	int tries = 0;
				boolean chance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(chest)+time);
				while(!chance && tries < 3)
				{
			        chest = ConfigHandler.getArmor(2);
			        if(!GeneralHelperMethods.armorItemList(chest, ConfigHandler.armorBlacklist))
			        {
				        chance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(chest)+time);
				        tries++;
				        if(chance)
				        	living.setItemStackToSlot(EntityEquipmentSlot.CHEST, chest);
				    }
				}
			}
	        if(ConfigHandler.addChance!=0&&living.worldObj.rand.nextFloat() < (ConfigHandler.addChance+time) )
			{
		    	ItemStack legs = ConfigHandler.getArmor(1);
		    	int tries = 0;
				boolean chance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(legs)+time);
				while(!chance && tries < 3)
				{
			        legs = ConfigHandler.getArmor(1);
			        if(!GeneralHelperMethods.armorItemList(legs, ConfigHandler.armorBlacklist))
			        {
				        chance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(legs)+time);
				        tries++;
				        if(chance)
				        	living.setItemStackToSlot(EntityEquipmentSlot.LEGS, legs);
			        }
				}
			}
	        if(ConfigHandler.addChance!=0&& living.worldObj.rand.nextFloat() < (ConfigHandler.addChance+time) )
			{
		    	ItemStack feet = ConfigHandler.getArmor(0);
		    	int tries = 0;
				boolean chance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(feet)+time);
				while(!chance && tries < 3)
				{
					if(!GeneralHelperMethods.armorItemList(feet, ConfigHandler.armorBlacklist))
			        {
				        feet = ConfigHandler.getArmor(0);
				        chance = living.worldObj.rand.nextFloat()<(GeneralHelperMethods.calculateArmorRarityChance(feet)+time);
				        tries++;
				        if(chance)
				        	living.setItemStackToSlot(EntityEquipmentSlot.FEET, feet);
			        }
				}
			}
		}
    }
    
    public static boolean armorItemList(ItemStack stack, String[] list)
    {
    	for(int i = 0;i< list.length;i++)
		{
			if(stack.getItem().getRegistryName().toString().equals(list[i]))
			{
				return true;
			}
		}
		return false;
    }
    
    public static void enchantGear(EntityMob mob)
    {
		double time = getDifficultyAddition(mob);
    		for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values())
        {
	            if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR)
	            {
	            ItemStack itemstack = mob.getItemStackFromSlot(entityequipmentslot);
	
	            if (itemstack != null && mob.worldObj.rand.nextFloat() < (ConfigHandler.enchantChance+time))
	            {
	                EnchantmentHelper.addRandomEnchantment(mob.worldObj.rand, itemstack, Math.max(30,(int)(5.0F + (float)mob.worldObj.rand.nextInt((int) (10+time*10)))), true);
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

    public static float getBlockStrength(EntityLiving entityLiving, IBlockState state, World world, BlockPos pos, boolean ignoreTool)
    {
    	boolean nerfedPick = !Items.IRON_PICKAXE.canHarvestBlock(Blocks.STONE.getDefaultState(), new ItemStack(Items.IRON_PICKAXE));
        float hardness = world.getBlockState(pos).getBlockHardness(world, pos);
        
        if (hardness < 0.0F)
        {
            return 0.0F;
        }
        
		ItemStack main = entityLiving.getHeldItemMainhand();
		ItemStack off = entityLiving.getHeldItemOffhand();

		boolean canHarvestMain = ignoreTool || (main != null && (main.getItem().canHarvestBlock(state, main) || (main.getItem() instanceof ItemPickaxe && nerfedPick))) || state.getMaterial().isToolNotRequired();
		boolean canHarvestOff = ignoreTool || (off != null && (off.getItem().canHarvestBlock(state, off) || (off.getItem() instanceof ItemPickaxe && nerfedPick))) || state.getMaterial().isToolNotRequired();

		
        if (!canHarvestMain)
        {
            return getBreakSpeed(entityLiving, main, state) / hardness / 100F;
        }
        else if(!canHarvestOff)
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
    		double time = getDifficultyAddition(mob);
    		
    		if(mob.worldObj.rand.nextFloat() < (ConfigHandler.itemChance+time))
    		{
        		int itemRand = mob.worldObj.rand.nextInt(8);
        		switch(itemRand)
        		{
        			case 0:
        				mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.LAVA_BUCKET));
        				break;
        			case 1:
        				mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.SNOWBALL));
        				break;
        			case 2:
        				mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL));
        				break;
        			case 3:
        				mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        				break;
        			case 4:
        				mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ItemBlock.getItemFromBlock(Blocks.TNT)));
        				break;
        			case 5:
        				mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.FLINT_AND_STEEL));
        				break;
        			case 6:
        				mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), PotionTypes.HARMING));
        				break;
        			case 7:
        				mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.ENCHANTED_BOOK));
        				break;
        		}
    		}

    }
    
    public static double getDifficultyAddition(EntityMob mob)
    {
    		float f = mob.worldObj.getDifficulty() == EnumDifficulty.HARD ? 1.1F : 1.0F;
    		double timeTotal = mob.worldObj.getTotalWorldTime();
    		double timeWorld = mob.worldObj.getWorldTime();
    		double time = Math.max(timeTotal, timeWorld);
    		if(time<24000)
    			time=0;
    		else
    			time-=24000;
    		time = (time/(double)2400000)*f;
		return time;
    }
    
    public static void modifyAttr(EntityMob mob, IAttribute att, double value, double max, boolean multiply)
    {
    		double oldValue = mob.getAttributeMap().getAttributeInstance(att).getBaseValue();
    		value *= getDifficultyAddition(mob);
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
