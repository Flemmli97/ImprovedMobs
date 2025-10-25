package io.github.flemmli97.improvedmobs.common.config.holder;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.config.values.BreakableBlocks;
import io.github.flemmli97.improvedmobs.common.config.values.EntityFeatureConfig;
import io.github.flemmli97.improvedmobs.common.config.values.EntityItemConfig;
import io.github.flemmli97.improvedmobs.common.config.values.TargetMapConfig;
import net.minecraft.ChatFormatting;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Predicate;

public class ConfigSpecs {

    public static final IConfigSpec CLIENT_SPEC;
    public static final ClientConfigVals CLIENT_CONF;

    public static final IConfigSpec COMMON_SPEC;
    public static final CommonConfigVals COMMON_CONF;

    static class ClientConfigVals {

        public final ModConfigSpec.IntValue guiX;
        public final ModConfigSpec.IntValue guiY;
        public final ModConfigSpec.EnumValue<ChatFormatting> color;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.BooleanValue showDifficulty;
        public final ModConfigSpec.EnumValue<Config.DifficultyBarLocation> location;

        public ClientConfigVals(ModConfigSpec.Builder builder) {
            builder.comment("Gui Configs").push("gui");
            this.guiX = builder.defineInRange("Gui X", Config.ClientConfig.guiX, 0, Integer.MAX_VALUE);
            this.guiY = builder.defineInRange("Gui Y", Config.ClientConfig.guiY, 0, Integer.MAX_VALUE);
            this.color = builder.comment("Textformatting codes for the display of the difficulty").defineEnum("Difficulty color", Config.ClientConfig.color);
            this.scale = builder.comment("Scaling of the difficulty text").defineInRange("Text Scale", Config.ClientConfig.scale, 0, Double.MAX_VALUE);
            this.showDifficulty = builder.comment("Show the the difficulty text").define("Show Difficulty", Config.ClientConfig.showDifficulty);
            this.location = builder.comment("Relative location of the difficulty text in regards to the screen.").defineEnum("Difficulty location", Config.ClientConfig.location);
            builder.pop();
        }
    }

    static class CommonConfigVals {

        //General
        public final ModConfigSpec.BooleanValue enableDifficultyScaling;
        public final ModConfigSpec.IntValue difficultyDelay;
        public final ModConfigSpec.BooleanValue ignoreSpawner;
        public final ModConfigSpec.ConfigValue<List<String>> difficultyIncrease;
        public final ModConfigSpec.BooleanValue ignorePlayers;
        public final ModConfigSpec.BooleanValue considerTimeskip;
        public final ModConfigSpec.EnumValue<Config.DifficultyType> difficultyType;
        public final ModConfigSpec.ConfigValue<String> centerPos;
        public final ModConfigSpec.BooleanValue friendlyFire;
        public final ModConfigSpec.ConfigValue<List<String>> petArmorBlackList;
        public final ModConfigSpec.BooleanValue petWhiteList;

        //Black-WhiteList
        public final ModConfigSpec.ConfigValue<List<String>> featureBlacklist;
        public final ModConfigSpec.ConfigValue<List<String>> entityBlacklist;
        public final ModConfigSpec.ConfigValue<List<String>> featureWhitelist;

        //Integration
        public final ModConfigSpec.EnumValue<Config.IntegrationType> vanillaClamped;
        public final ModConfigSpec.DoubleValue vanillaClampedMax;
        public final ModConfigSpec.EnumValue<Config.IntegrationType> usePowerScaleMod;
        public final ModConfigSpec.EnumValue<Config.IntegrationType> usePlayerEXMod;
        public final ModConfigSpec.DoubleValue playerEXScale;
        public final ModConfigSpec.EnumValue<Config.IntegrationType> useLevelZMod;
        public final ModConfigSpec.DoubleValue levelZScale;
        public final ModConfigSpec.EnumValue<Config.IntegrationType> useRunecraftoryMod;
        public final ModConfigSpec.DoubleValue runecraftoryScale;

