package com.flemmli97.improvedmobs.events;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.ai.BlockBreakGoal;
import com.flemmli97.improvedmobs.ai.IGoalModifier;
import com.flemmli97.improvedmobs.ai.ILadderFlagNode;
import com.flemmli97.improvedmobs.ai.ItemUseGoal;
import com.flemmli97.improvedmobs.ai.LadderClimbGoal;
import com.flemmli97.improvedmobs.ai.StealGoal;
import com.flemmli97.improvedmobs.ai.WaterRidingGoal;
import com.flemmli97.improvedmobs.capability.ITileOpened;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import com.flemmli97.improvedmobs.commands.IMCommand;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.config.EntityModifyFlagConfig;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.flemmli97.improvedmobs.mixin.NearestTargetGoalMixin;
import com.flemmli97.improvedmobs.mixin.PathNavigatorAccessor;
import com.flemmli97.improvedmobs.mixin.TargetGoalMixin;
import com.flemmli97.improvedmobs.utils.GeneralHelperMethods;
import com.flemmli97.improvedmobs.utils.IMAttributes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.SwimmerPathNavigator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class EventHandler {

    public static final ResourceLocation tileCap = new ResourceLocation(ImprovedMobs.MODID, "opened_flag");
    public static final String breaker = ImprovedMobs.MODID + ":Breaker";
    private static final String modifyArmor = ImprovedMobs.MODID + ":ModifyArmor";
    private static final String modifyHeld = ImprovedMobs.MODID + ":ModifyHeld";
    private static final String modifyAttributes = ImprovedMobs.MODID + ":ModifyAttr";
    private static final String enchanted = ImprovedMobs.MODID + ":DoEnchant";

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof IInventory)
            event.addCapability(tileCap, new TileCapProvider());
    }

    @SubscribeEvent
    public void serverStart(FMLServerStartingEvent event) {
        Config.CommonConfig.serverInit(event.getServer());
    }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent event) {
        IMCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void entityProps(EntityEvent.EntityConstructing e) {
        if (e.getEntity().world != null && !e.getEntity().world.isRemote) {
            if (e.getEntity() instanceof MobEntity) {
                MobEntity living = (MobEntity) e.getEntity();
                if (DifficultyData.getDifficulty(living.world, living) >= Config.CommonConfig.difficultyBreak && Config.CommonConfig.breakerChance != 0 && e.getEntity().world.rand.nextFloat() < Config.CommonConfig.breakerChance
                        && !Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.BLOCKBREAK, Config.CommonConfig.mobListBreakWhitelist)) {
                    living.getPersistentData().putBoolean(breaker, true);
                }
                if (!Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.ARMOR, Config.CommonConfig.armorMobWhitelist)) {
                    living.getPersistentData().putBoolean(modifyArmor, true);
                }
                if (!Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.HELDITEMS, Config.CommonConfig.heldMobWhitelist)) {
                    living.getPersistentData().putBoolean(modifyHeld, true);
                }
                if (!Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.ATTRIBUTES, Config.CommonConfig.mobAttributeWhitelist)) {
                    living.getPersistentData().putBoolean(modifyAttributes, true);
                }
            }
        }
    }

    @SubscribeEvent
    public void entityProps(LivingSpawnEvent.CheckSpawn e) {
        if (e.getEntity() instanceof MobEntity && !e.getWorld().isRemote()) {
            if (GeneralHelperMethods.isMobInList((MobEntity) e.getEntity(), Config.CommonConfig.mobListLight, Config.CommonConfig.mobListLightBlackList)) {
                int light = e.getWorld().getLightLevel(LightType.BLOCK, e.getEntity().getBlockPos());
                if (light >= Config.CommonConfig.light) {
                    e.setResult(Event.Result.DENY);
                } else {
                    e.setResult(Event.Result.ALLOW);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLoad(EntityJoinWorldEvent e) {
        if (e.getWorld().isRemote)
            return;
        if (e.getEntity() instanceof MobEntity && e.getEntity().getPersistentData().getBoolean(ImprovedMobs.waterRiding)) {
            MobEntity boat = (MobEntity) e.getEntity();
            boat.getPersistentData().putBoolean(modifyArmor, false);
            boat.getPersistentData().putBoolean(modifyHeld, false);
            if (!boat.getPersistentData().contains(modifyAttributes) || boat.getPersistentData().getBoolean(modifyAttributes)) {
                boat.getPersistentData().putBoolean(modifyAttributes, false);
                ModifiableAttributeInstance inst = boat.getAttribute(Attributes.GENERIC_MAX_HEALTH);
                if (inst != null)
                    inst.setBaseValue(5);
                boat.setHealth(boat.getMaxHealth());
            }
            ((IGoalModifier) boat.goalSelector).goalRemovePredicate((g) -> !(g instanceof LookAtGoal || g instanceof RandomWalkingGoal || g instanceof RandomSwimmingGoal));
            ((IGoalModifier) boat.targetSelector).goalRemovePredicate((g) -> true);
            return;
        }
        boolean mobGriefing = e.getWorld().getGameRules().getBoolean(GameRules.MOB_GRIEFING);
        if (e.getEntity() instanceof MobEntity) {
            MobEntity living = (MobEntity) e.getEntity();
            this.applyAttributesAndItems(living);
            if (living.getPersistentData().getBoolean(breaker)) {
                ((IGoalModifier) living.targetSelector).modifyGoal(NearestAttackableTargetGoal.class, (g) -> {
                    if (g instanceof NearestAttackableTargetGoal && living.world.rand.nextFloat() < 0.5) {
                        ((TargetGoalMixin) g).setShouldCheckSight(false);
                        ((NearestTargetGoalMixin) g).getTargetEntitySelector().setLineOfSiteRequired();
                    }
                });
                if (mobGriefing) {
                    living.goalSelector.addGoal(1, new BlockBreakGoal(living));
                    ItemStack stack = Config.CommonConfig.breakingItem.getStack();
                    if (!Config.CommonConfig.shouldDropEquip)
                        stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                    living.setItemStackToSlot(EquipmentSlotType.OFFHAND, stack);
                }
            }
            if (!Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.USEITEM, Config.CommonConfig.mobListUseWhitelist)) {
                living.goalSelector.addGoal(1, new ItemUseGoal(living, 15));
                //EntityAITechGuns.applyAI(living);
            }
            if (!Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.SWIMMRIDE, Config.CommonConfig.mobListBoatWhitelist)) {
                //Exclude slime. They cant attack while riding anyway. Too much hardcoded things
                if (!(/*living.canBreatheUnderwater() || */living.getNavigator() instanceof SwimmerPathNavigator) && !(living instanceof SlimeEntity))
                    living.goalSelector.addGoal(6, new WaterRidingGoal(living));
            }
            if (!Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.LADDER, Config.CommonConfig.mobListLadderWhitelist)) {
                if (!(living.getNavigator() instanceof ClimberPathNavigator)) {
                    NodeProcessor proc = ((PathNavigatorAccessor) living.getNavigator()).getNodeProcessor();
                    if (proc instanceof ILadderFlagNode)
                        ((ILadderFlagNode) proc).setCanClimbLadder(true);
                    living.goalSelector.addGoal(4, new LadderClimbGoal(living));
                }
            }
            boolean villager = false;
            boolean neutral = living instanceof EndermanEntity || living instanceof ZombifiedPiglinEntity;
            if (!Config.CommonConfig.entityBlacklist.testForFlag(living, EntityModifyFlagConfig.Flags.TARGETVILLAGER, Config.CommonConfig.targetVillagerWhitelist)) {
                villager = true;
                if (!neutral)
                    living.targetSelector.addGoal(2, this.setNoLoS(living, AbstractVillagerEntity.class, !living.getPersistentData().getBoolean(breaker) || living.world.rand.nextFloat() <= 0.5, null));
            }
            if (Config.CommonConfig.neutralAggressiv != 0 && living.world.rand.nextFloat() <= Config.CommonConfig.neutralAggressiv)
                if (neutral) {
                    living.targetSelector.addGoal(1, this.setNoLoS(living, PlayerEntity.class, !living.getPersistentData().getBoolean(breaker) || living.world.rand.nextFloat() < 0.5, null));
                    if (villager)
                        living.targetSelector.addGoal(2, this.setNoLoS(living, AbstractVillagerEntity.class, living.getPersistentData().getBoolean(breaker) || living.world.rand.nextFloat() < 0.5, null));
                }
            List<EntityType<?>> types = Config.CommonConfig.autoTargets.get(living.getType().getRegistryName());
            if (types != null)
                living.targetSelector.addGoal(3, this.setNoLoS(living, LivingEntity.class, !living.getPersistentData().getBoolean(breaker) || living.world.rand.nextFloat() < 0.5, (l) -> types.contains(l.getType())));

        }
        if (e.getEntity() instanceof CreatureEntity) {
            CreatureEntity creature = (CreatureEntity) e.getEntity();
            if (DifficultyData.getDifficulty(creature.world, creature) >= Config.CommonConfig.difficultySteal && mobGriefing && Config.CommonConfig.stealerChance != 0 && e.getEntity().world.rand.nextFloat() < Config.CommonConfig.stealerChance
                    && !Config.CommonConfig.entityBlacklist.testForFlag(creature, EntityModifyFlagConfig.Flags.STEAL, Config.CommonConfig.mobListStealWhitelist)) {
                creature.goalSelector.addGoal(5, new StealGoal(creature));
            }
        }
    }

    private <T extends LivingEntity> NearestAttackableTargetGoal<T> setNoLoS(MobEntity e, Class<T> clss, boolean sight, Predicate<LivingEntity> pred) {
        NearestAttackableTargetGoal<T> goal;
        if (pred == null)
            goal = new NearestAttackableTargetGoal<>(e, clss, sight);
        else
            goal = new NearestAttackableTargetGoal<>(e, clss, 10, sight, false, pred);
        if (!sight)
            ((NearestTargetGoalMixin) goal).getTargetEntitySelector().setLineOfSiteRequired();
        return goal;
    }

    private void applyAttributesAndItems(MobEntity living) {
        if (living.getPersistentData().getBoolean(modifyArmor)) {
            //List<IRecipe> r= CraftingManager.getInstance().getRecipeList(); for further things maybe
            GeneralHelperMethods.equipArmor(living);
            living.getPersistentData().putBoolean(modifyArmor, false);
        }
        if (living.getPersistentData().getBoolean(modifyHeld)) {
            GeneralHelperMethods.equipHeld(living);
            living.getPersistentData().putBoolean(modifyHeld, false);
        }
        if (!living.getPersistentData().contains(enchanted)) {
            GeneralHelperMethods.enchantGear(living);
            living.getPersistentData().putBoolean(enchanted, true);
        }
        if (living.getPersistentData().getBoolean(modifyAttributes)) {
            if (Config.CommonConfig.healthIncrease != 0 && !Config.CommonConfig.useScalingHealthMod) {
                GeneralHelperMethods.modifyAttr(living, Attributes.GENERIC_MAX_HEALTH, Config.CommonConfig.healthIncrease * 0.016, Config.CommonConfig.healthMax, true);
                living.setHealth(living.getMaxHealth());
            }
            if (Config.CommonConfig.damageIncrease != 0 && !Config.CommonConfig.useScalingHealthMod)
                GeneralHelperMethods.modifyAttr(living, Attributes.GENERIC_ATTACK_DAMAGE, Config.CommonConfig.damageIncrease * 0.008, Config.CommonConfig.damageMax, true);
            if (Config.CommonConfig.speedIncrease != 0)
                GeneralHelperMethods.modifyAttr(living, Attributes.GENERIC_MOVEMENT_SPEED, Config.CommonConfig.speedIncrease * 0.0008, Config.CommonConfig.speedMax, false);
            if (Config.CommonConfig.knockbackIncrease != 0)
                GeneralHelperMethods.modifyAttr(living, Attributes.GENERIC_KNOCKBACK_RESISTANCE, Config.CommonConfig.knockbackIncrease * 0.002, Config.CommonConfig.knockbackMax, false);
            if (Config.CommonConfig.magicResIncrease != 0)
                IMAttributes.apply(living, IMAttributes.Attribute.MAGIC_RES, Config.CommonConfig.magicResIncrease * 0.0016f, Config.CommonConfig.magicResMax);
            //GeneralHelperMethods.modifyAttr(living, IMAttributes.MAGIC_RES, Config.ServerConfig.magicResIncrease * 0.0016, Config.ServerConfig.magicResMax, false);
            if (Config.CommonConfig.projectileIncrease != 0)
                IMAttributes.apply(living, IMAttributes.Attribute.PROJ_BOOST, Config.CommonConfig.projectileIncrease * 0.008f, Config.CommonConfig.projectileMax);
            //GeneralHelperMethods.modifyAttr(living, IMAttributes.PROJ_BOOST, Config.ServerConfig.projectileIncrease * 0.008, Config.ServerConfig.projectileMax, false);
            living.getPersistentData().putBoolean(modifyAttributes, false);
        }
    }

    @SubscribeEvent
    public void hurtEvent(LivingHurtEvent e) {
        DamageSource source = e.getSource();
        if (source.isProjectile() && source.getTrueSource() instanceof MonsterEntity)
            e.setAmount(e.getAmount() * (1 + IMAttributes.get((MobEntity) source.getTrueSource(), IMAttributes.Attribute.PROJ_BOOST)));//this.getAttValue((MonsterEntity) source.getTrueSource(), IMAttributes.PROJ_BOOST))));
        if (e.getEntity() instanceof MonsterEntity) {
            if (e.getSource().isMagicDamage())
                e.setAmount(e.getAmount() * (1 - IMAttributes.get((MobEntity) e.getEntity(), IMAttributes.Attribute.MAGIC_RES)));//this.getAttValue((MonsterEntity) e.getEntity(), IMAttributes.MAGIC_RES))));
        }
    }

    @SubscribeEvent
    public void removeBoats(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof MobEntity && event.getEntity().getPersistentData().getBoolean(ImprovedMobs.waterRiding)) {
            if (!event.getEntity().isBeingRidden())
                event.getEntity().remove();
        }
    }

    @SubscribeEvent
    public void pathDebug(LivingEvent e) {
        if (Config.CommonConfig.debugPath && e.getEntity() instanceof MobEntity && !e.getEntity().world.isRemote) {
            Path path = ((MobEntity) e.getEntity()).getNavigator().getPath();
            if (path != null) {
                for (int i = 0; i < path.getCurrentPathLength(); i++)
                    ((ServerWorld) e.getEntity().world).spawnParticle(ParticleTypes.NOTE, path.getPathPointFromIndex(i).x + 0.5, path.getPathPointFromIndex(i).y + 0.2, path.getPathPointFromIndex(i).z + 0.5, 1, 0, 0, 0, 0);
                ((ServerWorld) e.getEntity().world).spawnParticle(ParticleTypes.HEART, path.getFinalPathPoint().x + 0.5, path.getFinalPathPoint().y + 0.2, path.getFinalPathPoint().z + 0.5, 1, 0, 0, 0, 0);
            }
        }
    }

    @SubscribeEvent
    public void attackEvent(LivingAttackEvent e) {
        if (!Config.CommonConfig.friendlyFire && e.getEntity() instanceof TameableEntity && !e.getEntity().world.isRemote) {
            TameableEntity pet = (TameableEntity) e.getEntity();
            if (e.getSource().getTrueSource() != null && e.getSource().getTrueSource() == pet.getOwner() && !e.getSource().getTrueSource().isSneaking()) {
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void openTile(PlayerInteractEvent.RightClickBlock e) {
        if (!e.getWorld().isRemote && !e.getPlayer().isSneaking()) {
            TileEntity tile = e.getWorld().getTileEntity(e.getPos());
            if (tile instanceof IInventory) {
                Optional<ITileOpened> cap = tile.getCapability(TileCapProvider.OpenedCap, null).resolve();
                cap.ifPresent(iTileOpened -> iTileOpened.setOpened(tile));
            }
        }
    }

    @SubscribeEvent
    public void equipPet(PlayerInteractEvent.EntityInteract e) {
        if (e.getHand() == Hand.MAIN_HAND && e.getTarget() instanceof MobEntity && e.getTarget() instanceof TameableEntity && !e.getTarget().world.isRemote && e.getPlayer().isSneaking()
                && !GeneralHelperMethods.isMobInList((MobEntity) e.getTarget(), Config.CommonConfig.petArmorBlackList, Config.CommonConfig.petWhiteList)) {
            TameableEntity pet = (TameableEntity) e.getTarget();
            if (e.getPlayer() == pet.getOwner()) {
                MobEntity living = (MobEntity) e.getTarget();
                ItemStack heldItem = e.getPlayer().getHeldItemMainhand();
                if (heldItem.getItem() instanceof ArmorItem) {
                    ArmorItem armor = (ArmorItem) heldItem.getItem();
                    EquipmentSlotType type = armor.getEquipmentSlot();
                    switch (type) {
                        case HEAD:
                            this.equipPetItem(e.getPlayer(), living, heldItem, EquipmentSlotType.HEAD);
                            break;
                        case CHEST:
                            this.equipPetItem(e.getPlayer(), living, heldItem, EquipmentSlotType.CHEST);
                            break;
                        case LEGS:
                            this.equipPetItem(e.getPlayer(), living, heldItem, EquipmentSlotType.LEGS);
                            break;
                        case FEET:
                            this.equipPetItem(e.getPlayer(), living, heldItem, EquipmentSlotType.FEET);
                            break;
                        default:
                            break;
                    }
                    e.setCanceled(true);
                }
            }
        }
    }

    private void equipPetItem(PlayerEntity player, MobEntity living, ItemStack stack, EquipmentSlotType slot) {
        ItemStack current = living.getItemStackFromSlot(slot);
        if (!current.isEmpty() && !player.isCreative()) {
            ItemEntity entityitem = new ItemEntity(living.world, living.getX(), living.getY(), living.getZ(), current);
            entityitem.setNoPickupDelay();
            living.world.addEntity(entityitem);
        }
        living.setItemStackToSlot(slot, stack.copy());
        if (!player.isCreative())
            stack.shrink(1);
    }

    /*@SubscribeEvent
    public void techGunsChange(LivingEquipmentChangeEvent event) {
        if(Confige.commonConf.useTGunsMod && !event.getEntity().world.isRemote && event.getEntity() instanceof MonsterEntity && event.getSlot() == EquipmentSlotType.MAINHAND && event.getTo().getItem() instanceof GenericGun){
            MonsterEntity mob = (MonsterEntity) event.getEntity();
            boolean hadAI = false;
            List<EntityAITechGuns> list = Lists.newArrayList();
            for(EntityAITasks.EntityAITaskEntry entry : mob.tasks.taskEntries){
                if(entry.action instanceof EntityAITechGuns)
                    list.add((EntityAITechGuns) entry.action);
                if(entry.action instanceof EntityAIRangedAttack)
                    hadAI = true;
            }
            list.forEach(mob.tasks::removeTask);
            //removeAttackAI(mob);
            if(!hadAI)
                EntityAITechGuns.applyAI(mob);
        }
    }*/

    @SubscribeEvent
    public void projectileImpact(ProjectileImpactEvent e) {
        if (e.getEntity() instanceof ProjectileEntity && e.getEntity().getPersistentData().contains(ImprovedMobs.thrownEntityID)) {
            Entity thrower = ((ProjectileEntity) e.getEntity()).getOwner();
            if (thrower instanceof MobEntity) {
                if (!(e.getEntity() instanceof PotionEntity) && e.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY) {
                    EntityRayTraceResult res = (EntityRayTraceResult) e.getRayTraceResult();
                    if (!res.getEntity().equals(((MobEntity) thrower).getAttackTarget()))
                        e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void explosion(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getExploder() instanceof TNTEntity && event.getExplosion().getExploder().getPersistentData().contains(ImprovedMobs.thrownEntityID)) {
            LivingEntity igniter = event.getExplosion().getExplosivePlacedBy();
            if (igniter instanceof MobEntity) {
                event.getAffectedBlocks().clear();
                event.getAffectedEntities().removeIf(e -> !e.equals(((MobEntity) igniter).getAttackTarget()));
            }
        }
    }

    /*public static void removeAttackAI(MonsterEntity mob) {
        List<EntityAIAttackMelee> list = Lists.newArrayList();
        mob.tasks.taskEntries.forEach(entry -> {
            if(entry.action instanceof EntityAIAttackMelee)
                list.add((EntityAIAttackMelee) entry.action);
        });
        list.forEach(mob.tasks::removeTask);
    }*/
}
