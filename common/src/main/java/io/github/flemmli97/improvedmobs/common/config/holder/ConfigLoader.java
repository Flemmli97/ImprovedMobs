package io.github.flemmli97.improvedmobs.common.config.holder;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.tenshilib.loader.TenshiLibCrossPlat;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;
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
        Config.CommonConfig.difficultyIncrease.read(ConfigSpecs.COMMON_CONF.difficultyIncrease.get());
        Config.CommonConfig.ignorePlayers = ConfigSpecs.COMMON_CONF.ignorePlayers.get();
        Config.CommonConfig.considerTimeskip = ConfigSpecs.COMMON_CONF.considerTimeskip.get();
        Config.CommonConfig.difficultyType = ConfigSpecs.COMMON_CONF.difficultyType.get();
        Config.CommonConfig.centerPos.read(ConfigSpecs.COMMON_CONF.centerPos.get());
        Config.CommonConfig.friendlyFire = ConfigSpecs.COMMON_CONF.friendlyFire.get();
        Config.CommonConfig.petArmorBlackList = ConfigSpecs.COMMON_CONF.petArmorBlackList.get();
        Config.CommonConfig.petWhiteList = ConfigSpecs.COMMON_CONF.petWhiteList.get();

        List<DifficultyFeatures> featureList = ConfigSpecs.COMMON_CONF.featureBlacklist.get()
                .stream().map(DifficultyFeatures::valueOf).toList();
        Config.CommonConfig.featureBlacklist = featureList.isEmpty() ? EnumSet.noneOf(DifficultyFeatures.class) : EnumSet.copyOf(featureList);
        List<String> blacklist = ConfigSpecs.COMMON_CONF.entityBlacklist.get();
        if (blacklist.size() != 1 || !blacklist.getFirst().equals("UNINITIALIZED"))
            Config.CommonConfig.entityBlacklist.read(ConfigSpecs.COMMON_CONF.entityBlacklist.get());
        featureList = ConfigSpecs.COMMON_CONF.featureWhitelist.get()
                .stream().map(DifficultyFeatures::valueOf).toList();
        Config.CommonConfig.featureWhitelist = featureList.isEmpty() ? EnumSet.noneOf(DifficultyFeatures.class) : EnumSet.copyOf(featureList);

        Config.CommonConfig.vanillaClamped = ConfigSpecs.COMMON_CONF.vanillaClamped.get();
        Config.CommonConfig.vanillaClampedMax = ConfigSpecs.COMMON_CONF.vanillaClampedMax.get().floatValue();
        Config.CommonConfig.useScalingHealthMod = TenshiLibCrossPlat.INSTANCE.isModLoaded("scalinghealth") ? ConfigSpecs.COMMON_CONF.useScalingHealthMod.get() : Config.IntegrationType.OFF;
        Config.CommonConfig.usePlayerEXMod = TenshiLibCrossPlat.INSTANCE.isModLoaded("playerex") ? ConfigSpecs.COMMON_CONF.usePlayerEXMod.get() : Config.IntegrationType.OFF;
        Config.CommonConfig.playerEXScale = ConfigSpecs.COMMON_CONF.playerEXScale.get().floatValue();
        Config.CommonConfig.useLevelZMod = TenshiLibCrossPlat.INSTANCE.isModLoaded("levelz") ? ConfigSpecs.COMMON_CONF.useLevelZMod.get() : Config.IntegrationType.OFF;
        Config.CommonConfig.levelZScale = ConfigSpecs.COMMON_CONF.levelZScale.get().floatValue();

        Config.CommonConfig.breakableBlocks.read(ConfigSpecs.COMMON_CONF.breakableBlocks.get());
        Config.CommonConfig.breakingAsBlacklist = ConfigSpecs.COMMON_CONF.breakingAsBlacklist.get();
        Config.CommonConfig.useBlockBreakSound = ConfigSpecs.COMMON_CONF.useBlockBreakSound.get();
        Config.CommonConfig.breakerChance.read(ConfigSpecs.COMMON_CONF.breakerChance.get());
        Config.CommonConfig.difficultyBreak = ConfigSpecs.COMMON_CONF.difficultyBreak.get().floatValue();
        Config.CommonConfig.breakerInitCooldown = ConfigSpecs.COMMON_CONF.breakerInitCooldown.get();
        Config.CommonConfig.breakerCooldown = ConfigSpecs.COMMON_CONF.breakerCooldown.get();
        Config.CommonConfig.ignoreHarvestLevel = ConfigSpecs.COMMON_CONF.ignoreHarvestLevel.get();
        Config.CommonConfig.restoreDelay = ConfigSpecs.COMMON_CONF.restoreDelay.get();
        Config.CommonConfig.idleBreak = ConfigSpecs.COMMON_CONF.idleBreak.get();
        Config.CommonConfig.breakerSightIgnore = ConfigSpecs.COMMON_CONF.breakerSightIgnore.get().floatValue();
        Config.CommonConfig.breakSpeed.read(ConfigSpecs.COMMON_CONF.breakSpeed.get());
        Config.CommonConfig.stealerChance.read(ConfigSpecs.COMMON_CONF.stealerChance.get());
        Config.CommonConfig.difficultySteal = ConfigSpecs.COMMON_CONF.difficultySteal.get().floatValue();
        Config.CommonConfig.blackListedContainerBlocks = ConfigSpecs.COMMON_CONF.blackListedContainerBlocks.get();
        Config.CommonConfig.breakBlockEntities = ConfigSpecs.COMMON_CONF.breakBlockEntities.get();
        Config.CommonConfig.breakingItem.clear();
        for (String s : ConfigSpecs.COMMON_CONF.breakingItems.get()) {
            s = s.replace(" ", "");
            String[] sub = s.split(";");
            try {
                Config.CommonConfig.breakingItem.add(new Config.WeightedItem(sub[0], Integer.parseInt(sub[1])));
            } catch (Exception e) {
                ImprovedMobs.LOGGER.error("Faulty entry for breaking item {}", s, e);
            }
        }
        Config.CommonConfig.neutralAggressiv.read(ConfigSpecs.COMMON_CONF.neutralAggressiv.get());
        Config.CommonConfig.guardianAIChance.read(ConfigSpecs.COMMON_CONF.guardianAIChance.get());
        Config.CommonConfig.flyAIChance.read(ConfigSpecs.COMMON_CONF.flyAIChance.get());
        Config.CommonConfig.tntBlockDestruction = ConfigSpecs.COMMON_CONF.tntBlockDestruction.get();
        Config.CommonConfig.ignoreSightChance.read(ConfigSpecs.COMMON_CONF.ignoreSightChance.get());
        Config.CommonConfig.autoTargets.read(ConfigSpecs.COMMON_CONF.autoTargets.get());

        Config.CommonConfig.equipmentModBlacklist = ConfigSpecs.COMMON_CONF.equipmentModBlacklist.get();
        Config.CommonConfig.equipmentModWhitelist = ConfigSpecs.COMMON_CONF.equipmentModWhitelist.get();
        Config.CommonConfig.itemuseBlacklist = ConfigSpecs.COMMON_CONF.itemuseBlacklist.get();
        Config.CommonConfig.itemuseWhitelist = ConfigSpecs.COMMON_CONF.itemuseWhitelist.get();
        Config.CommonConfig.entityItemConfig.read(ConfigSpecs.COMMON_CONF.entityItemConfig.get());

        Config.CommonConfig.equipmentChance.read(ConfigSpecs.COMMON_CONF.equipmentChance.get());
        Config.CommonConfig.additionalEquipmentChance.read(ConfigSpecs.COMMON_CONF.additionalEquipmentChance.get());
        Config.CommonConfig.randomTrimChance.read(ConfigSpecs.COMMON_CONF.randomTrimChance.get());
        Config.CommonConfig.mainHandChance.read(ConfigSpecs.COMMON_CONF.mainHandChance.get());
        Config.CommonConfig.offHandChance.read(ConfigSpecs.COMMON_CONF.offHandChance.get());
        Config.CommonConfig.dropChance.read(ConfigSpecs.COMMON_CONF.dropChance.get());
        Config.CommonConfig.enchantChance.read(ConfigSpecs.COMMON_CONF.enchantChance.get());
        Config.CommonConfig.enchantCalc.read(ConfigSpecs.COMMON_CONF.enchantCalc.get());
        Config.CommonConfig.enchantBlacklist = ConfigSpecs.COMMON_CONF.enchantBlacklist.get();
        Config.CommonConfig.enchantWhitelist = ConfigSpecs.COMMON_CONF.enchantWhitelist.get();
    }

    public static void serverInit(ServerLevel world) {
        List<? extends String> l = ConfigSpecs.COMMON_CONF.entityBlacklist.get();
        if (l.size() == 1 && l.getFirst().equals("UNINITIALIZED")) {
            Config.CommonConfig.entityBlacklist.initDefault(world);
            ConfigSpecs.COMMON_CONF.entityBlacklist.set(Config.CommonConfig.entityBlacklist.write());
            ConfigSpecs.COMMON_CONF.entityBlacklist.save();
        }
    }
}
