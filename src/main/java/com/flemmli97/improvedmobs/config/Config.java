package com.flemmli97.improvedmobs.config;

import com.flemmli97.tenshilib.api.config.ItemWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class Config {

    public static class ClientConfig {

        public static int guiX;
        public static int guiY;
        public static TextFormatting color = TextFormatting.DARK_PURPLE;
        public static float scale = 1;

        public static void load() {
            guiX = ConfigSpecs.clientConf.guiX.get();
            guiY = ConfigSpecs.clientConf.guiY.get();
            color = ConfigSpecs.clientConf.color.get();
            scale = ConfigSpecs.clientConf.scale.get().floatValue();
        }
    }

    public static class CommonConfig {

        //General
        public static boolean enableDifficultyScaling;
        public static int difficultyDelay;
        public static boolean ignorePlayers;
        public static List<? extends String> mobListLight;
        public static boolean mobListLightBlackList;
        public static int light;
        public static boolean shouldPunishTimeSkip;
        public static boolean friendlyFire;
        public static List<? extends String> petArmorBlackList;
        public static boolean petWhiteList;

        public static boolean doIMDifficulty = true;

        //Black-WhiteList
        public static EntityModifyFlagConfig entityBlacklist = new EntityModifyFlagConfig();
        public static boolean mobAttributeWhitelist;
        public static boolean armorMobWhitelist;
        public static boolean heldMobWhitelist;
        public static boolean mobListBreakWhitelist;
        public static boolean mobListUseWhitelist;
        public static boolean mobListLadderWhitelist;
        public static boolean mobListStealWhitelist;
        public static boolean mobListBoatWhitelist;
        public static boolean targetVillagerWhitelist;

        //Debug
        public static boolean debugPath;

        //Integration
        public static boolean useScalingHealthMod = true;
        public static boolean useTGunsMod = true;
        public static boolean useReforgedMod = true;
        public static boolean useCoroUtil = true;

        //AI
        public static BreakableBlocks breakableBlocks = new BreakableBlocks();
        public static boolean breakingAsBlacklist;
        public static boolean useBlockBreakSound;
        public static float breakerChance = 1;
        public static float stealerChance = 1;
        public static boolean breakTileEntities;
        public static ItemWrapper breakingItem = new ItemWrapper("minecraft:diamond_pickaxe");
        public static float neutralAggressiv;
        public static MobClassMapConfig autoTargets = new MobClassMapConfig();
        public static int repairTick = 200;
        public static float difficultyBreak;
        public static float difficultySteal;

        //Equipment
        public static List<String> equipmentModBlacklist;
        public static boolean equipmentModWhitelist;
        public static float baseEquipChance;
        public static float baseEquipChanceAdd;
        public static float diffEquipAdd;
        public static float baseWeaponChance;
        public static float diffWeaponChance;
        public static float baseEnchantChance;
        public static float diffEnchantAdd;
        public static float baseItemChance;
        public static float diffItemChanceAdd;
        public static boolean shouldDropEquip;

        //Attributes
        public static double healthIncrease;
        public static double healthMax;
        public static double roundHP;
        public static double damageIncrease;
        public static double damageMax;
        public static double speedIncrease;
        public static double speedMax;
        public static double knockbackIncrease;
        public static double knockbackMax;
        public static float magicResIncrease;
        public static float magicResMax;
        public static float projectileIncrease;
        public static float projectileMax;

        public static void load() {
            enableDifficultyScaling = ConfigSpecs.commonConf.enableDifficultyScaling.get();
            difficultyDelay = ConfigSpecs.commonConf.difficultyDelay.get();
            ignorePlayers = ConfigSpecs.commonConf.ignorePlayers.get();
            mobListLight = ConfigSpecs.commonConf.mobListLight.get();
            mobListLightBlackList = ConfigSpecs.commonConf.mobListLightBlackList.get();
            light = ConfigSpecs.commonConf.light.get();
            shouldPunishTimeSkip = ConfigSpecs.commonConf.shouldPunishTimeSkip.get();
            friendlyFire = ConfigSpecs.commonConf.friendlyFire.get();
            petArmorBlackList = ConfigSpecs.commonConf.petArmorBlackList.get();
            petWhiteList = ConfigSpecs.commonConf.petWhiteList.get();

            List<? extends String> l = ConfigSpecs.commonConf.entityBlacklist.get();
            if (l.size() != 1 || !l.get(0).equals("UNINIT"))
                entityBlacklist.readFromString(ConfigSpecs.commonConf.entityBlacklist.get());
            mobAttributeWhitelist = ConfigSpecs.commonConf.mobAttributeWhitelist.get();
            armorMobWhitelist = ConfigSpecs.commonConf.armorMobWhitelist.get();
            heldMobWhitelist = ConfigSpecs.commonConf.heldMobWhitelist.get();
            mobListBreakWhitelist = ConfigSpecs.commonConf.mobListBreakWhitelist.get();
            mobListUseWhitelist = ConfigSpecs.commonConf.mobListUseWhitelist.get();
            mobListLadderWhitelist = ConfigSpecs.commonConf.mobListLadderWhitelist.get();
            mobListStealWhitelist = ConfigSpecs.commonConf.mobListStealWhitelist.get();
            mobListBoatWhitelist = ConfigSpecs.commonConf.mobListBoatWhitelist.get();
            targetVillagerWhitelist = ConfigSpecs.commonConf.targetVillagerWhitelist.get();

            debugPath = ConfigSpecs.commonConf.debugPath.get();

            useScalingHealthMod = ConfigSpecs.commonConf.useScalingHealthMod.get() && ModList.get().isLoaded("scalinghealth");
            useTGunsMod = ConfigSpecs.commonConf.useTGunsMod.get() && ModList.get().isLoaded("techguns");
            useReforgedMod = ConfigSpecs.commonConf.useReforgedMod.get() && ModList.get().isLoaded("reforged");
            useCoroUtil = ConfigSpecs.commonConf.useCoroUtil.get() && ModList.get().isLoaded("coroutil");

            breakableBlocks.readFromString(ConfigSpecs.commonConf.breakableBlocks.get());
            breakingAsBlacklist = ConfigSpecs.commonConf.breakingAsBlacklist.get();
            useBlockBreakSound = ConfigSpecs.commonConf.useBlockBreakSound.get();
            breakerChance = ConfigSpecs.commonConf.breakerChance.get().floatValue();
            stealerChance = ConfigSpecs.commonConf.stealerChance.get().floatValue();
            breakTileEntities = ConfigSpecs.commonConf.breakTileEntities.get();
            breakingItem.readFromString(ConfigSpecs.commonConf.breakingItem.get());
            neutralAggressiv = ConfigSpecs.commonConf.neutralAggressiv.get().floatValue();
            autoTargets.readFromString(ConfigSpecs.commonConf.autoTargets.get());
            repairTick = ConfigSpecs.commonConf.repairTick.get();
            difficultyBreak = ConfigSpecs.commonConf.difficultyBreak.get().floatValue();
            difficultySteal = ConfigSpecs.commonConf.difficultySteal.get().floatValue();

            equipmentModBlacklist = ConfigSpecs.commonConf.equipmentModBlacklist.get();
            equipmentModWhitelist = ConfigSpecs.commonConf.equipmentModWhitelist.get();
            baseEquipChance = ConfigSpecs.commonConf.baseEquipChance.get().floatValue();
            baseEquipChanceAdd = ConfigSpecs.commonConf.baseEquipChanceAdd.get().floatValue();
            diffEquipAdd = ConfigSpecs.commonConf.diffEquipAdd.get().floatValue();
            baseWeaponChance = ConfigSpecs.commonConf.baseWeaponChance.get().floatValue();
            diffWeaponChance = ConfigSpecs.commonConf.diffWeaponChance.get().floatValue();
            baseEnchantChance = ConfigSpecs.commonConf.baseEnchantChance.get().floatValue();
            diffEnchantAdd = ConfigSpecs.commonConf.diffEnchantAdd.get().floatValue();
            baseItemChance = ConfigSpecs.commonConf.baseItemChance.get().floatValue();
            diffItemChanceAdd = ConfigSpecs.commonConf.diffItemChanceAdd.get().floatValue();
            shouldDropEquip = ConfigSpecs.commonConf.shouldDropEquip.get();

            healthIncrease = ConfigSpecs.commonConf.healthIncrease.get();
            healthMax = ConfigSpecs.commonConf.healthMax.get();
            roundHP = ConfigSpecs.commonConf.roundHP.get();
            damageIncrease = ConfigSpecs.commonConf.damageIncrease.get();
            damageMax = ConfigSpecs.commonConf.damageMax.get();
            speedIncrease = ConfigSpecs.commonConf.speedIncrease.get();
            speedMax = ConfigSpecs.commonConf.speedMax.get();
            knockbackIncrease = ConfigSpecs.commonConf.knockbackIncrease.get();
            knockbackMax = ConfigSpecs.commonConf.knockbackMax.get();
            magicResIncrease = ConfigSpecs.commonConf.magicResIncrease.get().floatValue();
            magicResMax = ConfigSpecs.commonConf.magicResMax.get().floatValue();
            projectileIncrease = ConfigSpecs.commonConf.projectileIncrease.get().floatValue();
            projectileMax = ConfigSpecs.commonConf.projectileMax.get().floatValue();
        }

        public static void serverInit(MinecraftServer server) {
            List<? extends String> l = ConfigSpecs.commonConf.entityBlacklist.get();
            if (l.size() == 1 && l.get(0).equals("UNINITIALIZED")) {
                entityBlacklist.initDefault(server.getOverworld());
                ConfigSpecs.commonConf.entityBlacklist.set(entityBlacklist.writeToString());
            }
        }
    }
}
