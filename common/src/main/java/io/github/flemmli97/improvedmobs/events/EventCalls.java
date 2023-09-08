package io.github.flemmli97.improvedmobs.events;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.BlockBreakGoal;
import io.github.flemmli97.improvedmobs.ai.FlyRidingGoal;
import io.github.flemmli97.improvedmobs.ai.ItemUseGoal;
import io.github.flemmli97.improvedmobs.ai.LadderClimbGoal;
import io.github.flemmli97.improvedmobs.ai.StealGoal;
import io.github.flemmli97.improvedmobs.ai.WaterRidingGoal;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.EntityModifyFlagConfig;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.entities.ServersideRegister;
import io.github.flemmli97.improvedmobs.mixin.MobEntityMixin;
import io.github.flemmli97.improvedmobs.mixin.NearestTargetGoalMixin;
import io.github.flemmli97.improvedmobs.mixin.TargetGoalMixin;
import io.github.flemmli97.improvedmobs.mixinhelper.IGoalModifier;
import io.github.flemmli97.improvedmobs.mixinhelper.INodeBreakable;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import io.github.flemmli97.improvedmobs.utils.Utils;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class EventCalls {

    public static void worldJoin(ServerPlayer player, MinecraftServer server) {
        CrossPlatformStuff.INSTANCE.sendDifficultyDataTo(player, server);
        CrossPlatformStuff.INSTANCE.sendConfigSync(player);
    }

    public static void increaseDifficulty(ServerLevel level) {
        if (!Config.CommonConfig.enableDifficultyScaling || !Config.CommonConfig.difficultyType.increaseDifficulty)
            return;
        if (level.dimension() == Level.OVERWORLD) {
            boolean shouldIncrease = (Config.CommonConfig.ignorePlayers || !level.getServer().getPlayerList().getPlayers().isEmpty()) && level.getDayTime() > Config.CommonConfig.difficultyDelay;
            DifficultyData data = DifficultyData.get(level.getServer());
            if (Config.CommonConfig.shouldPunishTimeSkip) {
                long timeDiff = Math.abs(level.getDayTime() - data.getPrevTime());
                if (timeDiff > 2400) {
                    long i = timeDiff / 2400;
                    if (timeDiff - i * 2400 > (i + 1) * 2400 - timeDiff)
                        i += 1;
                    while (i > 0) {
                        data.increaseDifficultyBy(current -> shouldIncrease && Config.CommonConfig.doIMDifficulty ? Config.CommonConfig.increaseHandler.get(current) : 0f, level.getDayTime(), level.getServer());
                        i--;
                    }
                }
            } else {
                if (level.getDayTime() - data.getPrevTime() > 2400) {
                    data.increaseDifficultyBy(current -> shouldIncrease && Config.CommonConfig.doIMDifficulty ? Config.CommonConfig.increaseHandler.get(current) : 0, level.getDayTime(), level.getServer());
                }
            }
        }
    }

    public static void onEntityLoad(Mob mob) {
        if (mob.level().isClientSide)
            return;
        EntityFlags flags = EntityFlags.get(mob);
        ServersideRegister.replaceEntity(mob);
        boolean mobGriefing = mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        float difficulty = DifficultyData.getDifficulty(mob.level(), mob);
        if (flags.canBreakBlocks == EntityFlags.FlagType.UNDEF) {
            if (difficulty >= Config.CommonConfig.difficultyBreak && Config.CommonConfig.breakerChance != 0 && mob.getRandom().nextFloat() < Config.CommonConfig.breakerChance
                    && !Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.BLOCKBREAK, Config.CommonConfig.mobListBreakWhitelist)) {
                flags.canBreakBlocks = EntityFlags.FlagType.TRUE;
            } else
                flags.canBreakBlocks = EntityFlags.FlagType.FALSE;
        }
        if (flags.canFly == EntityFlags.FlagType.UNDEF) {
            if (mob.getRandom().nextFloat() < Config.CommonConfig.flyAIChance && !Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.PARROT, Config.CommonConfig.mobListFlyWhitelist)) {
                flags.canFly = EntityFlags.FlagType.TRUE;
            } else
                flags.canFly = EntityFlags.FlagType.FALSE;
        }
        if (flags.canBreakBlocks == EntityFlags.FlagType.TRUE) {
            ((IGoalModifier) mob.targetSelector).modifyGoal(NearestAttackableTargetGoal.class, (g) -> {
                if (mob.getRandom().nextFloat() < 0.7) {
                    ((TargetGoalMixin) g).setShouldCheckSight(false);
                    ((NearestTargetGoalMixin) g).getTargetEntitySelector().ignoreLineOfSight();
                }
            });
            if (mobGriefing) {
                ((INodeBreakable) mob.getNavigation().getNodeEvaluator()).setCanBreakBlocks(true);
                mob.goalSelector.addGoal(1, new BlockBreakGoal(mob));
                if (mob.getOffhandItem().isEmpty()) {
                    ItemStack stack = Config.CommonConfig.getRandomBreakingItem(mob.getRandom());
                    if (!Config.CommonConfig.shouldDropEquip)
                        mob.setDropChance(EquipmentSlot.OFFHAND, -100);
                    mob.setItemSlot(EquipmentSlot.OFFHAND, stack);
                }
            }
        }
        applyAttributesAndItems(mob, difficulty);
        if (!Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.USEITEM, Config.CommonConfig.mobListUseWhitelist)) {
            mob.goalSelector.addGoal(1, new ItemUseGoal(mob, 12));
        }
        if (mob.getRandom().nextFloat() < Config.CommonConfig.guardianAIChance && !Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.GUARDIAN, Config.CommonConfig.mobListBoatWhitelist)) {
            //Exclude slime. They cant attack while riding anyway. Too much hardcoded things
            if (!(((MobEntityMixin) mob).getTrueNavigator() instanceof WaterBoundPathNavigation) && !(mob instanceof Slime)) {
                mob.goalSelector.addGoal(6, new WaterRidingGoal(mob));
            }
        }
        if (flags.canFly == EntityFlags.FlagType.TRUE) {
            //Exclude slime. They cant attack while riding anyway. Too much hardcoded things
            if (!(((MobEntityMixin) mob).getTrueNavigator() instanceof FlyingPathNavigation) && !(mob instanceof Slime)) {
                mob.goalSelector.addGoal(6, new FlyRidingGoal(mob));
            }
        }
        if (!Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.LADDER, Config.CommonConfig.mobListLadderWhitelist)) {
            if (!(mob.getNavigation() instanceof WallClimberNavigation)) {
                EntityFlags.get(mob).ladderClimber = true;
                mob.goalSelector.addGoal(4, new LadderClimbGoal(mob));
                ((INodeBreakable) mob.getNavigation().getNodeEvaluator()).setCanClimbLadder(true);
            }
        }
        boolean villager = !Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.TARGETVILLAGER, Config.CommonConfig.targetVillagerWhitelist);
        boolean aggressive;
        if ((mob instanceof NeutralMob) && !Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.NEUTRALAGGRO, Config.CommonConfig.neutralAggroWhitelist)) {
            aggressive = Config.CommonConfig.neutralAggressiv != 0 && mob.getRandom().nextFloat() < Config.CommonConfig.neutralAggressiv;
            if (aggressive)
                mob.targetSelector.addGoal(1, setNoLoS(mob, Player.class, flags.canBreakBlocks == EntityFlags.FlagType.TRUE || mob.getRandom().nextFloat() < 0.5, null));
        } else
            aggressive = true;
        if (villager && aggressive) {
            ((IGoalModifier) mob.targetSelector).goalRemovePredicate(g -> g instanceof NearestTargetGoalMixin target && target.targetTypeClss() == AbstractVillager.class);
            mob.targetSelector.addGoal(3, setNoLoS(mob, AbstractVillager.class, flags.canBreakBlocks == EntityFlags.FlagType.TRUE || mob.getRandom().nextFloat() < 0.5, null));
        }
        List<EntityType<?>> types = Config.CommonConfig.autoTargets.get(PlatformUtils.INSTANCE.entities().getIDFrom(mob.getType()));
        if (types != null)
            mob.targetSelector.addGoal(3, setNoLoS(mob, LivingEntity.class, flags.canBreakBlocks == EntityFlags.FlagType.TRUE || mob.getRandom().nextFloat() < 0.5, (l) -> types.contains(l.getType())));
        if (mob instanceof PathfinderMob pathfinderMob && difficulty >= Config.CommonConfig.difficultySteal && mobGriefing
                && Config.CommonConfig.stealerChance != 0 && mob.getRandom().nextFloat() < Config.CommonConfig.stealerChance
                && !Config.CommonConfig.entityBlacklist.hasFlag(mob, EntityModifyFlagConfig.Flags.STEAL, Config.CommonConfig.mobListStealWhitelist)) {
            pathfinderMob.goalSelector.addGoal(5, new StealGoal(pathfinderMob));
        }
    }

    private static <T extends LivingEntity> NearestAttackableTargetGoal<T> setNoLoS(Mob e, Class<T> clss, boolean sight, Predicate<LivingEntity> pred) {
        NearestAttackableTargetGoal<T> goal;
        if (pred == null)
            goal = new NearestAttackableTargetGoal<>(e, clss, sight);
        else
            goal = new NearestAttackableTargetGoal<>(e, clss, 10, sight, false, pred);
        if (!sight)
            ((NearestTargetGoalMixin) goal).getTargetEntitySelector().ignoreLineOfSight();
        return goal;
    }

    private static void applyAttributesAndItems(Mob living, float difficulty) {
        EntityFlags flags = EntityFlags.get(living);
        if (!flags.modifyArmor) {
            if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.ARMOR, Config.CommonConfig.armorMobWhitelist))
                Utils.equipArmor(living, difficulty);
            flags.modifyArmor = true;
        }
        if (!flags.modifyHeldItems) {
            if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.HELDITEMS, Config.CommonConfig.heldMobWhitelist))
                Utils.equipHeld(living, difficulty);
            flags.modifyHeldItems = true;
        }
        if (!flags.enchantGear) {
            Utils.enchantGear(living, difficulty);
            flags.enchantGear = true;
        }
        if (!flags.modifyAttributes) {
            if (!Config.CommonConfig.entityBlacklist.hasFlag(living, EntityModifyFlagConfig.Flags.ATTRIBUTES, Config.CommonConfig.mobAttributeWhitelist)) {
                if (Config.CommonConfig.healthIncrease != 0 && !Config.CommonConfig.useScalingHealthMod) {
                    Utils.modifyAttr(living, Attributes.MAX_HEALTH, Config.CommonConfig.healthIncrease * 0.016, Config.CommonConfig.healthMax, difficulty, true);
                    living.setHealth(living.getMaxHealth());
                }
                if (Config.CommonConfig.damageIncrease != 0 && !Config.CommonConfig.useScalingHealthMod)
                    Utils.modifyAttr(living, Attributes.ATTACK_DAMAGE, Config.CommonConfig.damageIncrease * 0.008, Config.CommonConfig.damageMax, difficulty, true);
                if (Config.CommonConfig.speedIncrease != 0)
                    Utils.modifyAttr(living, Attributes.MOVEMENT_SPEED, Config.CommonConfig.speedIncrease * 0.0008, Config.CommonConfig.speedMax, difficulty, false);
                if (Config.CommonConfig.knockbackIncrease != 0)
                    Utils.modifyAttr(living, Attributes.KNOCKBACK_RESISTANCE, Config.CommonConfig.knockbackIncrease * 0.002, Config.CommonConfig.knockbackMax, difficulty, false);
                if (Config.CommonConfig.magicResIncrease != 0)
                    EntityFlags.get(living).magicRes = Math.min(Config.CommonConfig.magicResIncrease * 0.0016f * difficulty, Config.CommonConfig.magicResMax);
                if (Config.CommonConfig.projectileIncrease != 0)
                    EntityFlags.get(living).projMult = 1 +
                            (Config.CommonConfig.projectileMax <= 0 ? Config.CommonConfig.projectileIncrease * 0.008f * difficulty : Math.min(Config.CommonConfig.projectileIncrease * 0.008f * difficulty, Config.CommonConfig.projectileMax - 1));
                if (Config.CommonConfig.explosionIncrease != 0)
                    EntityFlags.get(living).explosionMult = 1 +
                            (Config.CommonConfig.explosionMax <= 0 ? Config.CommonConfig.explosionIncrease * 0.003f * difficulty : Math.min(Config.CommonConfig.explosionIncrease * 0.003f * difficulty, Config.CommonConfig.explosionMax - 1));
            }
            flags.modifyAttributes = true;
        }

        if(Config.CommonConfig.varySizebyPehkui)
        {
            if(!flags.isVariedSize&&living instanceof Monster)
            Utils.modifyScale(living,Config.CommonConfig.sizeMin,Config.CommonConfig.sizeMax);
            flags.isVariedSize=true;
        }

    }
    public static float hurtEvent(LivingEntity entity, DamageSource source, float dmg) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() instanceof Monster)
            return dmg * (EntityFlags.get(source.getEntity()).projMult);
        if (source.is(DamageTypeTags.IS_EXPLOSION) && source.getEntity() instanceof Monster)
            return dmg * (EntityFlags.get(source.getEntity()).explosionMult);
        if (entity instanceof Monster) {
            if (source.is(DamageTypeTags.WITCH_RESISTANT_TO))
                return dmg * (1 - EntityFlags.get(entity).magicRes);
        }
        return dmg;
    }

    public static boolean onAttackEvent(LivingEntity target, DamageSource damagesource) {
        if (!target.level().isClientSide) {
            if (!Config.CommonConfig.friendlyFire && target instanceof TamableAnimal) {
                TamableAnimal pet = (TamableAnimal) target;
                if (damagesource.getEntity() != null && damagesource.getEntity() == pet.getOwner() && !damagesource.getEntity().isShiftKeyDown()) {
                    return true;
                }
            }
            Entity source = damagesource.getEntity();
            if (target instanceof Player) {
                Entity direct = damagesource.getDirectEntity();
                EntityFlags flag;
                if (direct instanceof Snowball && (flag = EntityFlags.get(direct)).isThrownEntity) {
                    flag.isThrownEntity = false;
                    target.hurt(damagesource, 0.001f);
                }
            } else if (source instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) source;
                if (CrossPlatformStuff.INSTANCE.canDisableShield(attacker.getMainHandItem(), target.getUseItem(), target, attacker)) {
                    triggerDisableShield(attacker, target);
                }
            }
        }
        return false;
    }

    private static void triggerDisableShield(LivingEntity attacker, LivingEntity target) {
        float f = 0.25F + EnchantmentHelper.getBlockEfficiency(attacker) * 0.05F;
        if (attacker.isSprinting()) {
            f += 0.75F;
        }
        if (attacker.getRandom().nextFloat() < f) {
            EntityFlags.get(target).disableShield();
            target.stopUsingItem();
            target.level().broadcastEntityEvent(target, (byte) 30);
        }
    }

    public static void openTile(Player player, BlockPos pos) {
        if (!player.level().isClientSide && !player.isShiftKeyDown()) {
            BlockEntity tile = player.level().getBlockEntity(pos);
            if (tile != null) {
                CrossPlatformStuff.INSTANCE.onPlayerOpen(tile);
            }
        }
    }

    public static boolean equipPet(Player player, InteractionHand hand, Entity target) {
        if (hand == InteractionHand.MAIN_HAND && target instanceof Mob mob && (mob instanceof OwnableEntity || mob.getType().is(ImprovedMobs.ARMOR_EQUIPPABLE)) && !target.level().isClientSide && player.isShiftKeyDown()
                && !Utils.isInList(target, Config.CommonConfig.petArmorBlackList, Config.CommonConfig.petWhiteList, Utils.entityID)) {
            if (!(mob instanceof OwnableEntity pet) || player == pet.getOwner()) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof ArmorItem armor) {
                    EquipmentSlot type = armor.getEquipmentSlot();
                    switch (type) {
                        case HEAD -> equipPetItem(player, mob, heldItem, EquipmentSlot.HEAD);
                        case CHEST -> equipPetItem(player, mob, heldItem, EquipmentSlot.CHEST);
                        case LEGS -> equipPetItem(player, mob, heldItem, EquipmentSlot.LEGS);
                        case FEET -> equipPetItem(player, mob, heldItem, EquipmentSlot.FEET);
                        default -> {
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static void equipPetItem(Player player, Mob living, ItemStack stack, EquipmentSlot slot) {
        ItemStack current = living.getItemBySlot(slot);
        if (!current.isEmpty() && !player.isCreative()) {
            ItemEntity entityitem = new ItemEntity(living.level(), living.getX(), living.getY(), living.getZ(), current);
            entityitem.setNoPickUpDelay();
            living.level().addFreshEntity(entityitem);
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        living.setItemSlot(slot, copy);
        if (!player.isCreative())
            stack.shrink(1);
    }

    public static boolean projectileImpact(Projectile projectile, HitResult hitResult) {
        if (EntityFlags.get(projectile).isThrownEntity) {
            Entity thrower = (projectile).getOwner();
            if (thrower instanceof Mob) {
                if (!(projectile instanceof ThrownPotion) && hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult res = (EntityHitResult) hitResult;
                    if (!res.getEntity().equals(((Mob) thrower).getTarget()))
                        return true;
                }
            }
        }
        return false;
    }

    public static void explosion(Explosion explosion, Entity source, List<Entity> affectedEntities) {
        if (source instanceof PrimedTnt && EntityFlags.get(source).isThrownEntity) {
            explosion.getToBlow().clear();
            LivingEntity igniter = explosion.getIndirectSourceEntity();
            if (igniter instanceof Mob) {
                affectedEntities.removeIf(e -> !e.equals(((Mob) igniter).getTarget()));
            }
        }
    }
}
