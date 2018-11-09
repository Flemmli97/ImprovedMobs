package com.flemmli97.improvedmobs.handler;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.entity.ai.EntityAIBlockBreaking;
import com.flemmli97.improvedmobs.entity.ai.EntityAIClimbLadder;
import com.flemmli97.improvedmobs.entity.ai.EntityAIRideBoat;
import com.flemmli97.improvedmobs.entity.ai.EntityAISteal;
import com.flemmli97.improvedmobs.entity.ai.EntityAIUseItem;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;
import com.flemmli97.improvedmobs.handler.packet.PathDebugging;
import com.flemmli97.improvedmobs.handler.tilecap.ITileOpened;
import com.flemmli97.improvedmobs.handler.tilecap.TileCapProvider;

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
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerAI {
		
	public static final ResourceLocation TileCap = new ResourceLocation(ImprovedMobs.MODID, "openedFlag");
	private static final String breaker = ImprovedMobs.MODID+":Breaker";
	private static final String modifyArmor = ImprovedMobs.MODID+":InitArmor";
	private static final String modifiyHeld = ImprovedMobs.MODID+":InitHeld";
	private static final String modifiyAttributes = ImprovedMobs.MODID+":InitAttr";

	@SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getObject() instanceof IInventory)
        {
		    event.addCapability(TileCap, new TileCapProvider());
        }
    }
	
	@SubscribeEvent
	public void entityProps(EntityConstructing e) {
		if (e.getEntity() instanceof EntityMob && e.getEntity().world!=null && !e.getEntity().world.isRemote)
		{
			if(!GeneralHelperMethods.isMobInList((EntityMob) e.getEntity(), ConfigHandler.mobListBreakBlacklist, ConfigHandler.mobListBreakWhitelist))
			{
				if(ConfigHandler.breakerChance!=0 &&e.getEntity().world.rand.nextFloat()<ConfigHandler.breakerChance)
				{
					e.getEntity().addTag(breaker);
				}
			}
			if(!(e.getEntity() instanceof IEntityOwnable))
			{
				EntityMob mob = (EntityMob) e.getEntity();
				if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.armorMobBlacklist, ConfigHandler.armorMobWhiteList))
				{
					mob.getEntityData().setBoolean(modifyArmor, false);	
				}
				if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobListUseBlacklist, ConfigHandler.mobListUseWhitelist))
				{
					mob.getEntityData().setBoolean(modifiyHeld, false);	
				}
				if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobAttributeBlackList, ConfigHandler.mobAttributeWhitelist))
				{
					mob.getEntityData().setBoolean(modifiyAttributes, false);	
				}
			}
		}
	}
	
	/*@SubscribeEvent
	public void equip(SpecialSpawn e)
	{
		if (e.getEntityLiving() instanceof EntityMob && e.getEntityLiving().world!=null && !e.getEntityLiving().world.isRemote && !(e.getEntityLiving() instanceof IEntityOwnable))
		{			
			EntityMob mob = (EntityMob) e.getEntityLiving();
			if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.armorMobBlacklist) ||(ConfigHandler.armorMobWhiteList && GeneralHelperMethods.isMobInList(mob, ConfigHandler.armorMobBlacklist)))
			{
				//List<IRecipe> r= CraftingManager.getInstance().getRecipeList(); for further things maybe	
				if(ConfigHandler.baseEquipChance!=0 )
					GeneralHelperMethods.tryEquipArmor(mob);
				if(ConfigHandler.baseEnchantChance!=0)
					GeneralHelperMethods.enchantGear(mob);		
			}
			if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobListUseBlacklist) ||(ConfigHandler.mobListUseWhitelist && GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobListUseBlacklist)))
			{
				if(ConfigHandler.baseItemChance!=0)
					GeneralHelperMethods.equipItem(mob);
			}
			if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobAttributeBlackList) ||(ConfigHandler.mobAttributeWhitelist && GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobAttributeBlackList)))
			{
				if(ConfigHandler.healthIncrease!=0)
				{
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MAX_HEALTH, ConfigHandler.healthIncrease*0.02, ConfigHandler.healthMax,  true);
					mob.setHealth(mob.getMaxHealth());
				}
				if(ConfigHandler.damageIncrease!=0)
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.ATTACK_DAMAGE, ConfigHandler.damageIncrease*0.01, ConfigHandler.damageMax,  true);
				if(ConfigHandler.speedIncrease!=0)
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MOVEMENT_SPEED, ConfigHandler.speedIncrease*0.001, ConfigHandler.speedMax,  false);
				if(ConfigHandler.knockbackIncrease!=0)
					GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, ConfigHandler.knockbackIncrease*0.002, ConfigHandler.knockbackMax,  false);
			}
		}
	}*/
	
	@SubscribeEvent
	public void entityProps(CheckSpawn e) {
		if(e.getEntityLiving() instanceof EntityLiving && !e.getWorld().isRemote)
		{
			if(GeneralHelperMethods.isMobInList((EntityLiving) e.getEntityLiving(), ConfigHandler.mobListLight, ConfigHandler.mobListLightBlackList))
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
		if(e.getEntity() instanceof EntityLiving && !e.getWorld().isRemote)
		{
			EntityLiving living= (EntityLiving) e.getEntity();
			if(!GeneralHelperMethods.isMobInList(living, ConfigHandler.mobListLadderBlacklist, ConfigHandler.mobListLadderWhitelist))
			{
				if(!(living.getNavigator() instanceof PathNavigateClimber))
					living.tasks.addTask(4, new EntityAIClimbLadder(living));
			}
		}
	    if (e.getEntity() instanceof EntityMob && !e.getWorld().isRemote) 
	    {    	
    		EntityMob mob= (EntityMob) e.getEntity();
    		boolean mobGriefing = mob.world.getGameRules().getBoolean("mobGriefing");
    		this.applyAttributesAndItems(mob);
			if(mob.getTags().contains(breaker))
			{
	    		mob.targetTasks.taskEntries.iterator().forEachRemaining(t->{if(t.action instanceof EntityAINearestAttackableTarget)
				{
					EntityAINearestAttackableTarget<?> aiNearestTarget = (EntityAINearestAttackableTarget<?>) t.action;
						Class<?> targetCls = ObfuscationReflectionHelper.getPrivateValue(EntityAINearestAttackableTarget.class, aiNearestTarget, "field_75307_b","targetClass");
						if(targetCls == EntityPlayer.class)
						{
							//if(!(mob instanceof EntityEnderman && mob instanceof EntityPigZombie))
							ObfuscationReflectionHelper.setPrivateValue(EntityAITarget.class, (EntityAITarget)aiNearestTarget, false, "field_75297_f","shouldCheckSight");
							//else if(ConfigHandler.neutralAggressiv!=0 && mob.world.rand.nextFloat() <= ConfigHandler.neutralAggressiv)
							//	ObfuscationReflectionHelper.setPrivateValue(EntityAITarget.class, (EntityAITarget)aiNearestTarget, false, "field_75297_f","shouldCheckSight");
						}
				}});
	    		if(mobGriefing)
		        {
		    		mob.tasks.addTask(1, new EntityAIBlockBreaking(mob));
		    		ItemStack stack = new ItemStack(ConfigHandler.breakingItem);
		    		if(!ConfigHandler.shouldDropEquip)
		    			stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
		    		mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
		        }
			}
	    	if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobListUseBlacklist, ConfigHandler.mobListUseWhitelist))
			{
	    		mob.tasks.addTask(3, new EntityAIUseItem(mob, 15));
	    	}
	    	if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobListStealBlacklist, ConfigHandler.mobListStealWhitelist))
			{
	    		if(mobGriefing)
	    			mob.tasks.addTask(5, new EntityAISteal(mob));
	    	}
	    	if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.mobListBoatBlacklist, ConfigHandler.mobListBoatWhitelist))
			{
	    		if(!(mob.canBreatheUnderwater() || mob.getNavigator() instanceof PathNavigateSwimmer))
	    			mob.tasks.addTask(6, new EntityAIRideBoat(mob));
	    	}
    		if(ConfigHandler.targetVillager && !(mob instanceof EntityZombie))
    		{
    			if(!(mob instanceof EntityEnderman || mob instanceof EntityPigZombie))
    				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(mob, EntityVillager.class, mob.getTags().contains("Breaker")? false:mob.world.rand.nextFloat()<=0.5));
    			else if(ConfigHandler.neutralAggressiv!=0 && mob.world.rand.nextFloat() <= ConfigHandler.neutralAggressiv)
    				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(mob, EntityVillager.class, mob.getTags().contains("Breaker")? false:mob.world.rand.nextFloat()<=0.5));
    		}
	    }
	}
	
	private void applyAttributesAndItems(EntityMob mob)
	{
		if(mob.getEntityData().hasKey(modifyArmor) && !mob.getEntityData().getBoolean(modifyArmor))
		{
			//List<IRecipe> r= CraftingManager.getInstance().getRecipeList(); for further things maybe	
			if(ConfigHandler.baseEquipChance!=0 )
				GeneralHelperMethods.tryEquipArmor(mob);
			if(ConfigHandler.baseEnchantChance!=0)
				GeneralHelperMethods.enchantGear(mob);
			mob.getEntityData().setBoolean(modifyArmor, true);
		}
		if(mob.getEntityData().hasKey(modifiyHeld) && !mob.getEntityData().getBoolean(modifiyHeld))
		{
			if(ConfigHandler.baseItemChance!=0)
				GeneralHelperMethods.equipItem(mob);
			mob.getEntityData().setBoolean(modifiyHeld, true);
		}
		if(mob.getEntityData().hasKey(modifiyAttributes) && !mob.getEntityData().getBoolean(modifiyAttributes))
		{
			if(ConfigHandler.healthIncrease!=0 && !ConfigHandler.useScalingHealthMod)
			{
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MAX_HEALTH, ConfigHandler.healthIncrease*0.016, ConfigHandler.healthMax,  true);
				mob.setHealth(mob.getMaxHealth());
			}
			if(ConfigHandler.damageIncrease!=0 && !ConfigHandler.useScalingHealthMod)
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.ATTACK_DAMAGE, ConfigHandler.damageIncrease*0.008, ConfigHandler.damageMax,  true);
			if(ConfigHandler.speedIncrease!=0)
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MOVEMENT_SPEED, ConfigHandler.speedIncrease*0.0008, ConfigHandler.speedMax,  false);
			if(ConfigHandler.knockbackIncrease!=0)
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, ConfigHandler.knockbackIncrease*0.002, ConfigHandler.knockbackMax,  false);
			mob.getEntityData().setBoolean(modifiyAttributes, true);
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
					PacketHandler.sendToAllAround(new PathDebugging(EnumParticleTypes.NOTE.getParticleID(), path.getPathPointFromIndex(i).x,path.getPathPointFromIndex(i).y,path.getPathPointFromIndex(i).z), 0, e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, 64);
				PacketHandler.sendToAllAround(new PathDebugging(EnumParticleTypes.HEART.getParticleID(), path.getFinalPathPoint().x,path.getFinalPathPoint().y,path.getFinalPathPoint().z), 0, e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, 64);
			}
		}
	}
	
	@SubscribeEvent
	public void friendlyFire(LivingAttackEvent e)
	{
		if(!ConfigHandler.friendlyFire &&e.getEntityLiving() instanceof IEntityOwnable && !e.getEntityLiving().world.isRemote)
		{
			IEntityOwnable pet = (IEntityOwnable) e.getEntityLiving();
			if(e.getSource().getTrueSource()!=null && e.getSource().getTrueSource() == pet.getOwner() && !e.getSource().getTrueSource().isSneaking())
			{
				e.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void openTile(PlayerInteractEvent.RightClickBlock e) {
		if(!e.getWorld().isRemote && !e.getEntityPlayer().isSneaking())
		{
			TileEntity tile = e.getWorld().getTileEntity(e.getPos());
			if(tile!=null && tile instanceof IInventory)
			{
				ITileOpened cap = tile.getCapability(TileCapProvider.OpenedCap, null);
				if(cap!=null)
					cap.setOpened(tile);
			}
		}
	}
	
	@SubscribeEvent
    public void equipPet(EntityInteract e)
    {
    		if(e.getTarget() instanceof EntityLiving && e.getTarget() instanceof IEntityOwnable && !e.getTarget().world.isRemote && e.getEntityPlayer().isSneaking() &&
    				!GeneralHelperMethods.isMobInList((EntityLiving) e.getTarget(), ConfigHandler.petArmorBlackList, ConfigHandler.petWhiteList))
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
	
	//FakePlayer handler
	/*@SubscribeEvent
	public void projectile(EntityJoinWorldEvent event)
	{
		if(event.getEntity() instanceof EntityThrowable)
		{
			EntityThrowable projectile = (EntityThrowable) event.getEntity();
			Entity shooter = ((EntityThrowable)event.getEntity()).getThrower();
			if(shooter instanceof FakePlayer && shooter.getTags().contains(FakePlayerHandler.fakeID))
			{
				NBTTagCompound entityNBT = projectile.writeToNBT(new NBTTagCompound());
				entityNBT.setString("ownerName", shooter.getName());
				projectile.readFromNBT(entityNBT);
			}
		}
		else if(event.getEntity() instanceof EntityArrow)
		{
			Entity shooter = ((EntityArrow)event.getEntity()).shootingEntity;
			if(shooter instanceof FakePlayer && shooter.getTags().contains(FakePlayerHandler.fakeID))
			{
				((EntityArrow)event.getEntity()).shootingEntity=((WorldServer)event.getWorld()).getEntityFromUuid(UUID.fromString(shooter.getName()));
			}
		}
	}*/
}
