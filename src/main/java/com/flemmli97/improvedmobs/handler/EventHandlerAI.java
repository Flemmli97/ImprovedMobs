package com.flemmli97.improvedmobs.handler;

import com.flemmli97.improvedmobs.entity.ai.EntityAIBlockBreaking;
import com.flemmli97.improvedmobs.entity.ai.EntityAIRideBoat;
import com.flemmli97.improvedmobs.entity.ai.EntityAIUseItem;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;
import com.flemmli97.improvedmobs.handler.packet.PathDebugging;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerAI {
	
	@SubscribeEvent
	public void entityProps(EntityConstructing e) {
		if (e.getEntity() instanceof EntityMob && e.getEntity().world!=null && !e.getEntity().world.isRemote)
		{
			if(ConfigHandler.breakerChance!=0 &&e.getEntity().world.rand.nextFloat()<ConfigHandler.breakerChance)
			{
				e.getEntity().addTag("Breaker");
			}
		}
	}
	
	@SubscribeEvent
	public void equip(SpecialSpawn e)
	{
		if (e.getEntityLiving() instanceof EntityMob && e.getEntityLiving().world!=null && !e.getEntityLiving().world.isRemote && !(e.getEntityLiving() instanceof IEntityOwnable))
		{			
			EntityMob mob = (EntityMob) e.getEntityLiving();
			if(!GeneralHelperMethods.isMobInList((EntityLiving) mob, ConfigHandler.armorMobBlacklist))
			{
				//List<IRecipe> r= CraftingManager.getInstance().getRecipeList(); for further things maybe	
				if(ConfigHandler.baseEquipChance!=0 )
					GeneralHelperMethods.tryEquipArmor(mob);
				if(ConfigHandler.baseEnchantChance!=0)
					GeneralHelperMethods.enchantGear(mob);
				if(ConfigHandler.baseItemChance!=0)
					GeneralHelperMethods.equipItem(mob);
				if(ConfigHandler.healthIncrease!=0)
				{
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MAX_HEALTH, ConfigHandler.healthIncrease, ConfigHandler.healthMax,  true);
					mob.setHealth(mob.getMaxHealth());
				}
				if(ConfigHandler.damageIncrease!=0)
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.ATTACK_DAMAGE, ConfigHandler.damageIncrease, ConfigHandler.damageMax,  true);
				if(ConfigHandler.speedIncrease!=0)
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MOVEMENT_SPEED, ConfigHandler.speedIncrease, ConfigHandler.speedMax,  false);
				if(ConfigHandler.knockbackIncrease!=0)
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, ConfigHandler.knockbackIncrease, ConfigHandler.knockbackMax,  false);
			}
		}
	}
	
	@SubscribeEvent
	public void entityProps(CheckSpawn e) {
		if(e.getEntityLiving() instanceof EntityLiving && !e.getWorld().isRemote)
		{
			if(GeneralHelperMethods.isMobInList((EntityLiving) e.getEntityLiving(), ConfigHandler.mobListLight))
			{
				int light = e.getWorld().getLightFor(EnumSkyBlock.BLOCK, e.getEntity().getPosition());
				if(light>=ConfigHandler.light)
				{
					e.setResult(Result.DENY);
					return;
				}
				else
				{
					e.setResult(Result.ALLOW);
					return;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityLoad(EntityJoinWorldEvent e) {
	    if (e.getEntity() instanceof EntityMob && !e.getWorld().isRemote) 
	    {    	
    		EntityMob living= (EntityMob) e.getEntity();
    		living.targetTasks.taskEntries.iterator().forEachRemaining(t->{if(t.action instanceof EntityAINearestAttackableTarget)
			{
				EntityAINearestAttackableTarget<?> aiNearestTarget = (EntityAINearestAttackableTarget<?>) t.action;
				if(living.getTags().contains("Breaker"))
				{
					Class<?> targetCls = ObfuscationReflectionHelper.getPrivateValue(EntityAINearestAttackableTarget.class, aiNearestTarget, "field_75307_b","targetClass");
					if(targetCls == EntityPlayer.class)
					{
						if(!(living instanceof EntityEnderman && living instanceof EntityPigZombie))
							ObfuscationReflectionHelper.setPrivateValue(EntityAITarget.class, (EntityAITarget)aiNearestTarget, false, "field_75297_f","shouldCheckSight");
						else if(ConfigHandler.neutralAggressiv!=0 && living.world.rand.nextFloat() <= ConfigHandler.neutralAggressiv)
							ObfuscationReflectionHelper.setPrivateValue(EntityAITarget.class, (EntityAITarget)aiNearestTarget, false, "field_75297_f","shouldCheckSight");
					}
				}
			}});
	    	if(living.getTags().contains("Breaker"))
	        {
		    		living.tasks.addTask(1, new EntityAIBlockBreaking(living));
		    		living.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.DIAMOND_PICKAXE));
		    		if(!ConfigHandler.shouldDropEquip)
		    			living.setDropChance(EntityEquipmentSlot.OFFHAND, 0);
	        }
	    	if((ConfigHandler.mobListAsWhitelist && GeneralHelperMethods.isMobInList((EntityLiving) e.getEntity(), ConfigHandler.mobListAIBlacklist)) ||!GeneralHelperMethods.isMobInList((EntityLiving) e.getEntity(), ConfigHandler.mobListAIBlacklist))
	    	{
	    		living.tasks.addTask(3, new EntityAIUseItem(living, 15));
	    		if(!(living.canBreatheUnderwater() || living.getNavigator() instanceof PathNavigateSwimmer))
	    			living.tasks.addTask(6, new EntityAIRideBoat(living));
	    	}
    		if(ConfigHandler.targetVillager && !(living instanceof EntityZombie))
			living.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(living, EntityVillager.class, living.getTags().contains("Breaker")? false:living.world.rand.nextFloat()<=0.5));
	    }
	}
	
	@SubscribeEvent
	public void pathDebug(LivingEvent e)
	{
		if(ConfigHandler.debuggingPath && e.getEntityLiving() instanceof EntityLiving && !e.getEntityLiving().world.isRemote)
		{
			Path path= ((EntityLiving)e.getEntityLiving()).getNavigator().getPath();
			if(path!=null)
			{
				for(int i = 0; i < path.getCurrentPathLength(); i++)
					PacketHandler.sendToAllAround(new PathDebugging(EnumParticleTypes.NOTE.getParticleID(), path.getPathPointFromIndex(i).xCoord,path.getPathPointFromIndex(i).yCoord,path.getPathPointFromIndex(i).zCoord), 0, e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, 64);
				PacketHandler.sendToAllAround(new PathDebugging(EnumParticleTypes.HEART.getParticleID(), path.getFinalPathPoint().xCoord,path.getFinalPathPoint().yCoord,path.getFinalPathPoint().zCoord), 0, e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, 64);
			}
		}
	}
	
	@SubscribeEvent
	public void friendlyFire(LivingAttackEvent e)
	{
		if(!ConfigHandler.friendlyFire &&e.getEntityLiving() instanceof IEntityOwnable)
		{
			IEntityOwnable pet = (IEntityOwnable) e.getEntityLiving();
			if(e.getSource().getEntity()!=null && e.getSource().getEntity() == pet.getOwner() && !e.getSource().getEntity().isSneaking())
			{
				e.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
    public void equipPet(EntityInteract e)
    {
    		if(e.getTarget() instanceof EntityLiving && e.getTarget() instanceof IEntityOwnable && !e.getTarget().world.isRemote && e.getEntityPlayer().isSneaking() && !GeneralHelperMethods.isMobInList((EntityLiving) e.getTarget(), ConfigHandler.petArmorBlackList))
    		{
    			IEntityOwnable pet = (IEntityOwnable) e.getTarget();
    			if(e.getEntityPlayer() == pet.getOwner())
	    		{
	    			EntityLiving living = (EntityLiving) e.getTarget();
	    			
	    			ItemStack heldItem = e.getEntityPlayer().getHeldItemMainhand();
	    			if(heldItem != null && heldItem.getItem() instanceof ItemArmor)
	    			{
	    				ItemArmor armor = (ItemArmor) heldItem.getItem();
	    				EntityEquipmentSlot type = armor.armorType;
	    				switch(type)
	    				{
	    					case HEAD:
	    		    				ItemStack helmet = living.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
	    		    				if(helmet != null && !e.getEntityPlayer().capabilities.isCreativeMode)
	    		    				{
	    		    					EntityItem entityitem = new EntityItem(living.world, living.posX, living.posY, living.posZ, helmet);
	    		    		            entityitem.setNoPickupDelay();
	    		    		            living.world.spawnEntity(entityitem);
	    		    				}
		    					living.setItemStackToSlot(EntityEquipmentSlot.HEAD, heldItem.copy());
	
		    					if(!e.getEntityPlayer().capabilities.isCreativeMode)
		    					{
		    						heldItem.setCount(heldItem.getCount()-1);
			    					if(heldItem.getCount()==0 && !e.getEntityPlayer().capabilities.isCreativeMode)
			    						e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
		    					}
	    					break;
	    					case CHEST:
			    				ItemStack chest = living.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			    				
			    				if(chest != null&& !e.getEntityPlayer().capabilities.isCreativeMode)
			    				{
			    					EntityItem entityitem = new EntityItem(living.world, living.posX, living.posY, living.posZ, chest);
			    		            entityitem.setNoPickupDelay();
			    		            living.world.spawnEntity(entityitem);
			    				}
		    					living.setItemStackToSlot(EntityEquipmentSlot.CHEST, heldItem.copy());
		
		    					if(!e.getEntityPlayer().capabilities.isCreativeMode)
		    					{
		    						heldItem.setCount(heldItem.getCount()-1);
			    					if(heldItem.getCount()==0 && !e.getEntityPlayer().capabilities.isCreativeMode)
			    						e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
		    					}
						break;
	    					case LEGS:
			    				ItemStack leggs = living.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
			    				if(leggs != null&& !e.getEntityPlayer().capabilities.isCreativeMode)
			    				{
			    					EntityItem entityitem = new EntityItem(living.world, living.posX, living.posY, living.posZ, leggs);
			    		            entityitem.setNoPickupDelay();
			    		            living.world.spawnEntity(entityitem);
			    				}
	    					living.setItemStackToSlot(EntityEquipmentSlot.LEGS, heldItem.copy());
	
	    					if(!e.getEntityPlayer().capabilities.isCreativeMode)
	    					{
	    						heldItem.setCount(heldItem.getCount()-1);
		    					if(heldItem.getCount()==0 && !e.getEntityPlayer().capabilities.isCreativeMode)
		    						e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
	    					}
						break;
	    					case FEET:
			    				ItemStack boots = living.getItemStackFromSlot(EntityEquipmentSlot.FEET);
			    				if(boots != null&& !e.getEntityPlayer().capabilities.isCreativeMode)
			    				{
			    					EntityItem entityitem = new EntityItem(living.world, living.posX, living.posY, living.posZ, boots);
			    		            entityitem.setNoPickupDelay();
			    		            living.world.spawnEntity(entityitem);
			    				}
	    					living.setItemStackToSlot(EntityEquipmentSlot.FEET, heldItem.copy());
	
	    					if(!e.getEntityPlayer().capabilities.isCreativeMode)
	    					{
	    						heldItem.setCount(heldItem.getCount()-1);
		    					if(heldItem.getCount()==0 && !e.getEntityPlayer().capabilities.isCreativeMode)
		    						e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
	    					}
						break;
						default:
							break;
	    				}
	    				e.setCanceled(true);
	    			}
    			}
    		}
    }
}
