package io.github.flemmli97.improvedmobs.forge.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;

import java.util.List;

public class ConfigLoader {

    public static void loadClient() {
        Config.ClientConfig.guiX = ConfigSpecs.CLIENT_CONF.guiX.get();
        Config.ClientConfig.guiY = ConfigSpecs.CLIENT_CONF.guiY.get();
        Config.ClientConfig.color = ConfigSpecs.CLIENT_CONF.color.get();
        Config.ClientConfig.scale = ConfigSpecs.CLIENT_CONF.scale.get().floatValue();
        Config.ClientConfig.showDifficulty = ConfigSpecs.CLIENT_CONF.showDifficulty.get();
        Config.ClientConfig.location = ConfigSpecs.CLIENT_CONF.location.get();
    }

    public static void loadCommon() {
        Config.CommonConfig.enableDifficultyScaling = ConfigSpecs.COMMON_CONF.enableDifficultyScaling.get();
        Config.CommonConfig.difficultyDelay = ConfigSpecs.COMMON_CONF.difficultyDelay.get();
        Config.CommonConfig.ignoreSpawner = ConfigSpecs.COMMON_CONF.ignoreSpawner.get();
        Config.CommonConfig.ignorePlayers = ConfigSpecs.COMMON_CONF.ignorePlayers.get();
        Config.CommonConfig.shouldPunishTimeSkip = ConfigSpecs.COMMON_CONF.shouldPunishTimeSkip.get();
        Config.CommonConfig.friendlyFire = ConfigSpecs.COMMON_CONF.friendlyFire.get();
        Config.CommonConfig.petArmorBlackList = ConfigSpecs.COMMON_CONF.petArmorBlackList.get();
        Config.CommonConfig.petWhiteList = ConfigSpecs.COMMON_CONF.petWhiteList.get();
        Config.CommonConfig.doIMDifficulty = ConfigSpecs.COMMON_CONF.doIMDifficulty.get();
        Config.CommonConfig.increaseHandler.readFromString(ConfigSpecs.COMMON_CONF.increaseHandler.get());
        Config.CommonConfig.difficultyType = ConfigSpecs.COMMON_CONF.difficultyType.get();
        Config.CommonConfig.centerPos.readFromString(ConfigSpecs.COMMON_CONF.centerPos.get());

        List<? extends String> l = ConfigSpecs.COMMON_CONF.entityBlacklist.get();
        if (l.size() != 1 || !l.get(0).equals("UNINIT"))
            Config.CommonConfig.entityBlacklist.readFromString(ConfigSpecs.COMMON_CONF.entityBlacklist.get());
        Config.CommonConfig.flagBlacklist = ConfigSpecs.COMMON_CONF.flagBlacklist.get();
        Config.CommonConfig.mobAttributeWhitelist = ConfigSpecs.COMMON_CONF.mobAttributeWhitelist.get();
        Config.CommonConfig.armorMobWhitelist = ConfigSpecs.COMMON_CONF.armorMobWhitelist.get();
        Config.CommonConfig.heldMobWhitelist = ConfigSpecs.COMMON_CONF.heldMobWhitelist.get();
        Config.CommonConfig.mobListBreakWhitelist = ConfigSpecs.COMMON_CONF.mobListBreakWhitelist.get();
        Config.CommonConfig.mobListUseWhitelist = ConfigSpecs.COMMON_CONF.mobListUseWhitelist.get();
        Config.CommonConfig.mobListLadderWhitelist = ConfigSpecs.COMMON_CONF.mobListLadderWhitelist.get();
        Config.CommonConfig.mobListStealWhitelist = ConfigSpecs.COMMON_CONF.mobListStealWhitelist.get();
        Config.CommonConfig.mobListBoatWhitelist = ConfigSpecs.COMMON_CONF.mobListBoatWhitelist.get();
        Config.CommonConfig.mobListFlyWhitelist = ConfigSpecs.COMMON_CONF.mobListFlyWhitelist.get();
        Config.CommonConfig.targetVillagerWhitelist = ConfigSpecs.COMMON_CONF.targetVillagerWhitelist.get();
        Config.CommonConfig.neutralAggroWhitelist = ConfigSpecs.COMMON_CONF.neutralAggroWhitelist.get();

        Config.CommonConfig.useScalingHealthMod = ConfigSpecs.COMMON_CONF.useScalingHealthMod.get() && ModList.get().isLoaded("scalinghealth");
        Config.CommonConfig.usePlayerEXMod = ConfigSpecs.COMMON_CONF.usePlayerEXMod.get() && ModList.get().isLoaded("playerex");
        Config.CommonConfig.useLevelZMod = ConfigSpecs.COMMON_CONF.useLevelZMod.get() && ModList.get().isLoaded("levelz");
        Config.CommonConfig.varySizebyPehkui = ConfigSpecs.COMMON_CONF.varySizebyPehkui.get() && ModList.get().isLoaded("pehkui");
        Config.CommonConfig.sizeMax = ConfigSpecs.COMMON_CONF.sizeMax.get().floatValue();
        Config.CommonConfig.sizeMin = ConfigSpecs.COMMON_CONF.sizeMin.get().floatValue();

        Config.CommonConfig.breakableBlocks.readFromString(ConfigSpecs.COMMON_CONF.breakableBlocks.get());
        Config.CommonConfig.breakingAsBlacklist = ConfigSpecs.COMMON_CONF.breakingAsBlacklist.get();
        Config.CommonConfig.useBlockBreakSound = ConfigSpecs.COMMON_CONF.useBlockBreakSound.get();
        Config.CommonConfig.breakerChance = ConfigSpecs.COMMON_CONF.breakerChance.get().floatValue();
        Config.CommonConfig.breakerInitCooldown = ConfigSpecs.COMMON_CONF.breakerInitCooldown.get();
        Config.CommonConfig.breakerCooldown = ConfigSpecs.COMMON_CONF.breakerCooldown.get();
        Config.CommonConfig.restoreDelay = ConfigSpecs.COMMON_CONF.restoreDelay.get();
        Config.CommonConfig.idleBreak = ConfigSpecs.COMMON_CONF.idleBreak.get();
        Config.CommonConfig.stealerChance = ConfigSpecs.COMMON_CONF.stealerChance.get().floatValue();
        Config.CommonConfig.blackListedContainerBlocks = ConfigSpecs.COMMON_CONF.blackListedContainerBlocks.get();
        Config.CommonConfig.breakTileEntities = ConfigSpecs.COMMON_CONF.breakTileEntities.get();

        Config.CommonConfig.breakingItem.clear();
        for (String s : ConfigSpecs.COMMON_CONF.breakingItems.get()) {
            s = s.replace(" ", "");
            String[] sub = s.split(";");
            if (sub.length != 2) {
                ImprovedMobs.LOGGER.error("Faulty entry for breaking item {}", s);
                continue;
            }
            try {
                Config.CommonConfig.breakingItem.add(new Config.WeightedItem(sub[0], Integer.parseInt(sub[1])));
            } catch (NumberFormatException e) {
                ImprovedMobs.LOGGER.error("Faulty entry for breaking item {}", s);
            }
        }
        Config.CommonConfig.neutralAggressiv = ConfigSpecs.COMMON_CONF.neutralAggressiv.get().floatValue();
        Config.CommonConfig.autoTargets.readFromString(ConfigSpecs.COMMON_CONF.autoTargets.get());
        Config.CommonConfig.difficultyBreak = ConfigSpecs.COMMON_CONF.difficultyBreak.get().floatValue();
        Config.CommonConfig.difficultySteal = ConfigSpecs.COMMON_CONF.difficultySteal.get().floatValue();
        Config.CommonConfig.guardianAIChance = ConfigSpecs.COMMON_CONF.guardianAIChance.get().floatValue();
        Config.CommonConfig.flyAIChance = ConfigSpecs.COMMON_CONF.flyAIChance.get().floatValue();

        Config.CommonConfig.equipmentModBlacklist = ConfigSpecs.COMMON_CONF.equipmentModBlacklist.get();
        Config.CommonConfig.equipmentModWhitelist = ConfigSpecs.COMMON_CONF.equipmentModWhitelist.get();
        Config.CommonConfig.itemuseBlacklist = ConfigSpecs.COMMON_CONF.itemuseBlacklist.get();
        Config.CommonConfig.itemuseWhitelist = ConfigSpecs.COMMON_CONF.itemuseWhitelist.get();
        Config.CommonConfig.entityItemConfig.readFromString(ConfigSpecs.COMMON_CONF.entityItemConfig.get());
        Config.CommonConfig.baseEquipChance = ConfigSpecs.COMMON_CONF.baseEquipChance.get().floatValue();
        Config.CommonConfig.baseEquipChanceAdd = ConfigSpecs.COMMON_CONF.baseEquipChanceAdd.get().floatValue();
        Config.CommonConfig.diffEquipAdd = ConfigSpecs.COMMON_CONF.diffEquipAdd.get().floatValue();
        Config.CommonConfig.baseWeaponChance = ConfigSpecs.COMMON_CONF.baseWeaponChance.get().floatValue();
        Config.CommonConfig.diffWeaponChance = ConfigSpecs.COMMON_CONF.diffWeaponChance.get().floatValue();
        Config.CommonConfig.baseEnchantChance = ConfigSpecs.COMMON_CONF.baseEnchantChance.get().floatValue();
        Config.CommonConfig.diffEnchantAdd = ConfigSpecs.COMMON_CONF.diffEnchantAdd.get().floatValue();
        Config.CommonConfig.enchantCalc.readFromString(ConfigSpecs.COMMON_CONF.enchantCalc.get());
        Config.CommonConfig.enchantBlacklist = ConfigSpecs.COMMON_CONF.enchantBlacklist.get();
        Config.CommonConfig.enchantWhitelist = ConfigSpecs.COMMON_CONF.enchantWhitelist.get();
        Config.CommonConfig.baseItemChance = ConfigSpecs.COMMON_CONF.baseItemChance.get().floatValue();
        Config.CommonConfig.diffItemChanceAdd = ConfigSpecs.COMMON_CONF.diffItemChanceAdd.get().floatValue();
        Config.CommonConfig.shouldDropEquip = ConfigSpecs.COMMON_CONF.shouldDropEquip.get();

        Config.CommonConfig.healthIncrease = ConfigSpecs.COMMON_CONF.healthIncrease.get();
        Config.CommonConfig.healthMax = ConfigSpecs.COMMON_CONF.healthMax.get();
        Config.CommonConfig.roundHP = ConfigSpecs.COMMON_CONF.roundHP.get();
        Config.CommonConfig.damageIncrease = ConfigSpecs.COMMON_CONF.damageIncrease.get();
        Config.CommonConfig.damageMax = ConfigSpecs.COMMON_CONF.damageMax.get();
        Config.CommonConfig.speedIncrease = ConfigSpecs.COMMON_CONF.speedIncrease.get();
        Config.CommonConfig.speedMax = ConfigSpecs.COMMON_CONF.speedMax.get();
        Config.CommonConfig.knockbackIncrease = ConfigSpecs.COMMON_CONF.knockbackIncrease.get();
        Config.CommonConfig.knockbackMax = ConfigSpecs.COMMON_CONF.knockbackMax.get();
        Config.CommonConfig.magicResIncrease = ConfigSpecs.COMMON_CONF.magicResIncrease.get().floatValue();
        Config.CommonConfig.magicResMax = ConfigSpecs.COMMON_CONF.magicResMax.get().floatValue();
        Config.CommonConfig.projectileIncrease = ConfigSpecs.COMMON_CONF.projectileIncrease.get().floatValue();
        Config.CommonConfig.projectileMax = ConfigSpecs.COMMON_CONF.projectileMax.get().floatValue();
        Config.CommonConfig.explosionIncrease = ConfigSpecs.COMMON_CONF.explosionIncrease.get().floatValue();
        Config.CommonConfig.explosionMax = ConfigSpecs.COMMON_CONF.explosionMax.get().floatValue();
    }

    public static void serverInit(ServerLevel world) {
        List<? extends String> l = ConfigSpecs.COMMON_CONF.entityBlacklist.get();
        if (l.size() == 1 && l.get(0).equals("UNINITIALIZED")) {
            Config.CommonConfig.entityBlacklist.initDefault(world);
            ConfigSpecs.COMMON_CONF.entityBlacklist.set(Config.CommonConfig.entityBlacklist.writeToString());
        }
    }
}
