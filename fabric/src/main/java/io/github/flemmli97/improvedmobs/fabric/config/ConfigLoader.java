package io.github.flemmli97.improvedmobs.fabric.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.tenshilib.api.config.ItemWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;

import java.util.List;

public class ConfigLoader {

    public static InteractionResult loadClient() {
        ConfigSpecs.ClientConfigVals specs = ConfigSpecs.clientConfigSpecs;
        Config.ClientConfig.guiX = specs.guiX.get();
        Config.ClientConfig.guiY = specs.guiY.get();
        Config.ClientConfig.color = specs.color.get();
        Config.ClientConfig.scale = specs.scale.get().floatValue();
        Config.ClientConfig.showDifficulty = specs.showDifficulty.get();
        Config.ClientConfig.location = specs.location.get();
        return InteractionResult.CONSUME;
    }

    public static InteractionResult loadCommon() {
        ConfigSpecs.CommonConfigVals specs = ConfigSpecs.commonConfigSpecs;
        Config.CommonConfig.enableDifficultyScaling = specs.enableDifficultyScaling.get();
        Config.CommonConfig.difficultyDelay = specs.difficultyDelay.get();
        Config.CommonConfig.ignoreSpawner = specs.ignoreSpawner.get();
        Config.CommonConfig.ignorePlayers = specs.ignorePlayers.get();
        Config.CommonConfig.shouldPunishTimeSkip = specs.shouldPunishTimeSkip.get();
        Config.CommonConfig.friendlyFire = specs.friendlyFire.get();
        Config.CommonConfig.petArmorBlackList = specs.petArmorBlackList.get();
        Config.CommonConfig.petWhiteList = specs.petWhiteList.get();
        Config.CommonConfig.doIMDifficulty = specs.doIMDifficulty.get();
        Config.CommonConfig.increaseHandler.readFromString(specs.increaseHandler.get());
        Config.CommonConfig.difficultyType = specs.difficultyType.get();
        Config.CommonConfig.centerPos.readFromString(specs.centerPos.get());

        List<? extends String> l = specs.entityBlacklist.get();
        if (l.size() != 1 || !l.get(0).equals("UNINIT"))
            Config.CommonConfig.entityBlacklist.readFromString(specs.entityBlacklist.get());
        Config.CommonConfig.flagBlacklist = specs.flagBlacklist.get();
        Config.CommonConfig.mobAttributeWhitelist = specs.mobAttributeWhitelist.get();
        Config.CommonConfig.armorMobWhitelist = specs.armorMobWhitelist.get();
        Config.CommonConfig.heldMobWhitelist = specs.heldMobWhitelist.get();
        Config.CommonConfig.mobListBreakWhitelist = specs.mobListBreakWhitelist.get();
        Config.CommonConfig.mobListUseWhitelist = specs.mobListUseWhitelist.get();
        Config.CommonConfig.mobListLadderWhitelist = specs.mobListLadderWhitelist.get();
        Config.CommonConfig.mobListStealWhitelist = specs.mobListStealWhitelist.get();
        Config.CommonConfig.mobListBoatWhitelist = specs.mobListBoatWhitelist.get();
        Config.CommonConfig.mobListFlyWhitelist = specs.mobListFlyWhitelist.get();
        Config.CommonConfig.targetVillagerWhitelist = specs.targetVillagerWhitelist.get();
        Config.CommonConfig.neutralAggroWhitelist = specs.neutralAggroWhitelist.get();

        Config.CommonConfig.useScalingHealthMod = FabricLoader.getInstance().isModLoaded("scalinghealth") ? specs.useScalingHealthMod.get() : Config.IntegrationType.OFF;
        Config.CommonConfig.usePlayerEXMod = FabricLoader.getInstance().isModLoaded("playerex") ? specs.usePlayerEXMod.get() : Config.IntegrationType.OFF;
        Config.CommonConfig.playerEXScale = specs.playerEXScale.get().floatValue();
        Config.CommonConfig.useLevelZMod = FabricLoader.getInstance().isModLoaded("levelz") ? specs.useLevelZMod.get() : Config.IntegrationType.OFF;
        Config.CommonConfig.levelZScale = specs.levelZScale.get().floatValue();
        Config.CommonConfig.varySizebyPehkui = specs.varySizebyPehkui.get() && FabricLoader.getInstance().isModLoaded("pehkui");
        Config.CommonConfig.sizeMax = specs.sizeMax.get().floatValue();
        Config.CommonConfig.sizeMin = specs.sizeMin.get().floatValue();

        Config.CommonConfig.breakableBlocks.readFromString(specs.breakableBlocks.get());
        Config.CommonConfig.breakingAsBlacklist = specs.breakingAsBlacklist.get();
        Config.CommonConfig.useBlockBreakSound = specs.useBlockBreakSound.get();
        Config.CommonConfig.breakerChance = specs.breakerChance.get().floatValue();
        Config.CommonConfig.breakerInitCooldown = specs.breakerInitCooldown.get();
        Config.CommonConfig.breakerCooldown = specs.breakerCooldown.get();
        Config.CommonConfig.ignoreHarvestLevel = specs.ignoreHarvestLevel.get();
        Config.CommonConfig.restoreDelay = specs.restoreDelay.get();
        Config.CommonConfig.idleBreak = specs.idleBreak.get();
        Config.CommonConfig.stealerChance = specs.stealerChance.get().floatValue();
        Config.CommonConfig.blackListedContainerBlocks = specs.blackListedContainerBlocks.get();
        Config.CommonConfig.breakTileEntities = specs.breakTileEntities.get();
        Config.CommonConfig.breakingItem.clear();
        for (String s : specs.breakingItems.get()) {
            s = s.replace(" ", "");
            String[] sub = s.split(";");
            if (sub.length != 2) {
                ImprovedMobs.logger.error("Faulty entry for breaking item {}", s);
                continue;
            }
            try {
                Config.CommonConfig.breakingItem.add(new Config.WeightedItem(new ItemWrapper(sub[0]), Integer.parseInt(sub[1])));
            } catch (NumberFormatException e) {
                ImprovedMobs.logger.error("Faulty entry for breaking item {}", s);
            }
        }
        Config.CommonConfig.neutralAggressiv = specs.neutralAggressiv.get().floatValue();
        Config.CommonConfig.autoTargets.readFromString(specs.autoTargets.get());
        Config.CommonConfig.difficultyBreak = specs.difficultyBreak.get().floatValue();
        Config.CommonConfig.difficultySteal = specs.difficultySteal.get().floatValue();
        Config.CommonConfig.guardianAIChance = specs.guardianAIChance.get().floatValue();
        Config.CommonConfig.flyAIChance = specs.flyAIChance.get().floatValue();
        Config.CommonConfig.tntBlockDestruction = specs.tntBlockDestruction.get();

        Config.CommonConfig.equipmentModBlacklist = specs.equipmentModBlacklist.get();
        Config.CommonConfig.equipmentModWhitelist = specs.equipmentModWhitelist.get();
        Config.CommonConfig.itemuseBlacklist = specs.itemuseBlacklist.get();
        Config.CommonConfig.itemuseWhitelist = specs.itemuseWhitelist.get();
        Config.CommonConfig.entityItemConfig.readFromString(specs.entityItemConfig.get());
        Config.CommonConfig.baseEquipChance = specs.baseEquipChance.get().floatValue();
        Config.CommonConfig.baseEquipChanceAdd = specs.baseEquipChanceAdd.get().floatValue();
        Config.CommonConfig.diffEquipAdd = specs.diffEquipAdd.get().floatValue();
        Config.CommonConfig.baseWeaponChance = specs.baseWeaponChance.get().floatValue();
        Config.CommonConfig.diffWeaponChance = specs.diffWeaponChance.get().floatValue();
        Config.CommonConfig.baseEnchantChance = specs.baseEnchantChance.get().floatValue();
        Config.CommonConfig.diffEnchantAdd = specs.diffEnchantAdd.get().floatValue();
        Config.CommonConfig.enchantCalc.readFromString(specs.enchantCalc.get());
        Config.CommonConfig.enchantBlacklist = specs.enchantBlacklist.get();
        Config.CommonConfig.enchantWhitelist = specs.enchantWhitelist.get();
        Config.CommonConfig.baseItemChance = specs.baseItemChance.get().floatValue();
        Config.CommonConfig.diffItemChanceAdd = specs.diffItemChanceAdd.get().floatValue();
        Config.CommonConfig.shouldDropEquip = specs.shouldDropEquip.get();

        Config.CommonConfig.healthIncrease = specs.healthIncrease.get();
        Config.CommonConfig.healthMax = specs.healthMax.get();
        Config.CommonConfig.roundHP = specs.roundHP.get();
        Config.CommonConfig.damageIncrease = specs.damageIncrease.get();
        Config.CommonConfig.damageMax = specs.damageMax.get();
        Config.CommonConfig.speedIncrease = specs.speedIncrease.get();
        Config.CommonConfig.speedMax = specs.speedMax.get();
        Config.CommonConfig.knockbackIncrease = specs.knockbackIncrease.get();
        Config.CommonConfig.knockbackMax = specs.knockbackMax.get();
        Config.CommonConfig.magicResIncrease = specs.magicResIncrease.get().floatValue();
        Config.CommonConfig.magicResMax = specs.magicResMax.get().floatValue();
        Config.CommonConfig.projectileIncrease = specs.projectileIncrease.get().floatValue();
        Config.CommonConfig.projectileMax = specs.projectileMax.get().floatValue();
        Config.CommonConfig.explosionIncrease = specs.explosionIncrease.get().floatValue();
        Config.CommonConfig.explosionMax = specs.explosionMax.get().floatValue();
        return InteractionResult.CONSUME;
    }

    public static void serverInit(ServerLevel world) {
        ConfigSpecs.CommonConfigVals vals = ConfigSpecs.commonConfigSpecs;
        List<? extends String> l = vals.entityBlacklist.get();
        if (l.size() == 1 && l.get(0).equals("UNINITIALIZED")) {
            Config.CommonConfig.entityBlacklist.initDefault(world);
            vals.entityBlacklist.set(Config.CommonConfig.entityBlacklist.writeToString());
            ConfigSpecs.commonConfig.save();
        }
    }
}
