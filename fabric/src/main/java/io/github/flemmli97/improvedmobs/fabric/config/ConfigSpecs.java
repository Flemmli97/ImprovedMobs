package io.github.flemmli97.improvedmobs.fabric.config;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.config.BreakableBlocks;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.EntityItemConfig;
import io.github.flemmli97.improvedmobs.config.EntityModifyFlagConfig;
import io.github.flemmli97.improvedmobs.config.MobClassMapConfig;
import io.github.flemmli97.tenshilib.common.config.JsonConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class ConfigSpecs {

    public static JsonConfig<ClientConfigVals> clientConfig;
    public static JsonConfig<CommonConfigVals> commonConfig;

    public static void initClientConfig() {
        clientConfig = new JsonConfig<>(FabricLoader.getInstance().getConfigDir().resolve("improvedmobs").resolve("client.json").toFile(),
                ConfigSpecs.ClientConfigVals.class, new ConfigSpecs.ClientConfigVals());
    }

    public static void initCommonConfig() {
        commonConfig = new JsonConfig<>(FabricLoader.getInstance().getConfigDir().resolve("improvedmobs").resolve("common.json").toFile(),
                ConfigSpecs.CommonConfigVals.class, new ConfigSpecs.CommonConfigVals());
    }

    public static class ClientConfigVals {

        public final CommentedVal<Integer> guiX;
        public final CommentedVal<Integer> guiY;
        public final CommentedVal<ChatFormatting> color;
        public final CommentedVal<Double> scale;
        public final CommentedVal<Boolean> showDifficulty;

        public ClientConfigVals() {
            CommentedVal.Builder builder = new CommentedVal.Builder();
            this.guiX = builder.define("Gui X", 5);
            this.guiY = builder.define("Gui Y", 5);
            this.color = builder.comment("Textformatting codes for the display of the difficulty").define("Difficulty color", ChatFormatting.DARK_PURPLE);
            this.scale = builder.comment("Scaling of the difficulty text").define("Text Scale", 1D);
            this.showDifficulty = builder.comment("Show the the difficulty text").define("Show Difficulty", true);
        }
    }

    public static class CommonConfigVals {

        //General
        public CommentedVal<Boolean> enableDifficultyScaling;
        public CommentedVal<Integer> difficultyDelay;
        public CommentedVal<List<String>> increaseHandler;
        public CommentedVal<Boolean> ignorePlayers;
        public CommentedVal<Boolean> shouldPunishTimeSkip;
        public CommentedVal<Boolean> friendlyFire;
        public CommentedVal<List<String>> petArmorBlackList;
        public CommentedVal<Boolean> petWhiteList;
        public CommentedVal<Boolean> doIMDifficulty;

        //Black-WhiteList
        public CommentedVal<List<String>> flagBlacklist;
        public CommentedVal<List<String>> entityBlacklist;
        public CommentedVal<Boolean> mobAttributeWhitelist;
        public CommentedVal<Boolean> armorMobWhitelist;
        public CommentedVal<Boolean> heldMobWhitelist;
        public CommentedVal<Boolean> mobListBreakWhitelist;
        public CommentedVal<Boolean> mobListUseWhitelist;
        public CommentedVal<Boolean> mobListLadderWhitelist;
        public CommentedVal<Boolean> mobListStealWhitelist;
        public CommentedVal<Boolean> mobListBoatWhitelist;
        public CommentedVal<Boolean> targetVillagerWhitelist;

        //Debug
        public CommentedVal<Boolean> debugPath;

        //Integration
        public CommentedVal<Boolean> useScalingHealthMod;
        public CommentedVal<Boolean> useTGunsMod;
        public CommentedVal<Boolean> useReforgedMod;
        public CommentedVal<Boolean> useCoroUtil;

        //AI
        public CommentedVal<List<String>> breakableBlocks;
        public CommentedVal<Boolean> breakingAsBlacklist;
        public CommentedVal<Boolean> useBlockBreakSound;
        public CommentedVal<Double> breakerChance;
        public CommentedVal<Integer> breakerInitCooldown;
        public CommentedVal<Integer> breakerCooldown;
        public CommentedVal<Double> stealerChance;
        public CommentedVal<Boolean> breakTileEntities;
        public CommentedVal<List<String>> breakingItems;
        public CommentedVal<Double> neutralAggressiv;
        public CommentedVal<List<String>> autoTargets;
        public CommentedVal<Integer> repairTick;
        public CommentedVal<Double> difficultyBreak;
        public CommentedVal<Double> difficultySteal;

        //Equipment
        public CommentedVal<List<String>> equipmentModBlacklist;
        public CommentedVal<Boolean> equipmentModWhitelist;
        public CommentedVal<List<String>> itemuseBlacklist;
        public CommentedVal<Boolean> itemuseWhitelist;
        public CommentedVal<List<String>> entityItemConfig;

        public CommentedVal<Double> baseEquipChance;
        public CommentedVal<Double> baseEquipChanceAdd;
        public CommentedVal<Double> diffEquipAdd;
        public CommentedVal<Double> baseWeaponChance;
        public CommentedVal<Double> diffWeaponChance;
        public CommentedVal<Double> baseEnchantChance;
        public CommentedVal<Double> diffEnchantAdd;
        public CommentedVal<List<String>> enchantCalc;
        public CommentedVal<Double> baseItemChance;
        public CommentedVal<Double> diffItemChanceAdd;
        public CommentedVal<Boolean> shouldDropEquip;

        //Attributes
        public CommentedVal<Double> healthIncrease;
        public CommentedVal<Double> healthMax;
        public CommentedVal<Double> roundHP;
        public CommentedVal<Double> damageIncrease;
        public CommentedVal<Double> damageMax;
        public CommentedVal<Double> speedIncrease;
        public CommentedVal<Double> speedMax;
        public CommentedVal<Double> knockbackIncrease;
        public CommentedVal<Double> knockbackMax;
        public CommentedVal<Double> magicResIncrease;
        public CommentedVal<Double> magicResMax;
        public CommentedVal<Double> projectileIncrease;
        public CommentedVal<Double> projectileMax;

        public CommonConfigVals() {
            CommentedVal.Builder builder = new CommentedVal.Builder();
            //builder.comment("With default value every difficulty perk maxes out at difficulty 250").push("general");
            this.enableDifficultyScaling = builder.comment("Disable/Enables the whole difficulty scaling of this mod. Requires a mc restart").define("Enable difficulty scaling", true);
            this.difficultyDelay = builder.comment("Time in ticks for which the difficulty shouldnt increase at the beginning. One full minecraft day is 24000 ticks").define("Difficulty Delay", 0);
            this.increaseHandler = builder.comment("Handles increase in difficulty regarding current difficulty.", "Format is <minimum current difficulty>-<increase every 2400 ticks>", "Example [\"0-0.01\",\"10-0.1\",\"30-0\"]", "So the difficulty increases by 0.01 every 2400 ticks (->0.1 per mc day) till it reaches a difficulty of 10.", "Then it increases by 1 per mc day till it reaches 30 and then stops.").define("Difficulty Increase", Lists.newArrayList("0-0.1"));
            this.ignorePlayers = builder.comment("Wether difficulty should only increase with at least one online players or not").define("Ignore Players", false);
            this.shouldPunishTimeSkip = builder.comment("Should punish time skipping with e.g. bed, commands? If false, difficulty will increase by 0.1 regardless of skipped time.").define("Punish Time Skip", true);
            this.friendlyFire = builder.comment("Disable/Enable friendly fire for owned pets.").define("FriendlyFire", false);
            this.petArmorBlackList = builder.comment("Blacklist for pet you should't be able to give armor to. Pets from mods, which have custom armor should be included here.").define("Pet Blacklist", new ArrayList<>());
            this.petWhiteList = builder.comment("Treat pet blacklist as whitelist").define("Pet Whitelist", false);
            this.doIMDifficulty = builder.comment("Increase difficulty with time", "Here untill its back as a gamerule").define("Difficulty toggle", true);

            //builder.comment("Black/Whitelist for various stuff").push("list");
            this.entityBlacklist = builder.comment("By default the mod only modifies EntityMobs. Add other entities here if you want to apply modifications to them. Usage:", EntityModifyFlagConfig.use()).define("More Entities", Lists.newArrayList("UNINITIALIZED"));
            this.flagBlacklist = builder.comment("Put the above flags here to completly disable them.").define("Flag Blacklist", new ArrayList<>());
            this.mobAttributeWhitelist = builder.comment("Treat ATTRIBUTES flags as whitelist").define("Attribute Whitelist", false);
            this.armorMobWhitelist = builder.comment("Treat ARMOR flags as whitelist").define("Armor Equip Whitelist", false);
            this.heldMobWhitelist = builder.comment("Treat HELDITEMS flags as whitelist").define("Held Equip Whitelist", false);
            this.mobListBreakWhitelist = builder.comment("Treat BLOCKBREAK flags as whitelist").define("Breaker Whitelist", false);
            this.mobListUseWhitelist = builder.comment("Treat USEITEM flags as whitelist").define("Item Use Whitelist", false);
            this.mobListLadderWhitelist = builder.comment("Treat LADDER flags as whitelist").define("Ladder Whitelist", false);
            this.mobListStealWhitelist = builder.comment("Treat STEAL flags as whitelist").define("Steal Whitelist", false);
            this.mobListBoatWhitelist = builder.comment("Treat SWIMMRIDE flags as whitelist").define("Boat Whitelist", false);
            this.targetVillagerWhitelist = builder.comment("Treat TARGETVILLAGER flags as whitelist").define("Villager Whitelist", false);

            //builder.comment("Debugging").push("debug");
            this.debugPath = builder.comment("Enable showing of entity paths").define("Path Debugging", false);

            //builder.comment("Settings for mod integration").push("integration");
            this.useScalingHealthMod = builder.comment("Should the scaling health mods difficulty system be used instead of this ones. (Requires scaling health mod)").define("Use Scaling Health Mod", true);
            this.useTGunsMod = builder.comment("Should mobs be able to use techguns weapons. (Requires techguns mod)").define("Use Techguns Mod", true);
            this.useReforgedMod = builder.comment("Should mobs be able to use weapons from the reforged mod. (Requires reforged mod)").define("Use Reforged Mod", true);
            this.useCoroUtil = builder.comment("Should the coroutils repair block be used. (Requires coroutils mod)").define("Use CoroUtils Mod", true);

            //builder.comment("Settings regarding custom ai for mobs").push("ai");
            this.breakableBlocks = builder.comment("Whitelist for blocks, which can be actively broken. " + BreakableBlocks.use(), "If you includery common blocks (like grass blocks) the pathfinding will be a bit strange").define("Block Whitelist", Lists.newArrayList("minecraft:fence_gates", "minecraft:wooden_doors", "c:glass", "c:glass_panes", "minecraft:glass"));
            this.breakingAsBlacklist = builder.comment("Treat Block Whitelist as Blocklist").define("Block as Blacklist", false);
            this.useBlockBreakSound = builder.comment("Use the block breaking sound instead of a knocking sound").define("Sound", false);
            this.breakerChance = builder.comment("Chance for a mob to be able to break blocks").define("Breaker Chance", 0.3);
            this.breakerInitCooldown = builder.comment("Initial cooldown for block breaking mobs").define("Breaker Initial Cooldown", 120);
            this.breakerCooldown = builder.comment("Cooldown for breaking blocks").define("Breaker Cooldown", 20);
            this.stealerChance = builder.comment("Chance for a mob to be able to steal items").define("Stealer Chance", 0.3);
            this.breakingItems = builder.comment("Items which will be given to mobs who can break blocks. Empty list = no items. Syntax: id;weight").define("Breaking items", Lists.newArrayList("minecraft:diamond_pickaxe;1", "minecraft:iron_axe;2"));
            this.breakTileEntities = builder.comment("Should mobs be able to break tile entities? Evaluated before the break list").define("Break Tiles", true);
            this.neutralAggressiv = builder.comment("Chance for neutral mobs to be aggressive").define("Neutral Aggressive Chance", 0.2);
            this.autoTargets = builder.comment("List for of pairs containing which mobs auto target others. Syntax is " + MobClassMapConfig.use()).define("Auto Target List", new ArrayList<>());
            this.repairTick = builder.comment("Delay for the coroutil repair block. Coroutil integration needs to be enabled").define("Repair Ticks", 500);
            this.difficultyBreak = builder.comment("Difficulty at which mobs are able to break blocks").define("Difficulty Break AI", 0D);
            this.difficultySteal = builder.comment("Difficulty at which mobs are able to steal items").define("Difficulty Steal AI", 0D);

            //builder.comment("Configs regarding mobs spawning with equipment").push("equipment");
            this.equipmentModBlacklist = builder.comment("Blacklist for mods. Add modid to prevent items from that mod being equipped. (For individual items use the equipment.json)").define("Item Blacklist", new ArrayList<>());
            this.equipmentModWhitelist = builder.comment("Use blacklist as whitelist").define("Item Whitelist", false);
            this.itemuseBlacklist = builder.comment("Blacklist for items mobs should never be able to use.", "Use as in using the item similar to players (e.g. shooting bows)").define("Item Use Blacklist", Lists.newArrayList("bigbrain:buckler"));
            this.itemuseWhitelist = builder.comment("Turn the use blacklist into a whitelist").define("Item Use Whitelist", false);
            this.entityItemConfig = builder.comment("Blacklist for specific mobs and items they shouldnt use (e.g. skeletons already use bows)", EntityItemConfig.use()).define("Entity Item Use Blacklist", Config.CommonConfig.entityItemConfig.writeToString());
            this.baseEquipChance = builder.comment("Base chance that a mob can have one piece of armor").define("Equipment Chance", 0.1);
            this.baseEquipChanceAdd = builder.comment("Base chance for each additional armor pieces").define("Additional Equipment Chance", 0.3);
            this.diffEquipAdd = builder.comment("Adds additional x*difficulty% to base equip chance").define("Equipment Addition", 0.3);
            this.baseWeaponChance = builder.comment("Chance for mobs to have a weapon").define("Weapon Chance", 0.05);
            this.diffWeaponChance = builder.comment("Adds additional x*difficulty% to base weapon chance").define("Weapon Chance Add", 0.3);
            this.baseEnchantChance = builder.comment("Base chance for each armor pieces to get enchanted").define("Enchanting Chance", 0.2);
            this.diffEnchantAdd = builder.comment("Adds additional x*difficulty% to base enchanting chance").define("Enchanting Addition", 0.2);
            this.enchantCalc = builder.comment("Specify min and max enchanting levels according to difficulty. difficulty-minLevel-maxLevel").define("Enchanting Calc", Lists.newArrayList("0-5-10", "25-5-15", "50-10-17", "100-15-25", "200-20-30", "250-30-35"));
            this.baseItemChance = builder.comment("Chance for mobs to have an item in offhand").define("Item Equip Chance", 0.05);
            this.diffItemChanceAdd = builder.comment("Adds additional x*difficulty% to base item chance").define("Item Chance add", 0.2);
            this.shouldDropEquip = builder.comment("Should mobs drop the armor equipped through this mod? (Other methods e.g. through vanilla is not included)").define("Should drop equipment", false);

            //builder.comment("Settings for attribute modifiers").push("attributes");
            this.healthIncrease = builder.comment("Health will be multiplied by difficulty*0.016*x. Set to 0 to disable").define("Health Increase Multiplier", 1.0);
            this.healthMax = builder.comment("Health will be multiplied by at maximum this. Set to 0 means no limit").define("Max Health Increase", 5.0);
            this.roundHP = builder.comment("Round health to the nearest x. Set to 0 to disable").define("Round HP", 0.5);
            this.damageIncrease = builder.comment("Damage will be multiplied by difficulty*0.008*x. Set to 0 to disable").define("Damage Increase Multiplier", 1.0);
            this.damageMax = builder.comment("Damage will be multiplied by at maximum this. Set to 0 means no limit").define("Max Damage Increase", 3.0);
            this.speedIncrease = builder.comment("Speed will be increased by difficulty*0.0008*x. Set to 0 to disable").define("Speed Increase", 1.0);
            this.speedMax = builder.comment("Maximum increase in speed").define("Max Speed", 0.1);
            this.knockbackIncrease = builder.comment("Knockback will be increased by difficulty*0.002*x. Set to 0 to disable").define("Knockback Increase", 1.0);
            this.knockbackMax = builder.comment("Maximum increase in knockback").define("Max Knockback", 0.5);
            this.magicResIncrease = builder.comment("Magic resistance will be increased by difficulty*0.0016*x. Set to 0 to disable").define("Magic Resistance Increase", 1.0);
            this.magicResMax = builder.comment("Maximum increase in magic resistance. Magic reduction is percentage").define("Max Magic Resistance", 0.4);
            this.projectileIncrease = builder.comment("Projectile Damage will be multiplied by 1+difficulty*0.008*x. Set to 0 to disable").define("Projectile Damage Increase", 1.0);
            this.projectileMax = builder.comment("Projectile damage will be multiplied by maximum of this").define("Max Projectile Damage", 2.0);
        }
    }
}
