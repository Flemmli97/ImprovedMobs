package com.flemmli97.improvedmobs.config;

import com.flemmli97.tenshilib.api.config.ItemWrapper;
import net.minecraft.item.Items;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    public static final ForgeConfigSpec clientSpec;
    public static final ClientConfig clientConf;

    public static final ForgeConfigSpec commonSpec;
    public static final ServerConfig commonConf;

    public static class ClientConfig {

        public int guiX;
        public int guiY;
        public TextFormatting color = TextFormatting.DARK_PURPLE;
        public float scale = 1;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            /*guiX = builder.translation("").define("", 0).get();
            guiY = builder.translation("").define("", 0).get();
            color = builder.translation("").defineEnum("", TextFormatting.DARK_PURPLE).get();
            scale = builder.translation("").define("", 0).get();*/
        }
    }

    public static class ServerConfig {

        //General
        public boolean enableDifficultyScaling = true;
        public int difficultyDelay;
        public boolean ignorePlayers;
        public String[] mobListLight = new String[0];
        public boolean mobListLightBlackList;
        public int light;
        public boolean shouldPunishTimeSkip;
        public boolean friendlyFire;
        public String[] petArmorBlackList = new String[0];
        public boolean petWhiteList;

        public boolean doIMDifficulty = true;

        //Black-WhiteList
        public EntityModifyFlagConfig entityBlacklist = new EntityModifyFlagConfig();
        public boolean mobAttributeWhitelist;
        public boolean armorMobWhitelist;
        public boolean heldMobWhitelist;
        public boolean mobListBreakWhitelist;
        public boolean mobListUseWhitelist;
        public boolean mobListLadderWhitelist;
        public boolean mobListStealWhitelist;
        public boolean mobListBoatWhitelist;
        public boolean targetVillagerWhitelist;

        //Debug
        public boolean debugPath;

        //Integration
        public boolean useScalingHealthMod = true;
        public boolean useTGunsMod = true;
        public boolean useReforgedMod = true;
        public boolean useCoroUtil = true;

        //AI
        public BreakableBlocks breakableBlocks = new BreakableBlocks(new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "DoorBlock", "!minecraft:iron_door", "minecraft:glass_pane", "minecraft:stained_glass_pane"});
        public boolean breakingAsBlacklist;
        public boolean useBlockBreakSound;
        public float breakerChance = 1;
        public float stealerChance= 1;
        public boolean breakTileEntities;
        public ItemWrapper breakingItem = new ItemWrapper(Items.DIAMOND_PICKAXE);
        public float neutralAggressiv;
        public MobClassMapConfig autoTargets = new MobClassMapConfig(new String[0]);
        public int repairTick = 200;
        public float difficultyBreak;
        public float difficultySteal;

        //Equipment
        public String[] equipmentModBlacklist = new String[0];
        public boolean equipmentModWhitelist;
        public float baseEquipChance;
        public float baseEquipChanceAdd;
        public float diffEquipAdd;
        public float baseWeaponChance;
        public float diffWeaponChance;
        public float baseEnchantChance;
        public float diffEnchantAdd;
        public float baseItemChance;
        public float diffItemChanceAdd;
        public boolean shouldDropEquip;

        //Attributes
        public float healthIncrease;
        public float healthMax;
        public float roundHP;
        public float damageIncrease;
        public float damageMax;
        public float speedIncrease;
        public float speedMax;
        public float knockbackIncrease;
        public float knockbackMax;
        public float magicResIncrease;
        public float magicResMax;
        public float projectileIncrease;
        public float projectileMax;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            //autoAddAI = builder.comment("Auto target mobs from other teams (if e.g. done per command)").translation("conf.mobbattle.addai")
            //        .define("autoAddAI", true);
            //builder.pop();
        }
    }

    static {
        Pair<ClientConfig, ForgeConfigSpec> specPair1 = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        clientSpec = specPair1.getRight();
        clientConf = specPair1.getLeft();

        Pair<ServerConfig, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        commonSpec = specPair2.getRight();
        commonConf = specPair2.getLeft();
    }
}
