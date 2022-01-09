package io.github.flemmli97.improvedmobs.forge.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.tenshilib.api.config.ItemWrapper;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class ConfigLoader {

    public static void loadClient() {
        Config.ClientConfig.guiX = ConfigSpecs.clientConf.guiX.get();
        Config.ClientConfig.guiY = ConfigSpecs.clientConf.guiY.get();
        Config.ClientConfig.color = ConfigSpecs.clientConf.color.get();
        Config.ClientConfig.scale = ConfigSpecs.clientConf.scale.get().floatValue();
        Config.ClientConfig.showDifficulty = ConfigSpecs.clientConf.showDifficulty.get();
    }

    public static void loadCommon() {
        Config.CommonConfig.enableDifficultyScaling = ConfigSpecs.commonConf.enableDifficultyScaling.get();
        Config.CommonConfig.difficultyDelay = ConfigSpecs.commonConf.difficultyDelay.get();
        Config.CommonConfig.ignorePlayers = ConfigSpecs.commonConf.ignorePlayers.get();
        Config.CommonConfig.shouldPunishTimeSkip = ConfigSpecs.commonConf.shouldPunishTimeSkip.get();
        Config.CommonConfig.friendlyFire = ConfigSpecs.commonConf.friendlyFire.get();
        Config.CommonConfig.petArmorBlackList = ConfigSpecs.commonConf.petArmorBlackList.get();
        Config.CommonConfig.petWhiteList = ConfigSpecs.commonConf.petWhiteList.get();
        Config.CommonConfig.doIMDifficulty = ConfigSpecs.commonConf.doIMDifficulty.get();
        Config.CommonConfig.increaseHandler.readFromString(ConfigSpecs.commonConf.increaseHandler.get());

        List<? extends String> l = ConfigSpecs.commonConf.entityBlacklist.get();
        if (l.size() != 1 || !l.get(0).equals("UNINIT"))
            Config.CommonConfig.entityBlacklist.readFromString(ConfigSpecs.commonConf.entityBlacklist.get());
        Config.CommonConfig.flagBlacklist = ConfigSpecs.commonConf.flagBlacklist.get();
        Config.CommonConfig.mobAttributeWhitelist = ConfigSpecs.commonConf.mobAttributeWhitelist.get();
        Config.CommonConfig.armorMobWhitelist = ConfigSpecs.commonConf.armorMobWhitelist.get();
        Config.CommonConfig.heldMobWhitelist = ConfigSpecs.commonConf.heldMobWhitelist.get();
        Config.CommonConfig.mobListBreakWhitelist = ConfigSpecs.commonConf.mobListBreakWhitelist.get();
        Config.CommonConfig.mobListUseWhitelist = ConfigSpecs.commonConf.mobListUseWhitelist.get();
        Config.CommonConfig.mobListLadderWhitelist = ConfigSpecs.commonConf.mobListLadderWhitelist.get();
        Config.CommonConfig.mobListStealWhitelist = ConfigSpecs.commonConf.mobListStealWhitelist.get();
        Config.CommonConfig.mobListBoatWhitelist = ConfigSpecs.commonConf.mobListBoatWhitelist.get();
        Config.CommonConfig.mobListFlyWhitelist = ConfigSpecs.commonConf.mobListFlyWhitelist.get();
        Config.CommonConfig.targetVillagerWhitelist = ConfigSpecs.commonConf.targetVillagerWhitelist.get();

        Config.CommonConfig.debugPath = ConfigSpecs.commonConf.debugPath.get();

        Config.CommonConfig.useScalingHealthMod = ConfigSpecs.commonConf.useScalingHealthMod.get() && ModList.get().isLoaded("scalinghealth");

        Config.CommonConfig.breakableBlocks.readFromString(ConfigSpecs.commonConf.breakableBlocks.get());
        Config.CommonConfig.breakingAsBlacklist = ConfigSpecs.commonConf.breakingAsBlacklist.get();
        Config.CommonConfig.useBlockBreakSound = ConfigSpecs.commonConf.useBlockBreakSound.get();
        Config.CommonConfig.breakerChance = ConfigSpecs.commonConf.breakerChance.get().floatValue();
        Config.CommonConfig.breakerInitCooldown = ConfigSpecs.commonConf.breakerInitCooldown.get();
        Config.CommonConfig.breakerCooldown = ConfigSpecs.commonConf.breakerCooldown.get();
        Config.CommonConfig.stealerChance = ConfigSpecs.commonConf.stealerChance.get().floatValue();
        Config.CommonConfig.breakTileEntities = ConfigSpecs.commonConf.breakTileEntities.get();

        Config.CommonConfig.breakingItem.clear();
        for (String s : ConfigSpecs.commonConf.breakingItems.get()) {
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
        Config.CommonConfig.neutralAggressiv = ConfigSpecs.commonConf.neutralAggressiv.get().floatValue();
        Config.CommonConfig.autoTargets.readFromString(ConfigSpecs.commonConf.autoTargets.get());
        Config.CommonConfig.repairTick = ConfigSpecs.commonConf.repairTick.get();
        Config.CommonConfig.difficultyBreak = ConfigSpecs.commonConf.difficultyBreak.get().floatValue();
        Config.CommonConfig.difficultySteal = ConfigSpecs.commonConf.difficultySteal.get().floatValue();
        Config.CommonConfig.flyAIChance = ConfigSpecs.commonConf.flyAIChance.get().floatValue();

        Config.CommonConfig.equipmentModBlacklist = ConfigSpecs.commonConf.equipmentModBlacklist.get();
        Config.CommonConfig.equipmentModWhitelist = ConfigSpecs.commonConf.equipmentModWhitelist.get();
        Config.CommonConfig.itemuseBlacklist = ConfigSpecs.commonConf.itemuseBlacklist.get();
        Config.CommonConfig.itemuseWhitelist = ConfigSpecs.commonConf.itemuseWhitelist.get();
        Config.CommonConfig.entityItemConfig.readFromString(ConfigSpecs.commonConf.entityItemConfig.get());
        Config.CommonConfig.baseEquipChance = ConfigSpecs.commonConf.baseEquipChance.get().floatValue();
        Config.CommonConfig.baseEquipChanceAdd = ConfigSpecs.commonConf.baseEquipChanceAdd.get().floatValue();
        Config.CommonConfig.diffEquipAdd = ConfigSpecs.commonConf.diffEquipAdd.get().floatValue();
        Config.CommonConfig.baseWeaponChance = ConfigSpecs.commonConf.baseWeaponChance.get().floatValue();
        Config.CommonConfig.diffWeaponChance = ConfigSpecs.commonConf.diffWeaponChance.get().floatValue();
        Config.CommonConfig.baseEnchantChance = ConfigSpecs.commonConf.baseEnchantChance.get().floatValue();
        Config.CommonConfig.diffEnchantAdd = ConfigSpecs.commonConf.diffEnchantAdd.get().floatValue();
        Config.CommonConfig.enchantCalc.readFromString(ConfigSpecs.commonConf.enchantCalc.get());
        Config.CommonConfig.baseItemChance = ConfigSpecs.commonConf.baseItemChance.get().floatValue();
        Config.CommonConfig.diffItemChanceAdd = ConfigSpecs.commonConf.diffItemChanceAdd.get().floatValue();
        Config.CommonConfig.shouldDropEquip = ConfigSpecs.commonConf.shouldDropEquip.get();

        Config.CommonConfig.healthIncrease = ConfigSpecs.commonConf.healthIncrease.get();
        Config.CommonConfig.healthMax = ConfigSpecs.commonConf.healthMax.get();
        Config.CommonConfig.roundHP = ConfigSpecs.commonConf.roundHP.get();
        Config.CommonConfig.damageIncrease = ConfigSpecs.commonConf.damageIncrease.get();
        Config.CommonConfig.damageMax = ConfigSpecs.commonConf.damageMax.get();
        Config.CommonConfig.speedIncrease = ConfigSpecs.commonConf.speedIncrease.get();
        Config.CommonConfig.speedMax = ConfigSpecs.commonConf.speedMax.get();
        Config.CommonConfig.knockbackIncrease = ConfigSpecs.commonConf.knockbackIncrease.get();
        Config.CommonConfig.knockbackMax = ConfigSpecs.commonConf.knockbackMax.get();
        Config.CommonConfig.magicResIncrease = ConfigSpecs.commonConf.magicResIncrease.get().floatValue();
        Config.CommonConfig.magicResMax = ConfigSpecs.commonConf.magicResMax.get().floatValue();
        Config.CommonConfig.projectileIncrease = ConfigSpecs.commonConf.projectileIncrease.get().floatValue();
        Config.CommonConfig.projectileMax = ConfigSpecs.commonConf.projectileMax.get().floatValue();
    }

    public static void serverInit(ServerLevel world) {
        List<? extends String> l = ConfigSpecs.commonConf.entityBlacklist.get();
        if (l.size() == 1 && l.get(0).equals("UNINITIALIZED")) {
            Config.CommonConfig.entityBlacklist.initDefault(world);
            ConfigSpecs.commonConf.entityBlacklist.set(Config.CommonConfig.entityBlacklist.writeToString());
        }
    }
}
