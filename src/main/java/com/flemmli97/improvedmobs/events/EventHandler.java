package com.flemmli97.improvedmobs.events;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.ai.BlockBreakGoal;
import com.flemmli97.improvedmobs.ai.FlyRidingGoal;
import com.flemmli97.improvedmobs.ai.IGoalModifier;
import com.flemmli97.improvedmobs.ai.ItemUseGoal;
import com.flemmli97.improvedmobs.ai.LadderClimbGoal;
import com.flemmli97.improvedmobs.ai.StealGoal;
import com.flemmli97.improvedmobs.ai.WaterRidingGoal;
import com.flemmli97.improvedmobs.capability.PlayerDifficultyData;
import com.flemmli97.improvedmobs.capability.TileCap;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import com.flemmli97.improvedmobs.commands.IMCommand;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.config.EntityModifyFlagConfig;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.flemmli97.improvedmobs.mixin.MobEntityMixin;
import com.flemmli97.improvedmobs.mixin.NearestTargetGoalMixin;
import com.flemmli97.improvedmobs.mixin.TargetGoalMixin;
import com.flemmli97.improvedmobs.utils.GeneralHelperMethods;
import com.flemmli97.improvedmobs.utils.IMAttributes;
import com.flemmli97.improvedmobs.utils.INodeBreakable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
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
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.FlyingPathNavigator;
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
import net.minecraft.world.World;
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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.List;
import java.util.function.Predicate;

public class EventHandler {

