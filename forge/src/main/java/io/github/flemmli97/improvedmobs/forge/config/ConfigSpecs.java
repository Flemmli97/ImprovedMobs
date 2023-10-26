package io.github.flemmli97.improvedmobs.forge.config;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.config.BreakableBlocks;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.EntityItemConfig;
import io.github.flemmli97.improvedmobs.config.EntityModifyFlagConfig;
import io.github.flemmli97.improvedmobs.config.MobClassMapConfig;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ConfigSpecs {

    public static final ForgeConfigSpec clientSpec;
    public static final ClientConfigVals clientConf;

    public static final ForgeConfigSpec commonSpec;
    public static final CommonConfigVals commonConf;

    static class ClientConfigVals {

        public final ForgeConfigSpec.IntValue guiX;
        public final ForgeConfigSpec.IntValue guiY;
        public final ForgeConfigSpec.EnumValue<ChatFormatting> color;
        public final ForgeConfigSpec.DoubleValue scale;
        public final ForgeConfigSpec.BooleanValue showDifficulty;
        public final ForgeConfigSpec.EnumValue<Config.DifficultyBarLocation> location;

        public ClientConfigVals(ForgeConfigSpec.Builder builder) {
            builder/*.translation("improvedmobs.gui")*/.comment("Gui Configs").push("gui");
            this.guiX = builder.defineInRange("Gui X", 5, 0, Integer.MAX_VALUE);
            this.guiY = builder.defineInRange("Gui Y", 5, 0, Integer.MAX_VALUE);
            this.color = builder.comment("Textformatting codes for the display of the difficulty").defineEnum("Difficulty color", ChatFormatting.DARK_PURPLE);
            this.scale = builder.comment("Scaling of the difficulty text").defineInRange("Text Scale", 1D, 0, Double.MAX_VALUE);
            this.showDifficulty = builder.comment("Show the the difficulty text").define("Show Difficulty", true);
            this.location = builder.comment("Relative location of the difficulty text in regards to the screen.").defineEnum("Difficulty location", Config.DifficultyBarLocation.TOPLEFT);
            builder.pop();
        }
    }

    static class CommonConfigVals {

        //General
        public final ForgeConfigSpec.BooleanValue enableDifficultyScaling;
        public final ForgeConfigSpec.IntValue difficultyDelay;
        public final ForgeConfigSpec.ConfigValue<List<String>> increaseHandler;
        public final ForgeConfigSpec.BooleanValue ignorePlayers;
        public final ForgeConfigSpec.BooleanValue shouldPunishTimeSkip;
        public final ForgeConfigSpec.BooleanValue friendlyFire;
        public final ForgeConfigSpec.ConfigValue<List<String>> petArmorBlackList;
        public final ForgeConfigSpec.BooleanValue petWhiteList;
        public final ForgeConfigSpec.BooleanValue doIMDifficulty;
        public final ForgeConfigSpec.EnumValue<Config.DifficultyType> difficultyType;
        public final ForgeConfigSpec.ConfigValue<String> centerPos;

        //Black-WhiteList
        public final ForgeConfigSpec.ConfigValue<List<String>> flagBlacklist;
        public final ForgeConfigSpec.ConfigValue<List<String>> entityBlacklist;
        public final ForgeConfigSpec.BooleanValue mobAttributeWhitelist;
        public final ForgeConfigSpec.BooleanValue armorMobWhitelist;
        public final ForgeConfigSpec.BooleanValue heldMobWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListBreakWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListUseWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListLadderWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListStealWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListBoatWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListFlyWhitelist;
        public final ForgeConfigSpec.BooleanValue targetVillagerWhitelist;
        public final ForgeConfigSpec.BooleanValue neutralAggroWhitelist;

        //Integration
        public final ForgeConfigSpec.BooleanValue useScalingHealthMod;
        public final ForgeConfigSpec.BooleanValue usePlayerEXMod;
        public final ForgeConfigSpec.BooleanValue useLevelZMod;
        public final ForgeConfigSpec.BooleanValue varySizebyPehkui;
        public final ForgeConfigSpec.DoubleValue sizeMin;
        public final ForgeConfigSpec.DoubleValue sizeMax;

        //AI
        public final ForgeConfigSpec.ConfigValue<List<String>> breakableBlocks;
        public final ForgeConfigSpec.BooleanValue breakingAsBlacklist;
        public final ForgeConfigSpec.BooleanValue useBlockBreakSound;
        public final ForgeConfigSpec.DoubleValue breakerChance;
        public final ForgeConfigSpec.IntValue breakerInitCooldown;
        public final ForgeConfigSpec.IntValue breakerCooldown;
        public final ForgeConfigSpec.BooleanValue idleBreak;
        public final ForgeConfigSpec.DoubleValue stealerChance;
        public final ForgeConfigSpec.ConfigValue<List<String>> blackListedContainerBlocks;
        public final ForgeConfigSpec.BooleanValue breakTileEntities;
        public final ForgeConfigSpec.ConfigValue<List<String>> breakingItems;
        public final ForgeConfigSpec.DoubleValue neutralAggressiv;
        public final ForgeConfigSpec.ConfigValue<List<String>> autoTargets;
        public final ForgeConfigSpec.DoubleValue difficultyBreak;
        public final ForgeConfigSpec.DoubleValue difficultySteal;
        public final ForgeConfigSpec.DoubleValue guardianAIChance;
        public final ForgeConfigSpec.DoubleValue flyAIChance;

        //Equipment
        public final ForgeConfigSpec.ConfigValue<List<String>> equipmentModBlacklist;
        public final ForgeConfigSpec.BooleanValue equipmentModWhitelist;
        public final ForgeConfigSpec.ConfigValue<List<String>> itemuseBlacklist;
        public final ForgeConfigSpec.BooleanValue itemuseWhitelist;
        public final ForgeConfigSpec.ConfigValue<List<String>> entityItemConfig;

        public final ForgeConfigSpec.DoubleValue baseEquipChance;
        public final ForgeConfigSpec.DoubleValue baseEquipChanceAdd;
        public final ForgeConfigSpec.DoubleValue diffEquipAdd;
        public final ForgeConfigSpec.DoubleValue baseWeaponChance;
        public final ForgeConfigSpec.DoubleValue diffWeaponChance;
        public final ForgeConfigSpec.DoubleValue baseEnchantChance;
        public final ForgeConfigSpec.DoubleValue diffEnchantAdd;
        public final ForgeConfigSpec.ConfigValue<List<String>> enchantCalc;
        public final ForgeConfigSpec.DoubleValue baseItemChance;
        public final ForgeConfigSpec.DoubleValue diffItemChanceAdd;
        public final ForgeConfigSpec.BooleanValue shouldDropEquip;

        //Attributes
        public final ForgeConfigSpec.DoubleValue healthIncrease;
        public final ForgeConfigSpec.DoubleValue healthMax;
        public final ForgeConfigSpec.DoubleValue roundHP;
        public final ForgeConfigSpec.DoubleValue damageIncrease;
        public final ForgeConfigSpec.DoubleValue damageMax;
        public final ForgeConfigSpec.DoubleValue speedIncrease;
        public final ForgeConfigSpec.DoubleValue speedMax;
        public final ForgeConfigSpec.DoubleValue knockbackIncrease;
        public final ForgeConfigSpec.DoubleValue knockbackMax;
        public final ForgeConfigSpec.DoubleValue magicResIncrease;
        public final ForgeConfigSpec.DoubleValue magicResMax;
        public final ForgeConfigSpec.DoubleValue projectileIncrease;
        public final ForgeConfigSpec.DoubleValue projectileMax;
        public final ForgeConfigSpec.DoubleValue explosionIncrease;
        public final ForgeConfigSpec.DoubleValue explosionMax;

        public CommonConfigVals(ForgeConfigSpec.Builder builder) {
            builder.comment("Default difficulty caps at 250")/*.translation("improvedmobs.general")*/.push("general");
            this.enableDifficultyScaling = builder.comment("Disable/Enables the whole difficulty scaling of this mod. Requires a mc restart").define("Enable difficulty scaling", true);
            this.difficultyDelay = builder.comment("Time in ticks for which the difficulty shouldnt increase at the beginning. One full minecraft day is 24000 ticks").defineInRange("Difficulty Delay", 0, 0, Integer.MAX_VALUE);
            this.increaseHandler = builder.comment("Handles increase in difficulty regarding current difficulty.", "Format is <minimum current difficulty>-<increase every 2400 ticks>", "Example [\"0-0.01\",\"10-0.1\",\"30-0\"]", "-> So the difficulty increases by 0.01 every 2400 ticks (->0.1 per mc day since a mc day has 24000 ticks) till it reaches a difficulty of 10.", "Then it increases by 1 per mc day till it reaches 30 and then stops.").define("Difficulty Increase", Lists.newArrayList("0-0.1", "250-0"));
            this.ignorePlayers = builder.comment("Wether difficulty should only increase with at least one online players or not").define("Ignore Players", false);
            this.shouldPunishTimeSkip = builder.comment("If true will increase difficulty by the amount of time skipped. Else will only increase difficulty once.").define("Punish Time Skip", true);
            this.friendlyFire = builder.comment("Disable/Enable friendly fire for owned pets.").define("FriendlyFire", false);
            this.petArmorBlackList = builder.comment("Blacklist for pet you should't be able to give armor to. Pets from mods, which have custom armor already should be included here (for balancing reasons).").define("Pet Blacklist", new ArrayList<>());
            this.petWhiteList = builder.comment("Treat pet blacklist as whitelist").define("Pet Whitelist", false);
            this.doIMDifficulty = builder.comment("Increase difficulty with time", "Here untill its back as a gamerule").define("Difficulty toggle", true);
            this.difficultyType = builder.comment("How the difficulty at a position is calculated. Supported values are: ",
                    "GLOBAL: Serverwide difficulty value",
                    "PLAYERMAX: Maximum difficulty of players in a 256 radius around the position",
                    "PLAYERMEAN: Average difficulty of players in a 256 radius around the position",
                    "DISTANCE: Uses the distance to the position defined in Center Position to define the difficulty",
                    "DISTANCESPAWN: Uses the distance to the world spawn to define the difficulty",
                    "If the type is any of the distance types the functionality of Difficulty Increase is changed to the following where the 1. value is the minimum distance and the 2. is the difficulty that applies. ",
                    "E.g. [\"0-0\",\"1000-5\" translates to 0 difficulty between 0-1000 distance and 5 difficulty for distance >= 1000").defineEnum("Difficulty type", Config.DifficultyType.GLOBAL);
            this.centerPos = builder.comment("Position used for DISTANCE difficulty type").define("Center Position", Config.CommonConfig.centerPos.writeToString());
            builder.pop();

            builder.comment("Black/Whitelist for various stuff").push("list");
            this.entityBlacklist = builder.comment(EntityModifyFlagConfig.use()).define("Entity Configs", Lists.newArrayList("UNINITIALIZED"));
            this.flagBlacklist = builder.comment("Any of the following ", EntityModifyFlagConfig.Flags.toggable().toString(), "added here will disable that feature completely.", "E.g. [\"GUARDIAN\"] will disable the guardian feature").define("Flag Blacklist", new ArrayList<>());
            this.mobAttributeWhitelist = builder.comment("Treat ATTRIBUTES flags as whitelist").define("Attribute Whitelist", false);
            this.armorMobWhitelist = builder.comment("Treat ARMOR flags as whitelist").define("Armor Equip Whitelist", false);
            this.heldMobWhitelist = builder.comment("Treat HELDITEMS flags as whitelist").define("Held Equip Whitelist", false);
            this.mobListBreakWhitelist = builder.comment("Treat BLOCKBREAK flags as whitelist").define("Breaker Whitelist", false);
            this.mobListUseWhitelist = builder.comment("Treat USEITEM flags as whitelist").define("Use Flag Whitelist", false);
            this.mobListLadderWhitelist = builder.comment("Treat LADDER flags as whitelist").define("Ladder Whitelist", false);
            this.mobListStealWhitelist = builder.comment("Treat STEAL flags as whitelist").define("Steal Whitelist", false);
            this.mobListBoatWhitelist = builder.comment("Treat GUARDIAN flags as whitelist").define("Guardian Whitelist", false);
            this.mobListFlyWhitelist = builder.comment("Treat PARROT flags as whitelist").define("Phantom Whitelist", false);
            this.targetVillagerWhitelist = builder.comment("Treat TARGETVILLAGER flags as whitelist").define("Villager Whitelist", false);
            this.neutralAggroWhitelist = builder.comment("Treat NEUTRALAGGRO flags as whitelist").define("Neutral Aggro Whitelist", false);
            builder.pop();

            builder.comment("Settings for mod integration").push("integration");
            this.useScalingHealthMod = builder.comment("Should the scaling health mods difficulty system be used instead of this ones. (Requires scaling health mod)").define("Use Scaling Health Mod", true);
            this.usePlayerEXMod = builder.comment("If true and playerEx is installed will use the level from playerEx as difficulty").define("Use Player EX Mod", true);
            this.useLevelZMod = builder.comment("If true and LevelZ is installed will use the the total skill level from LevelZ as difficulty").define("Use LevelZ Mod", true);
            this.varySizebyPehkui = builder.comment("Using pehkui to vary the size of mobs").define("Use pehkui Mod", false);
            this.sizeMax = builder.comment("The Max scale of mobs. Range [1.0,10], default 2.0").defineInRange("Max size Multiplier", 2.0, 1.0, 10.0);
            this.sizeMin = builder.comment("The Minimum scale of mobs. Range (0,1.0), default 0.5").defineInRange("Minimum size Multiplier", 0.5, 0, 1.0);
            builder.pop();

            builder.comment("Settings regarding custom ai for mobs").push("ai");
            this.breakableBlocks = builder.comment("Whitelist for blocks, which can be actively broken. ", BreakableBlocks.use(), "Note: If you include common blocks (like grass blocks) the pathfinding can have undesirable results.").define("Block Break Whitelist", Lists.newArrayList("forge:glass", "forge:glass_panes", "minecraft:fence_gates", "forge:fence_gates", "minecraft:wooden_doors"));
            this.breakingAsBlacklist = builder.comment("Treat Block Whitelist as Blocklist").define("Breaklist as Blacklist", false);
            this.useBlockBreakSound = builder.comment("Use the block breaking sound instead of a knocking sound").define("Sound", false);
            this.breakerChance = builder.comment("Chance for a mob to be able to break blocks").defineInRange("Breaker Chance", 0.3, 0, 1);
            this.breakerInitCooldown = builder.comment("Initial cooldown for block breaking mobs").defineInRange("Breaker Initial Cooldown", 120, 0, Integer.MAX_VALUE);
            this.breakerCooldown = builder.comment("Cooldown for breaking blocks").defineInRange("Breaker Cooldown", 20, 0, Integer.MAX_VALUE);
            this.idleBreak = builder.comment("If mobs should break blocks when not chasing a target").define("Idle Break", false);
            this.stealerChance = builder.comment("Chance for a mob to be able to steal items from inventory blocks").defineInRange("Stealer Chance", 0.3, 0, 1);
            this.blackListedContainerBlocks = builder.comment("List of blocks mobs shouldn't steal from. You can also add a modid to blacklist whole mods").define("Steal Block Blacklist", new ArrayList<>());
            this.breakingItems = builder.comment("Items which will be given to mobs who can break blocks. Empty list = no items. Syntax: id;weight", "Note: Mobs can only break blocks if the tool they are holding can break the blocks").define("Breaking items", Lists.newArrayList("minecraft:diamond_pickaxe;1", "minecraft:iron_axe;2"));
            this.breakTileEntities = builder.comment("Should mobs be able to break block entities? Evaluated before the break list").define("Break BlockEntities", true);
            this.neutralAggressiv = builder.comment("Chance for neutral mobs to be aggressive").defineInRange("Neutral Aggressive Chance", 0.1, 0, 1);
            this.autoTargets = builder.comment("List for of pairs containing which mobs auto target others. Syntax is " + MobClassMapConfig.use()).define("Auto Target List", new ArrayList<>());
            this.difficultyBreak = builder.comment("Difficulty at which mobs are able to break blocks").defineInRange("Difficulty Break AI", 0D, 0, Double.MAX_VALUE);
            this.difficultySteal = builder.comment("Difficulty at which mobs are able to steal items").defineInRange("Difficulty Steal AI", 0D, 0, Double.MAX_VALUE);
            this.guardianAIChance = builder.comment("Chance for mobs to be able to summon an aquatic mount").defineInRange("Guardian Chance", 1d, 0, 1);
            this.flyAIChance = builder.comment("Chance for mobs to be able to summon a flying mount").defineInRange("Phantom Chance", 0.5, 0, 1);
            builder.pop();

            builder.comment("Configs regarding mobs spawning with equipment").push("equipment");
            this.equipmentModBlacklist = builder.comment("Blacklist items from whole mods. Add modid to prevent items from that mod being equipped. (For individual items use the equipment.json)").define("Item Blacklist", new ArrayList<>());
            this.equipmentModWhitelist = builder.comment("Use blacklist as whitelist").define("Item Whitelist", false);
            this.itemuseBlacklist = builder.comment("Blacklist for items mobs should never be able to use.", "Use as in using the item similar to players (e.g. shooting bows)").define("Item Use Blacklist", Lists.newArrayList("bigbrain:buckler"));
            this.itemuseWhitelist = builder.comment("Turn the use blacklist into a whitelist").define("Item Use Whitelist", false);
            this.entityItemConfig = builder.comment("Blacklist for specific mobs and items they shouldnt use (e.g. skeletons already use bows)", EntityItemConfig.use()).define("Entity Item Use Blacklist", Config.CommonConfig.entityItemConfig.writeToString());
            this.baseEquipChance = builder.comment("Base chance that a mob can have one piece of armor").defineInRange("Equipment Chance", 0.1, 0, 1);
            this.baseEquipChanceAdd = builder.comment("Base chance for each additional armor pieces").defineInRange("Additional Equipment Chance", 0.3, 0, 1);
            this.diffEquipAdd = builder.comment("Adds additional x*difficulty% to base equip chance").defineInRange("Equipment Addition", 0.3, 0, Double.MAX_VALUE);
            this.baseWeaponChance = builder.comment("Chance for mobs to have a weapon").defineInRange("Weapon Chance", 0.05, 0, 1);
            this.diffWeaponChance = builder.comment("Adds additional x*difficulty% to base weapon chance").defineInRange("Weapon Chance Add", 0.3, 0, Double.MAX_VALUE);
            this.baseEnchantChance = builder.comment("Base chance for each armor pieces to get enchanted").defineInRange("Enchanting Chance", 0.2, 0, 1);
            this.diffEnchantAdd = builder.comment("Adds additional x*difficulty% to base enchanting chance").defineInRange("Enchanting Addition", 0.2, 0, Double.MAX_VALUE);
            this.enchantCalc = builder.comment("Specify min and max enchanting levels according to difficulty. difficulty-minLevel-maxLevel").define("Enchanting Calc", Lists.newArrayList("0-5-10", "25-5-15", "50-10-17", "100-15-25", "200-20-30", "250-30-35"));
            this.baseItemChance = builder.comment("Chance for mobs to have an item in offhand").defineInRange("Item Equip Chance", 0.05, 0, 1);
            this.diffItemChanceAdd = builder.comment("Adds additional x*difficulty% to base item chance").defineInRange("Item Chance add", 0.2, 0, Double.MAX_VALUE);
            this.shouldDropEquip = builder.comment("Should mobs drop the armor equipped through this mod? Will not change drops if the mob obtained the armor through other means (e.g. vanilla)").define("Should drop equipment", false);
            builder.pop();

            builder.comment("Settings for attribute modifiers").push("attributes");
            this.healthIncrease = builder.comment("Health will be multiplied by 1 + difficulty*0.016*x. Set to 0 to disable").defineInRange("Health Increase Multiplier", 1.0, 0, Double.MAX_VALUE);
            this.healthMax = builder.comment("Health will be multiplied by at maximum this. Set to 0 means no limit").defineInRange("Max Health Increase", 5.0, 0, Double.MAX_VALUE);
            this.roundHP = builder.comment("Round health to the nearest x. Set to 0 to disable").defineInRange("Round HP", 0.5, 0, Double.MAX_VALUE);
            this.damageIncrease = builder.comment("Damage will be multiplied by 1 + difficulty*0.008*x. Set to 0 to disable").defineInRange("Damage Increase Multiplier", 1.0, 0, Double.MAX_VALUE);
            this.damageMax = builder.comment("Damage will be multiplied by at maximum this. Set to 0 means no limit").defineInRange("Max Damage Increase", 3.0, 0, Double.MAX_VALUE);
            this.speedIncrease = builder.comment("Speed will be increased by difficulty*0.0008*x. Set to 0 to disable").defineInRange("Speed Increase", 1.0, 0, Double.MAX_VALUE);
            this.speedMax = builder.comment("Maximum increase in speed").defineInRange("Max Speed", 0.1, 0, 1);
            this.knockbackIncrease = builder.comment("Knockback will be increased by difficulty*0.002*x. Set to 0 to disable").defineInRange("Knockback Increase", 1.0, 0, Double.MAX_VALUE);
            this.knockbackMax = builder.comment("Maximum increase in knockback").defineInRange("Max Knockback", 0.5, 0, 1);
            this.magicResIncrease = builder.comment("Magic resistance will be increased by difficulty*0.0016*x. Set to 0 to disable").defineInRange("Magic Resistance Increase", 1.0, 0, Double.MAX_VALUE);
            this.magicResMax = builder.comment("Maximum increase in magic resistance. Magic reduction is percentage").defineInRange("Max Magic Resistance", 0.4, 0, 1);
            this.projectileIncrease = builder.comment("Projectile Damage will be multiplied by 1 + difficulty*0.008*x. Set to 0 to disable").defineInRange("Projectile Damage Increase", 1.0, 0, Double.MAX_VALUE);
            this.projectileMax = builder.comment("Projectile damage will be multiplied by maximum of this").defineInRange("Max Projectile Damage", 2.0, 0, Double.MAX_VALUE);
            this.explosionIncrease = builder.comment("Explosion Damage will be multiplied by 1 + difficulty*0.003*x. Set to 0 to disable").defineInRange("Explosion Damage Increase", 1.0, 0, Double.MAX_VALUE);
            this.explosionMax = builder.comment("Explosion damage will be multiplied by maximum of this").defineInRange("Max Explosion Damage", 1.75, 0, Double.MAX_VALUE);
            builder.pop();
        }
    }

    static {
        Pair<ClientConfigVals, ForgeConfigSpec> specPair1 = new ForgeConfigSpec.Builder().configure(ClientConfigVals::new);
        clientSpec = specPair1.getRight();
        clientConf = specPair1.getLeft();

        Pair<CommonConfigVals, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(CommonConfigVals::new);
        commonSpec = specPair2.getRight();
        commonConf = specPair2.getLeft();
    }
}