        //AI
        public final ModConfigSpec.ConfigValue<List<String>> breakableBlocks;
        public final ModConfigSpec.BooleanValue breakingAsBlacklist;
        public final ModConfigSpec.BooleanValue useBlockBreakSound;
        public final ModConfigSpec.ConfigValue<String> breakerChance;
        public final ModConfigSpec.DoubleValue difficultyBreak;
        public final ModConfigSpec.IntValue breakerInitCooldown;
        public final ModConfigSpec.IntValue breakerCooldown;
        public final ModConfigSpec.BooleanValue ignoreHarvestLevel;
        public final ModConfigSpec.IntValue restoreDelay;
        public final ModConfigSpec.BooleanValue idleBreak;
        public final ModConfigSpec.DoubleValue breakerSightIgnore;
        public final ModConfigSpec.ConfigValue<String> breakSpeed;
        public final ModConfigSpec.ConfigValue<List<String>> blackListedContainerBlocks;
        public final ModConfigSpec.BooleanValue breakBlockEntities;
        public final ModConfigSpec.ConfigValue<List<String>> breakingItems;
        public final ModConfigSpec.ConfigValue<String> stealerChance;
        public final ModConfigSpec.DoubleValue difficultySteal;
        public final ModConfigSpec.ConfigValue<String> neutralAggressiv;
        public final ModConfigSpec.ConfigValue<String> guardianAIChance;
        public final ModConfigSpec.ConfigValue<String> flyAIChance;
        public final ModConfigSpec.BooleanValue tntBlockDestruction;
        public final ModConfigSpec.ConfigValue<String> ignoreSightChance;
        public final ModConfigSpec.ConfigValue<List<String>> autoTargets;

        //Equipment
        public final ModConfigSpec.ConfigValue<List<String>> equipmentModBlacklist;
        public final ModConfigSpec.BooleanValue equipmentModWhitelist;
        public final ModConfigSpec.ConfigValue<List<String>> itemuseBlacklist;
        public final ModConfigSpec.BooleanValue itemuseWhitelist;
        public final ModConfigSpec.ConfigValue<List<String>> entityItemConfig;

        public final ModConfigSpec.ConfigValue<String> equipmentChance;
        public final ModConfigSpec.ConfigValue<String> additionalEquipmentChance;
        public final ModConfigSpec.ConfigValue<String> randomTrimChance;
        public final ModConfigSpec.ConfigValue<String> mainHandChance;
        public final ModConfigSpec.ConfigValue<String> offHandChance;
        public final ModConfigSpec.ConfigValue<String> dropChance;
        public final ModConfigSpec.ConfigValue<String> enchantChance;
        public final ModConfigSpec.ConfigValue<List<String>> enchantCalc;
        public final ModConfigSpec.ConfigValue<List<String>> enchantBlacklist;
        public final ModConfigSpec.BooleanValue enchantWhitelist;