    public static final ResourceLocation tileCap = new ResourceLocation(ImprovedMobs.MODID, "opened_flag");
    public static final ResourceLocation playerCap = new ResourceLocation(ImprovedMobs.MODID, "player_difficulty");
    public static final String breaker = ImprovedMobs.MODID + ":Breaker";
    public static final String flyer = ImprovedMobs.MODID + ":Flyer";
    private static final String modifyArmor = ImprovedMobs.MODID + ":ModifyArmor";
    private static final String modifyHeld = ImprovedMobs.MODID + ":ModifyHeld";
    private static final String modifyAttributes = ImprovedMobs.MODID + ":ModifyAttr";
    private static final String enchanted = ImprovedMobs.MODID + ":DoEnchant";

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<TileEntity> event) {
        event.addCapability(tileCap, new TileCap());
    }

    @SubscribeEvent
    public void attachCapabilityPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayerEntity)
            event.addCapability(playerCap, new PlayerDifficultyData());
    }

    /**
     * Move the init of default config to {@link WorldEvent.Load} cause {@link FMLServerStartingEvent} is too late.
     * Entities are already loaded at that point
     */
    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld && ((ServerWorld) event.getWorld()).getDimensionKey() == World.OVERWORLD) {
            Config.CommonConfig.serverInit((ServerWorld) event.getWorld());
        }
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
                if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.ARMOR, Config.CommonConfig.armorMobWhitelist)) {
                    living.getPersistentData().putBoolean(modifyArmor, true);
                }
                if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.HELDITEMS, Config.CommonConfig.heldMobWhitelist)) {
                    living.getPersistentData().putBoolean(modifyHeld, true);
                }
                if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.ATTRIBUTES, Config.CommonConfig.mobAttributeWhitelist)) {
                    living.getPersistentData().putBoolean(modifyAttributes, true);
                }
            }
        }
    }

    @SubscribeEvent
    public void entityProps(LivingSpawnEvent.CheckSpawn e) {
        if (e.getEntity() instanceof MobEntity && !e.getWorld().isRemote()) {
            if (GeneralHelperMethods.isMobInList((MobEntity) e.getEntity(), Config.CommonConfig.mobListLight, Config.CommonConfig.mobListLightBlackList)) {
                int light = e.getWorld().getLightFor(LightType.BLOCK, e.getEntity().getPosition());
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
                ModifiableAttributeInstance inst = boat.getAttribute(Attributes.MAX_HEALTH);
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
            if (!living.getPersistentData().contains(breaker)) {
                if (DifficultyData.getDifficulty(living.world, living) >= Config.CommonConfig.difficultyBreak && Config.CommonConfig.breakerChance != 0 && e.getEntity().world.rand.nextFloat() < Config.CommonConfig.breakerChance
                        && !Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.BLOCKBREAK, Config.CommonConfig.mobListBreakWhitelist)) {
                    living.getPersistentData().putBoolean(breaker, true);
                } else
                    living.getPersistentData().putBoolean(breaker, false);
            }
            if (!living.getPersistentData().contains(flyer)) {
                if (living.world.rand.nextFloat() < Config.CommonConfig.flyAIChance && !Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.PARROT, Config.CommonConfig.mobListFlyWhitelist))
                    living.getPersistentData().putBoolean(flyer, true);
                else
                    living.getPersistentData().putBoolean(flyer, false);
            }
            boolean canBreak = living.getPersistentData().getBoolean(breaker);
            boolean canFly = living.getPersistentData().getBoolean(flyer);
            this.applyAttributesAndItems(living);
            if (canBreak) {
                ((IGoalModifier) living.targetSelector).modifyGoal(NearestAttackableTargetGoal.class, (g) -> {
                    if (g instanceof NearestAttackableTargetGoal && living.world.rand.nextFloat() < 0.5) {
                        ((TargetGoalMixin) g).setShouldCheckSight(false);
                        ((NearestTargetGoalMixin) g).getTargetEntitySelector().setIgnoresLineOfSight();
                    }
                });
                if (mobGriefing) {
                    living.goalSelector.addGoal(1, new BlockBreakGoal(living));
                    //Cause of #115
                    if (living.getNavigator() == null || living.getNavigator().getNodeProcessor() == null)
                        throw new NullPointerException("Navigator null! " + living + " ; " + living.getNavigator());
                    ((INodeBreakable) living.getNavigator().getNodeProcessor()).setCanBreakBlocks(true);
                    ItemStack stack = Config.CommonConfig.getRandomBreakingItem(living.getRNG());
                    if (!Config.CommonConfig.shouldDropEquip)
                        stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                    living.setItemStackToSlot(EquipmentSlotType.OFFHAND, stack);
                }
            }
            if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.USEITEM, Config.CommonConfig.mobListUseWhitelist)) {
                living.goalSelector.addGoal(1, new ItemUseGoal(living, 15));
                //EntityAITechGuns.applyAI(living);
            }
            if (canFly) {
                //Exclude slime. They cant attack while riding anyway. Too much hardcoded things
                if (!(((MobEntityMixin) living).getTrueNavigator() instanceof FlyingPathNavigator) && !(living instanceof SlimeEntity)) {
                    living.goalSelector.addGoal(6, new FlyRidingGoal(living));
                }
            }
            if (living.world.rand.nextFloat() < Config.CommonConfig.guardianAIChance && !Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.GUARDIAN, Config.CommonConfig.mobListBoatWhitelist)) {
                //Exclude slime. They cant attack while riding anyway. Too much hardcoded things
                if (!(((MobEntityMixin) living).getTrueNavigator() instanceof SwimmerPathNavigator) && !(living instanceof SlimeEntity)) {
                    living.goalSelector.addGoal(6, new WaterRidingGoal(living));
                }
            }
            if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.LADDER, Config.CommonConfig.mobListLadderWhitelist)) {
                if (!(living.getNavigator() instanceof ClimberPathNavigator)) {
                    ((INodeBreakable) living.getNavigator().getNodeProcessor()).setCanClimbLadder(true);
                    living.goalSelector.addGoal(4, new LadderClimbGoal(living));
                }
            }
            boolean villager = !Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.TARGETVILLAGER, Config.CommonConfig.targetVillagerWhitelist);
            boolean aggressive;
            if ((living instanceof IAngerable) && !Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.NEUTRALAGGRO, Config.CommonConfig.neutralAggroWhitelist)) {
                aggressive = Config.CommonConfig.neutralAggressiv != 0 && living.world.rand.nextFloat() < Config.CommonConfig.neutralAggressiv;
                if (aggressive)
                    living.targetSelector.addGoal(1, this.setNoLoS(living, PlayerEntity.class, !canBreak || living.world.rand.nextFloat() < 0.5, null));
            } else
                aggressive = true;
            if (villager && aggressive) {
                ((IGoalModifier) living.targetSelector).goalRemovePredicate(g -> g instanceof NearestTargetGoalMixin && ((NearestTargetGoalMixin) g).targetTypeClss() == AbstractVillagerEntity.class);
                living.targetSelector.addGoal(3, this.setNoLoS(living, AbstractVillagerEntity.class, !canBreak || living.world.rand.nextFloat() < 0.5, null));
            }
            List<EntityType<?>> types = Config.CommonConfig.autoTargets.get(living.getType().getRegistryName());
            if (types != null)
                living.targetSelector.addGoal(3, this.setNoLoS(living, LivingEntity.class, !canBreak || living.world.rand.nextFloat() < 0.5, (l) -> types.contains(l.getType())));
        }
        if (e.getEntity() instanceof CreatureEntity) {
            CreatureEntity creature = (CreatureEntity) e.getEntity();
            if (DifficultyData.getDifficulty(creature.world, creature) >= Config.CommonConfig.difficultySteal && mobGriefing && Config.CommonConfig.stealerChance != 0 && e.getEntity().world.rand.nextFloat() < Config.CommonConfig.stealerChance
                    && !Config.CommonConfig.entityBlacklist.hasFlag(creature, EntityModifyFlagConfig.Flags.STEAL, Config.CommonConfig.mobListStealWhitelist)) {
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
            ((NearestTargetGoalMixin) goal).getTargetEntitySelector().setIgnoresLineOfSight();
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
            float difficulty = DifficultyData.getDifficulty(living.world, living);
            if (Config.CommonConfig.healthIncrease != 0 && !Config.CommonConfig.useScalingHealthMod) {
                GeneralHelperMethods.modifyAttr(living, Attributes.MAX_HEALTH, Config.CommonConfig.healthIncrease * 0.016, Config.CommonConfig.healthMax, difficulty, true);
                living.setHealth(living.getMaxHealth());
            }
            if (Config.CommonConfig.damageIncrease != 0 && !Config.CommonConfig.useScalingHealthMod)
                GeneralHelperMethods.modifyAttr(living, Attributes.ATTACK_DAMAGE, Config.CommonConfig.damageIncrease * 0.008, Config.CommonConfig.damageMax, difficulty, true);
            if (Config.CommonConfig.speedIncrease != 0)
                GeneralHelperMethods.modifyAttr(living, Attributes.MOVEMENT_SPEED, Config.CommonConfig.speedIncrease * 0.0008, Config.CommonConfig.speedMax, difficulty, false);
            if (Config.CommonConfig.knockbackIncrease != 0)
                GeneralHelperMethods.modifyAttr(living, Attributes.KNOCKBACK_RESISTANCE, Config.CommonConfig.knockbackIncrease * 0.002, Config.CommonConfig.knockbackMax, difficulty, false);
            if (Config.CommonConfig.magicResIncrease != 0)
                IMAttributes.apply(living, IMAttributes.Attribute.MAGIC_RES, Math.min(Config.CommonConfig.magicResIncrease * 0.0016f * difficulty, Config.CommonConfig.magicResMax));
            //GeneralHelperMethods.modifyAttr(living, IMAttributes.MAGIC_RES, Config.ServerConfig.magicResIncrease * 0.0016, Config.ServerConfig.magicResMax, false);
            if (Config.CommonConfig.projectileIncrease != 0)
                IMAttributes.apply(living, IMAttributes.Attribute.PROJ_BOOST, Config.CommonConfig.projectileMax <= 0 ? Config.CommonConfig.projectileIncrease * 0.008f * difficulty : Math.min(Config.CommonConfig.projectileIncrease * 0.008f * difficulty, Config.CommonConfig.projectileIncrease - 1));
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
        if (!e.getEntity().world.isRemote) {
            if (!Config.CommonConfig.friendlyFire && e.getEntityLiving() instanceof TameableEntity) {
                TameableEntity pet = (TameableEntity) e.getEntity();
                if (e.getSource().getTrueSource() != null && e.getSource().getTrueSource() == pet.getOwner() && !e.getSource().getTrueSource().isSneaking()) {
                    e.setCanceled(true);
                    return;
                }
            }
            Entity source = e.getSource().getTrueSource();
            if (e.getEntityLiving() instanceof PlayerEntity) {
                Entity direct = e.getSource().getImmediateSource();
                if (direct instanceof SnowballEntity && direct.getPersistentData().getBoolean(ImprovedMobs.thrownEntityID)) {
                    direct.getPersistentData().remove(ImprovedMobs.thrownEntityID);
                    e.getEntity().attackEntityFrom(e.getSource(), 0.001f);
                }
            } else if (source instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) source;
                if (attacker.getHeldItemMainhand().canDisableShield(e.getEntityLiving().getActiveItemStack(), e.getEntityLiving(), attacker)) {
                    triggerDisableShield(attacker, e.getEntityLiving());
                }
            }
        }
    }

    private static void triggerDisableShield(LivingEntity attacker, LivingEntity target) {
        float f = 0.25F + EnchantmentHelper.getEfficiencyModifier(attacker) * 0.05F;
        if (attacker.isSprinting()) {
            f += 0.75F;
        }
        if (attacker.getRNG().nextFloat() < f) {
            target.getPersistentData().putBoolean(ImprovedMobs.disableShield, true);
            target.resetActiveHand();
            target.world.setEntityState(target, (byte) 30);
        }
    }

    @SubscribeEvent
    public void openTile(PlayerInteractEvent.RightClickBlock e) {
        if (!e.getWorld().isRemote && !e.getPlayer().isSneaking()) {
            TileEntity tile = e.getWorld().getTileEntity(e.getPos());
            if (tile != null) {
                tile.getCapability(TileCapProvider.OpenedCap, null)
                        .ifPresent(iTileOpened -> iTileOpened.setOpened(tile));
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
            ItemEntity entityitem = new ItemEntity(living.world, living.getPosX(), living.getPosY(), living.getPosZ(), current);
            entityitem.setNoPickupDelay();
            living.world.addEntity(entityitem);
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        living.setItemStackToSlot(slot, copy);
        if (!player.isCreative())
            stack.shrink(1);
    }

    /*@SubscribeEvent
    public void techGunsChange(LivingEquipmentChangeEvent event) {
        if(Confige.commonConf.useTGunsMod && !event.getEntity().world.isRemote && event.getEntity() instanceof MonsterEntity && event.getSlot() == EquipmentSlotType.MAINHAND && event.getTo().getItem() instanceof GenericGun){
            MonsterEntity mob = (MonsterEntity) event.getEntity();
            boolean hadAI = false;
            List<EntityAITechGuns> list = new ArrayList<>();
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
            Entity thrower = ((ProjectileEntity) e.getEntity()).getShooter();
            if (thrower instanceof MobEntity) {
                if (!(e.getEntity() instanceof PotionEntity) && e.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY) {
                    EntityRayTraceResult res = (EntityRayTraceResult) e.getRayTraceResult();
                    if (!res.getEntity().equals(((MobEntity) thrower).getAttackTarget()))
                        e.setCanceled(true);
                }
            }
        }
    }

    //Note: Sodium-Forge breaks this since they modify explosion but dont call the forge event
    @SubscribeEvent
    public void explosion(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getExploder() instanceof TNTEntity && event.getExplosion().getExploder().getPersistentData().contains(ImprovedMobs.thrownEntityID)) {
            event.getAffectedBlocks().clear();
            LivingEntity igniter = event.getExplosion().getExplosivePlacedBy();
            if (igniter instanceof MobEntity) {
                event.getAffectedEntities().removeIf(e -> !e.equals(((MobEntity) igniter).getAttackTarget()));
            }
        }
    }

    /*public static void removeAttackAI(MonsterEntity mob) {
        List<EntityAIAttackMelee> list = new ArrayList<>();
        mob.tasks.taskEntries.forEach(entry -> {
            if(entry.action instanceof EntityAIAttackMelee)
                list.add((EntityAIAttackMelee) entry.action);
        });
        list.forEach(mob.tasks::removeTask);
    }*/
}
