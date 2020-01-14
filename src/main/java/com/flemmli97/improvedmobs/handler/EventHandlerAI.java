package com.flemmli97.improvedmobs.handler;

import java.lang.reflect.Field;
import java.util.List;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.entity.ai.EntityAIBlockBreaking;
import com.flemmli97.improvedmobs.entity.ai.EntityAIClimbLadder;
import com.flemmli97.improvedmobs.entity.ai.EntityAIRideBoat;
import com.flemmli97.improvedmobs.entity.ai.EntityAISteal;
import com.flemmli97.improvedmobs.entity.ai.EntityAITechGuns;
import com.flemmli97.improvedmobs.entity.ai.EntityAIUseItem;
import com.flemmli97.improvedmobs.entity.ai.NewWalkNodeProcessor;
import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.improvedmobs.handler.config.EntityModifyFlagConfig;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;
import com.flemmli97.improvedmobs.handler.helper.IMAttributes;
import com.flemmli97.improvedmobs.handler.packet.PacketHandler;
import com.flemmli97.improvedmobs.handler.packet.PathDebugging;
import com.flemmli97.improvedmobs.handler.tilecap.ITileOpened;
import com.flemmli97.improvedmobs.handler.tilecap.TileCapProvider;
import com.flemmli97.tenshilib.common.config.ConfigUtils.LoadState;
import com.flemmli97.tenshilib.common.events.PathFindInitEvent;
import com.flemmli97.tenshilib.common.javahelper.ReflectionUtils;
import com.google.common.collect.Lists;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.entities.ai.EntityAIRangedAttack;
import techguns.items.guns.GenericGun;

public class EventHandlerAI {

