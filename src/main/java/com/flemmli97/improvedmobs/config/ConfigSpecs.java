package com.flemmli97.improvedmobs.config;

import com.google.common.collect.Lists;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ConfigSpecs {

    public static final ForgeConfigSpec clientSpec;
    public static final ClientConfigVals clientConf;

    public static final ForgeConfigSpec commonSpec;
    public static final ServerConfigVals commonConf;

    static class ClientConfigVals {

        public ForgeConfigSpec.ConfigValue<Integer> guiX;
        public ForgeConfigSpec.ConfigValue<Integer> guiY;
        public ForgeConfigSpec.ConfigValue<TextFormatting> color;
        public ForgeConfigSpec.ConfigValue<Float> scale;

        public ClientConfigVals(ForgeConfigSpec.Builder builder) {
            builder/*.translation("improvedmobs.gui")*/.comment("Gui Configs").push("gui");
            this.guiX = builder.define("Gui X", 5);
            this.guiY = builder.define("Gui Y", 5);
            this.color = builder.comment("Textformatting codes for the display of the difficulty").defineEnum("Difficulty color", TextFormatting.DARK_PURPLE);
            this.scale = builder.comment("Scaling of the difficulty text").define("Text Scale", 1F);
            builder.pop();
        }
    }

    static class ServerConfigVals {

        //General
        public ForgeConfigSpec.BooleanValue enableDifficultyScaling;
        public ForgeConfigSpec.ConfigValue<Integer> difficultyDelay;
        public ForgeConfigSpec.BooleanValue ignorePlayers;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> mobListLight;
        public ForgeConfigSpec.BooleanValue mobListLightBlackList;
        public int light;
        public ForgeConfigSpec.BooleanValue shouldPunishTimeSkip;
        public ForgeConfigSpec.BooleanValue friendlyFire;
        public String[] petArmorBlackList = new String[0];
        public ForgeConfigSpec.BooleanValue petWhiteList;

        /*public ForgeConfigSpec.BooleanValue doIMDifficulty;

        //Black-WhiteList
        public EntityModifyFlagConfig entityBlacklist;// = new EntityModifyFlagConfig();
        public ForgeConfigSpec.BooleanValue mobAttributeWhitelist;
        public ForgeConfigSpec.BooleanValue armorMobWhitelist;
        public ForgeConfigSpec.BooleanValue heldMobWhitelist;
        public ForgeConfigSpec.BooleanValue mobListBreakWhitelist;
        public ForgeConfigSpec.BooleanValue mobListUseWhitelist;
        public ForgeConfigSpec.BooleanValue mobListLadderWhitelist;
        public ForgeConfigSpec.BooleanValue mobListStealWhitelist;
        public ForgeConfigSpec.BooleanValue mobListBoatWhitelist;
        public ForgeConfigSpec.BooleanValue targetVillagerWhitelist;

        //Debug
        public ForgeConfigSpec.BooleanValue debugPath;

        //Integration
        public ForgeConfigSpec.BooleanValue useScalingHealthMod = true;
        public ForgeConfigSpec.BooleanValue useTGunsMod = true;
        public ForgeConfigSpec.BooleanValue useReforgedMod = true;
        public ForgeConfigSpec.BooleanValue useCoroUtil = true;

        //AI
        public BreakableBlocks breakableBlocks;// = new BreakableBlocks(new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "DoorBlock", "!minecraft:iron_door", "minecraft:glass_pane", "minecraft:stained_glass_pane"});
        public ForgeConfigSpec.BooleanValue breakingAsBlacklist;
        public ForgeConfigSpec.BooleanValue useBlockBreakSound;
        public float breakerChance = 1;
        public float stealerChance= 1;
        public ForgeConfigSpec.BooleanValue breakTileEntities;
        public ItemWrapper breakingItem;// = new ItemWrapper(Items.DIAMOND_PICKAXE);
        public float neutralAggressiv;
        public MobClassMapConfig autoTargets;// = new MobClassMapConfig(new String[0]);
        public int repairTick = 200;
        public float difficultyBreak;
        public float difficultySteal;

        //Equipment
        public String[] equipmentModBlacklist = new String[0];
        public ForgeConfigSpec.BooleanValue equipmentModWhitelist;
        public float baseEquipChance;
        public float baseEquipChanceAdd;
        public float diffEquipAdd;
        public float baseWeaponChance;
        public float diffWeaponChance;
        public float baseEnchantChance;
        public float diffEnchantAdd;
        public float baseItemChance;
        public float diffItemChanceAdd;
        public ForgeConfigSpec.BooleanValue shouldDropEquip;

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
        public float projectileMax;*/

        public ServerConfigVals(ForgeConfigSpec.Builder builder) {
            builder.comment("With default value every difficulty perk maxes out at difficulty 250")/*.translation("improvedmobs.general")*/.push("general");
            //General
            enableDifficultyScaling = builder.comment("Disable/Enables the whole difficulty scaling of this mod").define("Enable difficulty scaling", true);
            difficultyDelay = builder.comment("Time in ticks for which the difficulty shouldnt increase at the beginning. One full minecraft day is 24000 ticks").define("Difficulty Delay", 0);
            ignorePlayers = builder.comment("Wether difficulty should only increase with at least one online players or not").define("Ignore Players", false);
            mobListLight = builder.comment("Mobs to include for the new light spawning rules.").defineList("Light list", Lists.newArrayList(), String.class::isInstance);
            mobListLightBlackList = builder.comment("Turn the list list whitelist to blacklist").define("Light list blacklist", false);
            /*public ForgeConfigSpec.BooleanValue mobListLightBlackList;
            public int light;
            public ForgeConfigSpec.BooleanValue shouldPunishTimeSkip;
            public ForgeConfigSpec.BooleanValue friendlyFire;
            public String[] petArmorBlackList = new String[0];
            public ForgeConfigSpec.BooleanValue petWhiteList;*/
            builder.pop();
            //public ForgeConfigSpec.BooleanValue doIMDifficulty;
        }
    }

    static {
        Pair<ClientConfigVals, ForgeConfigSpec> specPair1 = new ForgeConfigSpec.Builder().configure(ClientConfigVals::new);
        clientSpec = specPair1.getRight();
        clientConf = specPair1.getLeft();

        Pair<ServerConfigVals, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(ServerConfigVals::new);
        commonSpec = specPair2.getRight();
        commonConf = specPair2.getLeft();
    }
}
