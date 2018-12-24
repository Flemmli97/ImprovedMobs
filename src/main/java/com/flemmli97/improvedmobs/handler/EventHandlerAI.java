package com.flemmli97.improvedmobs.handler;

import java.lang.reflect.Field;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.entity.ai.EntityAIBlockBreaking;
import com.flemmli97.improvedmobs.entity.ai.EntityAIClimbLadder;
import com.flemmli97.improvedmobs.entity.ai.EntityAIRideBoat;
import com.flemmli97.improvedmobs.entity.ai.EntityAISteal;
import com.flemmli97.improvedmobs.entity.ai.EntityAIUseItem;
import com.flemmli97.improvedmobs.entity.ai.NewWalkNodeProcessor;
import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;
import com.flemmli97.improvedmobs.handler.packet.PathDebugging;
import com.flemmli97.improvedmobs.handler.tilecap.ITileOpened;
import com.flemmli97.improvedmobs.handler.tilecap.TileCapProvider;
import com.flemmli97.tenshilib.common.events.PathFindInitEvent;
import com.flemmli97.tenshilib.common.javahelper.ReflectionUtils;

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
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerAI {
		
	public static final ResourceLocation TileCap = new ResourceLocation(ImprovedMobs.MODID, "openedFlag");
	public static final String breaker = ImprovedMobs.MODID+":Breaker";
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
	public void config(OnConfigChangedEvent event)
	{
		if(event.getModID().equals(ImprovedMobs.MODID))
		{
			ConfigManager.sync(event.getModID(), Type.INSTANCE);
		}
	}
	
	private Field entity,nodeProcessor;
	@SubscribeEvent
	public void modifyPathfinder(PathFindInitEvent event)
    {
		if(this.entity==null)
			this.entity=ObfuscationReflectionHelper.findField(PathNavigate.class, "field_75515_a");
		if(this.nodeProcessor==null)
			this.nodeProcessor=ObfuscationReflectionHelper.findField(PathNavigate.class, "field_179695_a");
		if(event.getNavigator() instanceof PathNavigateGround)
		{
			NewWalkNodeProcessor walkNode =  new NewWalkNodeProcessor();
			EntityLiving entity = ReflectionUtils.getFieldValue(this.entity, event.getNavigator());
			walkNode.setBreakBlocks(entity.getTags().contains(EventHandlerAI.breaker));
			walkNode.setCanEnterDoors(true);
			ReflectionUtils.setFieldValue(this.nodeProcessor, event.getNavigator(), walkNode);
	        event.setPathFinder(new PathFinder(walkNode));
		}
    }
	
	@SubscribeEvent
	public void entityProps(EntityConstructing e) {
		if (e.getEntity() instanceof EntityMob && e.getEntity().world!=null && !e.getEntity().world.isRemote)
		{
			if(!GeneralHelperMethods.isMobInList((EntityMob) e.getEntity(), ConfigHandler.ai.mobListBreakBlacklist, ConfigHandler.ai.mobListBreakWhitelist))
			{
				if(ConfigHandler.ai.breakerChance!=0 &&e.getEntity().world.rand.nextFloat()<ConfigHandler.ai.breakerChance)
				{
					e.getEntity().addTag(breaker);
				}
			}
			if(!(e.getEntity() instanceof IEntityOwnable))
			{
				EntityMob mob = (EntityMob) e.getEntity();
				if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.equipment.armorMobBlacklist, ConfigHandler.equipment.armorMobWhiteList))
				{
					mob.getEntityData().setBoolean(modifyArmor, false);	
				}
				if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.ai.mobListUseBlacklist, ConfigHandler.ai.mobListUseWhitelist))
				{
					mob.getEntityData().setBoolean(modifiyHeld, false);	
				}
				if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.attributes.mobAttributeBlackList, ConfigHandler.attributes.mobAttributeWhitelist))
				{
					mob.getEntityData().setBoolean(modifiyAttributes, false);	
				}
			}
		}
	}
	
	@SubscribeEvent
	public void entityProps(CheckSpawn e) {
		if(e.getEntityLiving() instanceof EntityLiving && !e.getWorld().isRemote)
		{
			if(GeneralHelperMethods.isMobInList((EntityLiving) e.getEntityLiving(), ConfigHandler.general.mobListLight, ConfigHandler.general.mobListLightBlackList))
			{
				int light = e.getWorld().getLightFor(EnumSkyBlock.BLOCK, e.getEntity().getPosition());
				if(light>=ConfigHandler.general.light)
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
	
	private Field targetClass, shouldCheckSight;
	@SubscribeEvent
	public void onEntityLoad(EntityJoinWorldEvent e) {
		if(this.targetClass==null)
			this.targetClass=ObfuscationReflectionHelper.findField(EntityAINearestAttackableTarget.class, "field_75307_b");
		if(this.shouldCheckSight==null)
			this.shouldCheckSight=ObfuscationReflectionHelper.findField(EntityAITarget.class, "field_75297_f");
		if(e.getEntity() instanceof EntityLiving && !e.getWorld().isRemote)
		{
			EntityLiving living= (EntityLiving) e.getEntity();
			if(!GeneralHelperMethods.isMobInList(living, ConfigHandler.ai.mobListLadderBlacklist, ConfigHandler.ai.mobListLadderWhitelist))
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
	    		mob.targetTasks.taskEntries.forEach(t->{if(t.action instanceof EntityAINearestAttackableTarget)
				{
					EntityAINearestAttackableTarget<?> aiNearestTarget = (EntityAINearestAttackableTarget<?>) t.action;
					Class<?> targetCls = ReflectionUtils.getFieldValue(this.targetClass, aiNearestTarget);
					if(targetCls == EntityPlayer.class)
					{
						ReflectionUtils.setFieldValue(this.shouldCheckSight, aiNearestTarget, false);
					}
				}});
	    		if(mobGriefing)
		        {
		    		mob.tasks.addTask(1, new EntityAIBlockBreaking(mob));
		    		ItemStack stack = ConfigHandler.ai.breakingItem.getStack();
		    		if(!ConfigHandler.equipment.shouldDropEquip)
		    			stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
		    		mob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
		        }
			}
	    	if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.ai.mobListUseBlacklist, ConfigHandler.ai.mobListUseWhitelist))
			{
	    		mob.tasks.addTask(3, new EntityAIUseItem(mob, 15));
	    	}
	    	if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.ai.mobListStealBlacklist, ConfigHandler.ai.mobListStealWhitelist))
			{
	    		if(mobGriefing)
	    			mob.tasks.addTask(5, new EntityAISteal(mob));
	    	}
	    	if(!GeneralHelperMethods.isMobInList(mob, ConfigHandler.ai.mobListBoatBlacklist, ConfigHandler.ai.mobListBoatWhitelist))
			{
	    		if(!(mob.canBreatheUnderwater() || mob.getNavigator() instanceof PathNavigateSwimmer))
	    			mob.tasks.addTask(6, new EntityAIRideBoat(mob));
	    	}
    		if(ConfigHandler.ai.targetVillager && !(mob instanceof EntityZombie))
    		{
    			if(!(mob instanceof EntityEnderman || mob instanceof EntityPigZombie))
    				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(mob, EntityVillager.class, mob.getTags().contains("Breaker")? false:mob.world.rand.nextFloat()<=0.5));
    			else if(ConfigHandler.ai.neutralAggressiv!=0 && mob.world.rand.nextFloat() <= ConfigHandler.ai.neutralAggressiv)
    				mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(mob, EntityVillager.class, mob.getTags().contains("Breaker")? false:mob.world.rand.nextFloat()<=0.5));
    		}
	    }
	}
	
	private void applyAttributesAndItems(EntityMob mob)
	{
		if(mob.getEntityData().hasKey(modifyArmor) && !mob.getEntityData().getBoolean(modifyArmor))
		{
			//List<IRecipe> r= CraftingManager.getInstance().getRecipeList(); for further things maybe	
			if(ConfigHandler.equipment.baseEquipChance!=0 )
				GeneralHelperMethods.tryEquipArmor(mob);
			if(ConfigHandler.equipment.baseEnchantChance!=0)
				GeneralHelperMethods.enchantGear(mob);
			mob.getEntityData().setBoolean(modifyArmor, true);
		}
		if(mob.getEntityData().hasKey(modifiyHeld) && !mob.getEntityData().getBoolean(modifiyHeld))
		{
			if(ConfigHandler.equipment.baseItemChance!=0)
				GeneralHelperMethods.equipItem(mob);
			if(ConfigHandler.equipment.baseWeaponChance!=0)
				GeneralHelperMethods.equipWeapon(mob);
			mob.getEntityData().setBoolean(modifiyHeld, true);
		}
		if(mob.getEntityData().hasKey(modifiyAttributes) && !mob.getEntityData().getBoolean(modifiyAttributes))
		{
			if(ConfigHandler.attributes.healthIncrease!=0 && !ConfigHandler.integration.useScalingHealthMod)
			{
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MAX_HEALTH, ConfigHandler.attributes.healthIncrease*0.016, ConfigHandler.attributes.healthMax,  true);
				mob.setHealth(mob.getMaxHealth());
			}
			if(ConfigHandler.attributes.damageIncrease!=0 && !ConfigHandler.integration.useScalingHealthMod)
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.ATTACK_DAMAGE, ConfigHandler.attributes.damageIncrease*0.008, ConfigHandler.attributes.damageMax,  true);
			if(ConfigHandler.attributes.speedIncrease!=0)
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.MOVEMENT_SPEED, ConfigHandler.attributes.speedIncrease*0.0008, ConfigHandler.attributes.speedMax,  false);
			if(ConfigHandler.attributes.knockbackIncrease!=0)
				GeneralHelperMethods.modifyAttr(mob, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, ConfigHandler.attributes.knockbackIncrease*0.002, ConfigHandler.attributes.knockbackMax,  false);
			mob.getEntityData().setBoolean(modifiyAttributes, true);
		}
	}
	
	@SubscribeEvent
	public void pathDebug(LivingEvent e)
	{
		if(ConfigHandler.debug.debugPath && e.getEntityLiving() instanceof EntityLiving && !e.getEntityLiving().world.isRemote)
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
		if(!ConfigHandler.general.friendlyFire &&e.getEntityLiving() instanceof IEntityOwnable && !e.getEntityLiving().world.isRemote)
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
    				!GeneralHelperMethods.isMobInList((EntityLiving) e.getTarget(), ConfigHandler.general.petArmorBlackList, ConfigHandler.general.petWhiteList))
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