	public static final ResourceLocation TileCap = new ResourceLocation(ImprovedMobs.MODID, "openedFlag");
	public static final String breaker = ImprovedMobs.MODID + ":Breaker";
	private static final String modifyArmor = ImprovedMobs.MODID + ":InitArmor";
	private static final String modifyHeld = ImprovedMobs.MODID + ":InitHeld";
	private static final String modifyAttributes = ImprovedMobs.MODID + ":InitAttr";

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<TileEntity> event) {
		if(event.getObject() instanceof IInventory)
			event.addCapability(TileCap, new TileCapProvider());
	}

	@SubscribeEvent
	public void config(OnConfigChangedEvent event) {
		if(event.getModID().equals(ImprovedMobs.MODID))
			ConfigHandler.load(LoadState.SYNC);
	}

	private Field entity, nodeProcessor;

	@SubscribeEvent
	public void modifyPathfinder(PathFindInitEvent event) {
		if(this.entity == null)
			this.entity = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_75515_a");
		if(this.nodeProcessor == null)
			this.nodeProcessor = ObfuscationReflectionHelper.findField(PathNavigate.class, "field_179695_a");
		if(event.getNavigator() instanceof PathNavigateGround){
			NewWalkNodeProcessor walkNode = new NewWalkNodeProcessor();
			EntityLiving entity = ReflectionUtils.getFieldValue(this.entity, event.getNavigator());
			walkNode.setBreakBlocks(entity.getTags().contains(EventHandlerAI.breaker));
			walkNode.setCanEnterDoors(true);
			ReflectionUtils.setFieldValue(this.nodeProcessor, event.getNavigator(), walkNode);
			event.setPathFinder(new PathFinder(walkNode));
		}
	}

	@SubscribeEvent
	public void entityProps(EntityConstructing e) {
		if(e.getEntity().world != null && !e.getEntity().world.isRemote){
			if(e.getEntity() instanceof EntityLiving){
				EntityLiving living = (EntityLiving) e.getEntity();
				if(DifficultyData.getDifficulty(living.world, living) >= ConfigHandler.difficultyBreak && ConfigHandler.breakerChance != 0 && e.getEntity().world.rand.nextFloat() < ConfigHandler.breakerChance
						&& !ConfigHandler.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.BLOCKBREAK, ConfigHandler.mobListBreakWhitelist)){
					e.getEntity().addTag(breaker);
				}
				if(!ConfigHandler.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.ARMOR, ConfigHandler.armorMobWhitelist)){
					living.getEntityData().setBoolean(modifyArmor, false);
				}
				if(!ConfigHandler.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.HELDITEMS, ConfigHandler.heldMobWhitelist)){
					living.getEntityData().setBoolean(modifyHeld, false);
				}
				if(!ConfigHandler.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.ATTRIBUTES, ConfigHandler.mobAttributeWhitelist)){
					living.getEntityData().setBoolean(modifyAttributes, false);
					IMAttributes.apply(living);
				}
			}
		}
	}

	@SubscribeEvent
	public void entityProps(CheckSpawn e) {
		if(e.getEntityLiving() instanceof EntityLiving && !e.getWorld().isRemote){
			if(GeneralHelperMethods.isMobInList((EntityLiving) e.getEntityLiving(), ConfigHandler.mobListLight, ConfigHandler.mobListLightBlackList)){
				int light = e.getWorld().getLightFor(EnumSkyBlock.BLOCK, e.getEntity().getPosition());
				if(light >= ConfigHandler.light){
					e.setResult(Result.DENY);
					return;
				}else{
					e.setResult(Result.ALLOW);
					return;
				}
			}
		}
	}

	private Field targetClass, shouldCheckSight;

	@SubscribeEvent
	public void onEntityLoad(EntityJoinWorldEvent e) {
		if(this.targetClass == null)
			this.targetClass = ObfuscationReflectionHelper.findField(EntityAINearestAttackableTarget.class, "field_75307_b");
		if(this.shouldCheckSight == null)
			this.shouldCheckSight = ObfuscationReflectionHelper.findField(EntityAITarget.class, "field_75297_f");
		if(e.getWorld().isRemote)
			return;
		boolean mobGriefing = e.getWorld().getGameRules().getBoolean("mobGriefing");
		if(e.getEntity() instanceof EntityLiving){
			EntityLiving living = (EntityLiving) e.getEntity();
			this.applyAttributesAndItems(living);
			if(living.getTags().contains(breaker)){
				living.targetTasks.taskEntries.forEach(t -> {
					if(t.action instanceof EntityAINearestAttackableTarget){
						EntityAINearestAttackableTarget<?> aiNearestTarget = (EntityAINearestAttackableTarget<?>) t.action;
						Class<?> targetCls = ReflectionUtils.getFieldValue(this.targetClass, aiNearestTarget);
						if(targetCls == EntityPlayer.class){
							ReflectionUtils.setFieldValue(this.shouldCheckSight, aiNearestTarget, false);
						}
					}
				});
				if(mobGriefing){
					living.tasks.addTask(1, new EntityAIBlockBreaking(living));
					ItemStack stack = ConfigHandler.breakingItem.getStack();
					if(!ConfigHandler.shouldDropEquip)
						stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
					living.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
				}
			}
			if(!ConfigHandler.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.USEITEM, ConfigHandler.mobListUseWhitelist)){
				living.tasks.addTask(1, new EntityAIUseItem(living, 15));
				EntityAITechGuns.applyAI(living);
			}
			if(!ConfigHandler.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.SWIMMRIDE, ConfigHandler.mobListBoatWhitelist)){
				if(!(living.canBreatheUnderwater() || living.getNavigator() instanceof PathNavigateSwimmer))
					living.tasks.addTask(6, new EntityAIRideBoat(living));
			}
			if(!ConfigHandler.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.LADDER, ConfigHandler.mobListLadderWhitelist)){
				if(!(living.getNavigator() instanceof PathNavigateClimber))
					living.tasks.addTask(4, new EntityAIClimbLadder(living));
			}
		}
		if(e.getEntity() instanceof EntityCreature){
			EntityCreature creature = (EntityCreature) e.getEntity();
			if(DifficultyData.getDifficulty(creature.world, creature) >= ConfigHandler.difficultySteal && mobGriefing && ConfigHandler.stealerChance != 0 && e.getEntity().world.rand.nextFloat() < ConfigHandler.stealerChance
					&& !ConfigHandler.entityBlacklist.testForFlag(creature, EntityModifyFlagConfig.Flags.STEAL, ConfigHandler.mobListStealWhitelist)){
				creature.tasks.addTask(5, new EntityAISteal(creature));
			}
			boolean villager = false;
			boolean neutral = creature instanceof EntityEnderman || creature instanceof EntityPigZombie;
			if(!ConfigHandler.entityBlacklist.testForFlag(creature, EntityModifyFlagConfig.Flags.TARGETVILLAGER, ConfigHandler.targetVillagerWhitelist)){
				villager = true;
				if(!neutral)
					creature.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(creature, EntityVillager.class, creature.getTags().contains("Breaker") ? false : creature.world.rand.nextFloat() <= 0.5));
			}
			if(ConfigHandler.neutralAggressiv != 0 && creature.world.rand.nextFloat() <= ConfigHandler.neutralAggressiv)
				if(neutral){
					creature.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityPlayer>(creature, EntityPlayer.class, creature.getTags().contains("Breaker") ? false : creature.world.rand.nextFloat() <= 0.5));
					if(villager)
						creature.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityVillager>(creature, EntityVillager.class, creature.getTags().contains("Breaker") ? false : creature.world.rand.nextFloat() <= 0.5));
				}
			Class<? extends EntityLiving> clss = ConfigHandler.autoTargets.get(EntityList.getKey(creature));
			if(clss != null)
				this.addAutoTargetAI(creature, clss);
		}
	}

	private <T extends EntityLiving> void addAutoTargetAI(EntityCreature c, Class<T> clss) {
		c.targetTasks.addTask(3, new EntityAINearestAttackableTarget<T>(c, clss, c.getTags().contains("Breaker") ? false : true));
	}

	private void applyAttributesAndItems(EntityLiving living) {
		if(living.getEntityData().hasKey(modifyArmor) && !living.getEntityData().getBoolean(modifyArmor)){
			//List<IRecipe> r= CraftingManager.getInstance().getRecipeList(); for further things maybe	
			GeneralHelperMethods.equipArmor(living);
			living.getEntityData().setBoolean(modifyArmor, true);
		}
		if(living.getEntityData().hasKey(modifyHeld) && !living.getEntityData().getBoolean(modifyHeld)){
			GeneralHelperMethods.equipHeld(living);
			living.getEntityData().setBoolean(modifyHeld, true);
		}
		if(ConfigHandler.baseEnchantChance != 0)
			GeneralHelperMethods.enchantGear(living);
		if(living.getEntityData().hasKey(modifyAttributes) && !living.getEntityData().getBoolean(modifyAttributes)){
			if(ConfigHandler.healthIncrease != 0 && !ConfigHandler.useScalingHealthMod){
				GeneralHelperMethods.modifyAttr(living, SharedMonsterAttributes.MAX_HEALTH, ConfigHandler.healthIncrease * 0.016, ConfigHandler.healthMax, true);
				living.setHealth(living.getMaxHealth());
			}
			if(ConfigHandler.damageIncrease != 0 && !ConfigHandler.useScalingHealthMod)
				GeneralHelperMethods.modifyAttr(living, SharedMonsterAttributes.ATTACK_DAMAGE, ConfigHandler.damageIncrease * 0.008, ConfigHandler.damageMax, true);
			if(ConfigHandler.speedIncrease != 0)
				GeneralHelperMethods.modifyAttr(living, SharedMonsterAttributes.MOVEMENT_SPEED, ConfigHandler.speedIncrease * 0.0008, ConfigHandler.speedMax, false);
			if(ConfigHandler.knockbackIncrease != 0)
				GeneralHelperMethods.modifyAttr(living, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, ConfigHandler.knockbackIncrease * 0.002, ConfigHandler.knockbackMax, false);
			if(ConfigHandler.magicResIncrease != 0)
				GeneralHelperMethods.modifyAttr(living, IMAttributes.MAGIC_RES, ConfigHandler.magicResIncrease * 0.0016, ConfigHandler.magicResMax, false);
			if(ConfigHandler.projectileIncrease != 0)
				GeneralHelperMethods.modifyAttr(living, IMAttributes.PROJ_BOOST, ConfigHandler.projectileIncrease * 0.008, ConfigHandler.projectileMax, false);

			living.getEntityData().setBoolean(modifyAttributes, true);
		}
	}

	@SubscribeEvent
	public void pathDebug(LivingEvent e) {
		if(ConfigHandler.debugPath && e.getEntityLiving() instanceof EntityLiving && !e.getEntityLiving().world.isRemote){
			Path path = ((EntityLiving) e.getEntityLiving()).getNavigator().getPath();
			if(path != null){
				for(int i = 0; i < path.getCurrentPathLength(); i++)
					PacketHandler.sendToAllAround(new PathDebugging(EnumParticleTypes.NOTE.getParticleID(), path.getPathPointFromIndex(i).x, path.getPathPointFromIndex(i).y, path.getPathPointFromIndex(i).z), 0, e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, 64);
				PacketHandler.sendToAllAround(new PathDebugging(EnumParticleTypes.HEART.getParticleID(), path.getFinalPathPoint().x, path.getFinalPathPoint().y, path.getFinalPathPoint().z), 0, e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, 64);
			}
		}
	}

	@SubscribeEvent
	public void hurtEvent(LivingHurtEvent e) {
		DamageSource source = e.getSource();
		if(source.isProjectile() && source.getTrueSource() instanceof EntityMob)
			e.setAmount((float) (e.getAmount() * (1 + this.getAttValue((EntityMob) source.getTrueSource(), IMAttributes.PROJ_BOOST))));
		if(e.getEntity() instanceof EntityMob){
			if(e.getSource().isMagicDamage())
				e.setAmount((float) (e.getAmount() * (1 - this.getAttValue((EntityMob) e.getEntity(), IMAttributes.MAGIC_RES))));
		}
	}

	private double getAttValue(EntityMob mob, IAttribute att) {
		IAttributeInstance inst = mob.getEntityAttribute(att);
		if(inst != null)
			return inst.getAttributeValue();
		return 0;
	}

	@SubscribeEvent
	public void attackEvent(LivingAttackEvent e) {
		if(!ConfigHandler.friendlyFire && e.getEntityLiving() instanceof IEntityOwnable && !e.getEntityLiving().world.isRemote){
			IEntityOwnable pet = (IEntityOwnable) e.getEntityLiving();
			if(e.getSource().getTrueSource() != null && e.getSource().getTrueSource() == pet.getOwner() && !e.getSource().getTrueSource().isSneaking()){
				e.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void openTile(PlayerInteractEvent.RightClickBlock e) {
		if(!e.getWorld().isRemote && !e.getEntityPlayer().isSneaking()){
			TileEntity tile = e.getWorld().getTileEntity(e.getPos());
			if(tile != null && tile instanceof IInventory){
				ITileOpened cap = tile.getCapability(TileCapProvider.OpenedCap, null);
				if(cap != null)
					cap.setOpened(tile);
			}
		}
	}

	@SubscribeEvent
	public void equipPet(EntityInteract e) {
		if(e.getHand() == EnumHand.MAIN_HAND && e.getTarget() instanceof EntityLiving && e.getTarget() instanceof IEntityOwnable && !e.getTarget().world.isRemote && e.getEntityPlayer().isSneaking()
				&& !GeneralHelperMethods.isMobInList((EntityLiving) e.getTarget(), ConfigHandler.petArmorBlackList, ConfigHandler.petWhiteList)){
			IEntityOwnable pet = (IEntityOwnable) e.getTarget();
			if(e.getEntityPlayer() == pet.getOwner()){
				EntityLiving living = (EntityLiving) e.getTarget();
				ItemStack heldItem = e.getEntityPlayer().getHeldItemMainhand();
				if(heldItem.getItem() instanceof ItemArmor){
					ItemArmor armor = (ItemArmor) heldItem.getItem();
					EntityEquipmentSlot type = armor.armorType;
					switch(type){
						case HEAD:
							this.equipPetItem(e.getEntityPlayer(), living, heldItem, EntityEquipmentSlot.HEAD);
							break;
						case CHEST:
							this.equipPetItem(e.getEntityPlayer(), living, heldItem, EntityEquipmentSlot.CHEST);
							break;
						case LEGS:
							this.equipPetItem(e.getEntityPlayer(), living, heldItem, EntityEquipmentSlot.LEGS);
							break;
						case FEET:
							this.equipPetItem(e.getEntityPlayer(), living, heldItem, EntityEquipmentSlot.FEET);
							break;
						default:
							break;
					}
					e.setCanceled(true);
				}
			}
		}
	}

	private void equipPetItem(EntityPlayer player, EntityLiving living, ItemStack stack, EntityEquipmentSlot slot) {
		ItemStack current = living.getItemStackFromSlot(slot);
		if(!current.isEmpty() && !player.capabilities.isCreativeMode){
			EntityItem entityitem = new EntityItem(living.world, living.posX, living.posY, living.posZ, current);
			entityitem.setNoPickupDelay();
			living.world.spawnEntity(entityitem);
		}
		living.setItemStackToSlot(slot, stack.copy());
		if(!player.capabilities.isCreativeMode)
			stack.shrink(1);
	}

	@SubscribeEvent
	public void techGunsChange(LivingEquipmentChangeEvent event) {
		if(ConfigHandler.useTGunsMod && !event.getEntity().world.isRemote && event.getEntity() instanceof EntityMob && event.getSlot() == EntityEquipmentSlot.MAINHAND && event.getTo().getItem() instanceof GenericGun){
			EntityMob mob = (EntityMob) event.getEntity();
			boolean hadAI = false;
			List<EntityAITechGuns> list = Lists.newArrayList();
			for(EntityAITasks.EntityAITaskEntry entry : mob.tasks.taskEntries){
				if(entry.action instanceof EntityAITechGuns)
					list.add((EntityAITechGuns) entry.action);
				if(entry.action instanceof EntityAIRangedAttack)
					hadAI = true;
			}
			list.forEach(ai -> {
				mob.tasks.removeTask(ai);
			});
			//removeAttackAI(mob);
			if(!hadAI)
				EntityAITechGuns.applyAI(mob);
		}
	}

	public static void removeAttackAI(EntityMob mob) {
		List<EntityAIAttackMelee> list = Lists.newArrayList();
		mob.tasks.taskEntries.forEach(entry -> {
			if(entry.action instanceof EntityAIAttackMelee)
				list.add((EntityAIAttackMelee) entry.action);
		});
		list.forEach(ai -> {
			mob.tasks.removeTask(ai);
		});
	}
}