        public CommonConfigVals(ModConfigSpec.Builder builder) {
            builder.comment("General settings", "By default the difficulty caps at 250").push("general");
            this.enableDifficultyScaling = builder.comment("Enables increasing the difficulty over time").define("Enable difficulty scaling", Config.CommonConfig.enableDifficultyScaling);
            this.difficultyDelay = builder.comment("Time in ticks for which the difficulty shouldn't increase at the beginning. One full minecraft day is 24000 ticks").defineInRange("Difficulty Delay", Config.CommonConfig.difficultyDelay, 0, Integer.MAX_VALUE);
            this.ignoreSpawner = builder.comment("Whether mobs from spawners should be ignored").define("Ignore Spawner", Config.CommonConfig.ignoreSpawner);
            this.difficultyIncrease = builder.comment("Handles increase in difficulty regarding current difficulty.",
                    "Difficulty increase runs every 2400 ticks. One minecraft day has 24000 ticks.",
                    "Format is <difficulty threshold>;<expression> with expression being the new difficulty",
                    "Example [\"0;difficulty + 0.01\",\"10;difficulty + 0.1\",\"30;difficulty + 0\"]",
                    "With this the difficulty will increase by 0.01 every 2400 ticks for a total of 0.1 per mc day till reaching a difficulty of 10.",
                    "Afterwards it increases by 1 per mc day till it reaches 30 and then stops increasing.",
                    "Negative values are also supported.",
                    "Lastly following variables are available to use in expressions:",
                    "difficulty: The current difficulty in the context",
                    "distance_spawn: Distance to the spawnpoint. Does not include the height",
                    "distance_origin: Distance to 0,0",
                    "distance_center: Distance to the center as per defined in this config").define("Difficulty Increase", Config.CommonConfig.difficultyIncrease.write(), stringList());
            this.ignorePlayers = builder.comment("Wether difficulty should only increase with at least one online player or not").define("Ignore Players", Config.CommonConfig.ignorePlayers);
            this.considerTimeskip = builder.comment("If true will increase difficulty by the amount of time skipped. Else will only increase difficulty once.").define("Consider Time Skip", Config.CommonConfig.considerTimeskip);
            this.difficultyType = builder.comment("How the difficulty at a position is calculated. Supported values are: ",
                    "GLOBAL: Serverwide difficulty value",
                    "PLAYERMAX: Maximum difficulty of players in a 256 radius around the position",
                    "PLAYERMEAN: Average difficulty of players in a 256 radius around the position",
                    "PLAYERSUM: Sum of difficulty of players in a 256 radius around the position. There is no upper limit for this so max difficulty can be higher than the limit! You crazy if you use this",
                    "DISTANCE: Uses the distance to the position defined in Center Position to define the difficulty",
                    "DISTANCESPAWN: Uses the distance to the world spawn to define the difficulty",
                    "If the type is any of the distance types the first value in the Difficulty Increase will be the distance to whatever type is used while the second expression is the direct difficulty",
                    "E.g. if using DISTANCESPAWN the first value will be the distance to world spawn",
                    "Example: [\"0;0\",\"500;(distance_spawn - 500) * 0.01\"]",
                    "Here the difficulty between 0-500 blocks to spawn will stay at 0. From then on the difficulty increases by 1 every 100 blocks further",
                    "Note: the `difficulty` variable in distance based expression is always 0!").defineEnum("Difficulty type", Config.CommonConfig.difficultyType);
            this.centerPos = builder.comment("Position used for DISTANCE difficulty type").define("Center Position", Config.CommonConfig.centerPos.write());
            this.friendlyFire = builder.comment("Disable/Enable friendly fire for owned pets.").define("FriendlyFire", Config.CommonConfig.friendlyFire);
            this.petArmorBlackList = builder.comment("Blacklist for pet you should't be able to give armor to. Pets from mods, which have custom armor already should be included here (for balancing reasons).").define("Pet Blacklist", Config.CommonConfig.petArmorBlackList, stringList());
            this.petWhiteList = builder.comment("Treat pet blacklist as whitelist").define("Pet Whitelist", Config.CommonConfig.petWhiteList);
            builder.pop();

            builder.comment("Enable/Disable difficulty features").push("features");
            this.featureBlacklist = builder.comment("Any of the following ",
                    DifficultyFeatures.toggable().toString(),
                    "added here will disable that feature completely.",
                    "E.g. [\"" + DifficultyFeatures.GUARDIAN + "\"] will disable the guardian feature").define("Feature Blacklist", Config.CommonConfig.featureBlacklist.stream().map(Enum::toString).toList(), stringList());
            this.entityBlacklist = builder.comment(EntityFeatureConfig.use()).define("Entity Configs", Lists.newArrayList("UNINITIALIZED"), stringList());
            this.featureWhitelist = builder.comment("Any of the following ",
                    DifficultyFeatures.toggable().toString(),
                    "added here will turn that feature into a whitelist in regards to Entity Configs.").define("Feature Whitelist", Config.CommonConfig.featureBlacklist.stream().map(Enum::toString).toList(), stringList());
            builder.pop();

            builder.comment("Settings for mod integration").push("integration");
            this.vanillaClamped = builder.comment("Whether vanillas clamped regional difficulty should be used. ", "See https://minecraft.wiki/w/Difficulty#Clamped_regional_difficulty").defineEnum("Use Vanilla Difficulty", Config.CommonConfig.vanillaClamped);
            this.vanillaClampedMax = builder.comment("The max value for vanilla difficulty scaling. As clamped regional difficulty returns a value between 0 and 1", "Thus difficulty will be regional difficulty * max").defineInRange("Vanilla Max", Config.CommonConfig.vanillaClampedMax, 0, Double.MAX_VALUE);
            this.usePowerScaleMod = builder.comment("Should the power scale mods difficulty system be used instead of this ones. (Requires power scale mod)").defineEnum("Use Power Scale Mod", Config.CommonConfig.usePowerScaleMod);
            this.usePlayerEXMod = builder.comment("If true and playerEx is installed will use the level from playerEx as difficulty").defineEnum("Use Player EX Mod", Config.CommonConfig.usePlayerEXMod);
            this.playerEXScale = builder.comment("Scaling for playerEX integration").defineInRange("PlayerEX Scaling", Config.CommonConfig.playerEXScale, 0, Double.MAX_VALUE);
            this.useLevelZMod = builder.comment("If true and LevelZ is installed will use the the total skill level from LevelZ as difficulty").defineEnum("Use LevelZ Mod", Config.CommonConfig.useLevelZMod);
            this.levelZScale = builder.comment("Scaling for LevelZ integration").defineInRange("LevelZ Scaling", Config.CommonConfig.levelZScale, 0, Double.MAX_VALUE);
            this.useRunecraftoryMod = builder.comment("If true and RuneCraftory is installed will use the level from RuneCraftory as difficulty").defineEnum("Use RuneCraftory Mod", Config.CommonConfig.useRunecraftoryMod);
            this.runecraftoryScale = builder.comment("Scaling for RuneCraftory integration").defineInRange("RuneCraftory Scaling", Config.CommonConfig.runecraftoryScale, 0, Double.MAX_VALUE);
            builder.pop();

            builder.comment("Settings regarding custom ai for mobs").push("ai");
            this.breakableBlocks = builder.comment("Whitelist for blocks, which can be actively broken.", BreakableBlocks.use()).define("Block Break Whitelist", Config.CommonConfig.breakableBlocks.write(), stringList());
            this.breakingAsBlacklist = builder.comment("Treat Block Whitelist as Blacklist").define("Breaklist as Blacklist", Config.CommonConfig.breakingAsBlacklist);
            this.useBlockBreakSound = builder.comment("Use the block breaking sound instead of a knocking sound").define("Sound", Config.CommonConfig.useBlockBreakSound);
            this.breakerChance = builder.comment("(Expression): Chance for a mob to be able to break blocks").define("Breaker Chance", Config.CommonConfig.breakerChance.write());
            this.difficultyBreak = builder.comment("Difficulty at which mobs are able to break blocks").defineInRange("Difficulty Break AI", Config.CommonConfig.difficultyBreak, 0, Double.MAX_VALUE);
            this.breakerInitCooldown = builder.comment("Initial cooldown for block breaking mobs").defineInRange("Breaker Initial Cooldown", Config.CommonConfig.breakerInitCooldown, 0, Integer.MAX_VALUE);
            this.breakerCooldown = builder.comment("Cooldown for breaking blocks").defineInRange("Breaker Cooldown", Config.CommonConfig.breakerCooldown, 0, Integer.MAX_VALUE);
            this.ignoreHarvestLevel = builder.comment("By default mobs can only break the block they can harvest with the current tool they holding. Set this to true to disable that check (The block will not drop if they cant harvest it though!).").define("Ignore Harvest Check", Config.CommonConfig.ignoreHarvestLevel);
            this.restoreDelay = builder.comment("Blocks will be restored after x ticks being broken. If set to 0 will never restore", "This will not restore block entity data!").defineInRange("Restore delay", Config.CommonConfig.restoreDelay, 0, Integer.MAX_VALUE);
            this.idleBreak = builder.comment("If mobs should break blocks when not chasing a target").define("Idle Break", Config.CommonConfig.idleBreak);
            this.breakerSightIgnore = builder.comment("Chance a breaker mob to ignore line of sight").defineInRange("Breaker Sight Ignore", Config.CommonConfig.breakerSightIgnore, 0, 1);
            this.breakSpeed = builder.comment("(Expression): The speed modifier for breaking blocks").define("Breaking Speed Modifier", Config.CommonConfig.breakSpeed.write());
            this.stealerChance = builder.comment("(Expression): Chance for a mob to be able to steal items from inventory blocks").define("Stealer Chance", Config.CommonConfig.stealerChance.write());
            this.difficultySteal = builder.comment("Difficulty at which mobs are able to steal items").defineInRange("Difficulty Steal AI", Config.CommonConfig.difficultySteal, 0, Double.MAX_VALUE);
            this.blackListedContainerBlocks = builder.comment("List of blocks mobs shouldn't steal from. You can also add a modid to blacklist whole mods").define("Steal Block Blacklist", Config.CommonConfig.blackListedContainerBlocks, stringList());
            this.breakingItems = builder.comment("Items which will be given to mobs who can break blocks. Empty list = no items. Syntax: id;weight", "Note: Mobs can only break blocks if the tool they are holding can break the blocks").define("Breaking items", Lists.newArrayList("minecraft:diamond_pickaxe;1", "minecraft:iron_axe;2"), stringList());
            this.breakBlockEntities = builder.comment("Should mobs be able to break block entities? Evaluated before the break list").define("Break BlockEntities", Config.CommonConfig.breakBlockEntities);
            this.neutralAggressiv = builder.comment("(Expression): Chance for neutral mobs to be aggressive").define("Neutral Aggressive Chance", Config.CommonConfig.neutralAggressiv.write());
            this.guardianAIChance = builder.comment("(Expression): Chance for mobs to be able to summon an aquatic mount").define("Guardian Chance", Config.CommonConfig.guardianAIChance.write());
            this.flyAIChance = builder.comment("(Expression): Chance for mobs to be able to summon a flying mount").define("Flying Chance", Config.CommonConfig.flyAIChance.write());
            this.tntBlockDestruction = builder.comment("Set this to true to allow tnt thrown by mobs to destroy blocks").define("TNT Block Destruction", Config.CommonConfig.tntBlockDestruction);
            this.ignoreSightChance = builder.comment("(Expression): Chance for a mob to ignore line of sight", "This config ONLY affects villager target, neutral aggressive and auto targeting feature").define("Ignore Sight", Config.CommonConfig.ignoreSightChance.write());
            this.autoTargets = builder.comment(TargetMapConfig.use()).define("Auto Target List", Config.CommonConfig.autoTargets.write(), stringList());
            builder.pop();

            builder.comment("Configs regarding mobs spawning with equipment").push("equipment");
            this.equipmentModBlacklist = builder.comment("Blacklist items from whole mods. Add modid to prevent items from that mod being equipped.",
                    "For individual items use the equipment.json!",
                    "e.g. minecraft will disable all vanilla items to be given to mobs").define("Item Blacklist", Config.CommonConfig.equipmentModBlacklist, stringList());
            this.equipmentModWhitelist = builder.comment("Use blacklist as whitelist").define("Item Whitelist", Config.CommonConfig.equipmentModWhitelist);
            this.itemuseBlacklist = builder.comment("Blacklist for items mobs should never be able to use.", "Use as in using the item similar to players (e.g. shooting bows)").define("Item Use Blacklist", Config.CommonConfig.itemuseBlacklist, stringList());
            this.itemuseWhitelist = builder.comment("Turn the use blacklist into a whitelist").define("Item Use Whitelist", Config.CommonConfig.itemuseWhitelist);
            this.entityItemConfig = builder.comment("This mod adds a custom AI to mobs so they can use items.",
                    "Some mobs are able to use some items by default.",
                    "This defines a mapping to prevent those mobs using the added AI",
                    "For example Skeletons already use bows and if not blacklisted will make super fast shooting skeletons",
                    EntityItemConfig.use()).define("Entity Item Use Blacklist", Config.CommonConfig.entityItemConfig.writeToString(), stringList());
            this.equipmentChance = builder.comment("(Expression): Chance for a mob to have a piece of armor").define("Equipment Chance", Config.CommonConfig.equipmentChance.write());
            this.additionalEquipmentChance = builder.comment("(Expression): Chance for a mob to have additional pieces of armor").define("Additional Equipment Chance", Config.CommonConfig.additionalEquipmentChance.write());
            this.randomTrimChance = builder.comment("(Expression): Chance for an equipment to have a random armor trim").define("Armor Trim Chance", Config.CommonConfig.randomTrimChance.write());
            this.mainHandChance = builder.comment("(Expression): Chance for a mob to have an item in its main hand").define("Main Hand Item Chance", Config.CommonConfig.mainHandChance.write());
            this.offHandChance = builder.comment("(Expression): Chance for a mob to have an item in its offhand").define("Offhand Item Chance", Config.CommonConfig.offHandChance.write());
            this.dropChance = builder.comment("(Expression): Chance for a mob to drop its items", "This ONLY applies to equipment added through this mod!").define("Drop Chance", Config.CommonConfig.dropChance.write());
            this.enchantChance = builder.comment("(Expression): Chance for an equipment piece to be enchanted").define("Enchanting Chance", Config.CommonConfig.enchantChance.write());
            this.enchantCalc = builder.comment("Specify enchanting levels according to difficulty. <difficulty;expression>").define("Enchanting Calc", Config.CommonConfig.enchantCalc.write(), stringList());
            this.enchantBlacklist = builder.comment("Blacklist enchantments from being applied to equipments").define("Enchanting Blacklist", Config.CommonConfig.enchantBlacklist, stringList());
            this.enchantWhitelist = builder.comment("Turn the enchant blacklist to a whitelist").define("Enchanting Whitelist", Config.CommonConfig.enchantWhitelist);
            builder.pop();
        }
    }

    private static Predicate<Object> stringList() {
        return p -> p instanceof List<?> list && list.stream().allMatch(e -> e instanceof String);
    }

    static {
        Pair<ClientConfigVals, ModConfigSpec> specPair1 = new ModConfigSpec.Builder().configure(ClientConfigVals::new);
        CLIENT_SPEC = specPair1.getRight();
        CLIENT_CONF = specPair1.getLeft();

        Pair<CommonConfigVals, ModConfigSpec> specPair2 = new ModConfigSpec.Builder().configure(CommonConfigVals::new);
        COMMON_SPEC = specPair2.getRight();
        COMMON_CONF = specPair2.getLeft();
    }
}
