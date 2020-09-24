package com.flemmli97.improvedmobs.config;

import com.flemmli97.tenshilib.api.config.ItemWrapper;
import net.minecraft.item.Items;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static class ClientConfig {

        public static int guiX;
        public static int guiY;
        public static TextFormatting color = TextFormatting.DARK_PURPLE;
        public static float scale = 1;

        public static void load(boolean reload){
            guiX = ConfigSpecs.clientConf.guiX.get();
            guiY = ConfigSpecs.clientConf.guiY.get();
            color = ConfigSpecs.clientConf.color.get();
            scale = ConfigSpecs.clientConf.scale.get();
        }
    }

    public static class ServerConfig {

        //General
        public static boolean enableDifficultyScaling = true;
        public static int difficultyDelay;
        public static boolean ignorePlayers;
        public static String[] mobListLight = new String[0];
        public static boolean mobListLightBlackList;
        public static int light;
        public static boolean shouldPunishTimeSkip;
        public static boolean friendlyFire;
        public static String[] petArmorBlackList = new String[0];
        public static boolean petWhiteList;

        public static boolean doIMDifficulty = true;

        //Black-WhiteList
        public static EntityModifyFlagConfig entityBlacklist;// = new EntityModifyFlagConfig();
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
        public static BreakableBlocks breakableBlocks;// = new BreakableBlocks(new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "DoorBlock", "!minecraft:iron_door", "minecraft:glass_pane", "minecraft:stained_glass_pane"});
        public static boolean breakingAsBlacklist;
        public static boolean useBlockBreakSound;
        public static float breakerChance = 1;
        public static float stealerChance= 1;
        public static boolean breakTileEntities;
        public static ItemWrapper breakingItem;// = new ItemWrapper(Items.DIAMOND_PICKAXE);
        public static float neutralAggressiv;
        public static MobClassMapConfig autoTargets;// = new MobClassMapConfig(new String[0]);
        public static int repairTick = 200;
        public static float difficultyBreak;
        public static float difficultySteal;

        //Equipment
        public static String[] equipmentModBlacklist = new String[0];
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
        public static float healthIncrease;
        public static float healthMax;
        public static float roundHP;
        public static float damageIncrease;
        public static float damageMax;
        public static float speedIncrease;
        public static float speedMax;
        public static float knockbackIncrease;
        public static float knockbackMax;
        public static float magicResIncrease;
        public static float magicResMax;
        public static float projectileIncrease;
        public static float projectileMax;

        public static void load(boolean reload){

        }
    }
}
