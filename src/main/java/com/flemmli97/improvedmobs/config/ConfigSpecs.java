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
    public static final CommonConfigVals commonConf;

    static class ClientConfigVals {

        public final ForgeConfigSpec.ConfigValue<Integer> guiX;
        public final ForgeConfigSpec.ConfigValue<Integer> guiY;
        public final ForgeConfigSpec.ConfigValue<TextFormatting> color;
        public final ForgeConfigSpec.ConfigValue<Double> scale;
        public final ForgeConfigSpec.BooleanValue showDifficulty;

        public ClientConfigVals(ForgeConfigSpec.Builder builder) {
            builder/*.translation("improvedmobs.gui")*/.comment("Gui Configs").push("gui");
            this.guiX = builder.define("Gui X", 5);
            this.guiY = builder.define("Gui Y", 5);
            this.color = builder.comment("Textformatting codes for the display of the difficulty").defineEnum("Difficulty color", TextFormatting.DARK_PURPLE);
            this.scale = builder.comment("Scaling of the difficulty text").define("Text Scale", 1D);
            this.showDifficulty = builder.comment("Show the the difficulty text").define("Show Difficulty", true);
            builder.pop();
        }
    }

    static class CommonConfigVals {

        //General
        public final ForgeConfigSpec.BooleanValue enableDifficultyScaling;
        public final ForgeConfigSpec.ConfigValue<Integer> difficultyDelay;
        public final ForgeConfigSpec.BooleanValue ignorePlayers;
        public final ForgeConfigSpec.ConfigValue<List<String>> mobListLight;
        public final ForgeConfigSpec.BooleanValue mobListLightBlackList;
        public final ForgeConfigSpec.IntValue light;
        public final ForgeConfigSpec.BooleanValue shouldPunishTimeSkip;
        public final ForgeConfigSpec.BooleanValue friendlyFire;
        public final ForgeConfigSpec.ConfigValue<List<String>> petArmorBlackList;
        public final ForgeConfigSpec.BooleanValue petWhiteList;

        public final ForgeConfigSpec.BooleanValue doIMDifficulty;

        //Black-WhiteList
        public final ForgeConfigSpec.ConfigValue<List<String>> entityBlacklist;
        public final ForgeConfigSpec.BooleanValue mobAttributeWhitelist;
        public final ForgeConfigSpec.BooleanValue armorMobWhitelist;
        public final ForgeConfigSpec.BooleanValue heldMobWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListBreakWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListUseWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListLadderWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListStealWhitelist;
        public final ForgeConfigSpec.BooleanValue mobListBoatWhitelist;
        public final ForgeConfigSpec.BooleanValue targetVillagerWhitelist;

        //Debug
        public final ForgeConfigSpec.BooleanValue debugPath;

        //Integration
        public final ForgeConfigSpec.BooleanValue useScalingHealthMod;
        public final ForgeConfigSpec.BooleanValue useTGunsMod;
        public final ForgeConfigSpec.BooleanValue useReforgedMod;
        public final ForgeConfigSpec.BooleanValue useCoroUtil;

        //AI
        public final ForgeConfigSpec.ConfigValue<List<String>> breakableBlocks;
        public final ForgeConfigSpec.BooleanValue breakingAsBlacklist;
        public final ForgeConfigSpec.BooleanValue useBlockBreakSound;
        public final ForgeConfigSpec.DoubleValue breakerChance;
        public final ForgeConfigSpec.DoubleValue stealerChance;
        public final ForgeConfigSpec.BooleanValue breakTileEntities;
        public final ForgeConfigSpec.ConfigValue<String> breakingItem;
        public final ForgeConfigSpec.DoubleValue neutralAggressiv;
        public final ForgeConfigSpec.ConfigValue<List<String>> autoTargets;
        public final ForgeConfigSpec.ConfigValue<Integer> repairTick;
        public final ForgeConfigSpec.ConfigValue<Double> difficultyBreak;
        public final ForgeConfigSpec.ConfigValue<Double> difficultySteal;

        //Equipment
        public final ForgeConfigSpec.ConfigValue<List<String>> equipmentModBlacklist;
        public final ForgeConfigSpec.BooleanValue equipmentModWhitelist;
        public final ForgeConfigSpec.DoubleValue baseEquipChance;
        public final ForgeConfigSpec.DoubleValue baseEquipChanceAdd;
        public final ForgeConfigSpec.ConfigValue<Double> diffEquipAdd;
        public final ForgeConfigSpec.DoubleValue baseWeaponChance;
        public final ForgeConfigSpec.ConfigValue<Double> diffWeaponChance;
        public final ForgeConfigSpec.DoubleValue baseEnchantChance;
        public final ForgeConfigSpec.ConfigValue<Double> diffEnchantAdd;
        public final ForgeConfigSpec.ConfigValue<List<String>> enchantCalc;
        public final ForgeConfigSpec.DoubleValue baseItemChance;
        public final ForgeConfigSpec.ConfigValue<Double> diffItemChanceAdd;
        public final ForgeConfigSpec.BooleanValue shouldDropEquip;

        //Attributes
        public final ForgeConfigSpec.ConfigValue<Double> healthIncrease;
        public final ForgeConfigSpec.ConfigValue<Double> healthMax;
        public final ForgeConfigSpec.ConfigValue<Double> roundHP;
        public final ForgeConfigSpec.ConfigValue<Double> damageIncrease;
        public final ForgeConfigSpec.ConfigValue<Double> damageMax;
        public final ForgeConfigSpec.ConfigValue<Double> speedIncrease;
        public final ForgeConfigSpec.DoubleValue speedMax;
        public final ForgeConfigSpec.ConfigValue<Double> knockbackIncrease;
        public final ForgeConfigSpec.ConfigValue<Double> knockbackMax;
        public final ForgeConfigSpec.ConfigValue<Double> magicResIncrease;
        public final ForgeConfigSpec.DoubleValue magicResMax;
        public final ForgeConfigSpec.ConfigValue<Double> projectileIncrease;
        public final ForgeConfigSpec.ConfigValue<Double> projectileMax;

        public CommonConfigVals(ForgeConfigSpec.Builder builder) {
            builder.comment("With default value every difficulty perk maxes out at difficulty 250")/*.translation("improvedmobs.general")*/.push("general");
            enableDifficultyScaling = builder.worldRestart().comment("Disable/Enables the whole difficulty scaling of this mod. Requires a mc restart").define("Enable difficulty scaling", true);
            difficultyDelay = builder.comment("Time in ticks for which the difficulty shouldnt increase at the beginning. One full minecraft day is 24000 ticks").define("Difficulty Delay", 0);
            ignorePlayers = builder.comment("Wether difficulty should only increase with at least one online players or not").define("Ignore Players", false);
            mobListLight = builder.comment("Mobs to include for the new light spawning rules.").define("Light list", Lists.newArrayList());
            mobListLightBlackList = builder.comment("Turn the list list whitelist to blacklist").define("Light list blacklist", false);
            light = builder.comment("Light level >= x will prevent mob spawning for defined mobs.").defineInRange("Light", 7, 0, 16);
            shouldPunishTimeSkip = builder.comment("Should punish time skipping with e.g. bed, commands? If false, difficulty will increase by 0.1 regardless of skipped time.").define("Punish Time Skip", true);
            friendlyFire = builder.comment("Disable/Enable friendly fire for owned pets.").define("FriendlyFire", false);
            petArmorBlackList = builder.comment("Blacklist for pet you should't be able to give armor to. Pets from mods, which have custom armor should be included here.").define("Pet Blacklist", Lists.newArrayList());
            petWhiteList = builder.comment("Treat pet blacklist as whitelist").define("Pet Whitelist", false);
            doIMDifficulty = builder.comment("Increase difficulty with time", "Here untill its back as a gamerule").define("Difficulty toggle", true);
            builder.pop();

            builder.comment("Black/Whitelist for various stuff").push("list");
            entityBlacklist = builder.comment("By default the mod only modifies EntityMobs. Add other entities here if you want to apply modifications to them. Usage:", EntityModifyFlagConfig.use()).define("More Entities", Lists.newArrayList("UNINITIALIZED"), List.class::isInstance);
            mobAttributeWhitelist = builder.comment("Treat ATTRIBUTES flags as whitelist").define("Attribute Whitelist", false);
            armorMobWhitelist = builder.comment("Treat ARMOR flags as whitelist").define("Armor Equip Whitelist", false);
            heldMobWhitelist = builder.comment("Treat HELDITEMS flags as whitelist").define("Held Equip Whitelist", false);
            mobListBreakWhitelist = builder.comment("Treat BLOCKBREAK flags as whitelist").define("Breaker Whitelist", false);
            mobListUseWhitelist = builder.comment("Treat USEITEM flags as whitelist").define("Item Use Whitelist", false);
            mobListLadderWhitelist = builder.comment("Treat LADDER flags as whitelist").define("Ladder Whitelist", false);
            mobListStealWhitelist = builder.comment("Treat STEAL flags as whitelist").define("Steal Whitelist", false);
            mobListBoatWhitelist = builder.comment("Treat SWIMMRIDE flags as whitelist").define("Boat Whitelist", false);
            targetVillagerWhitelist = builder.comment("Treat TARGETVILLAGER flags as whitelist").define("Villager Whitelist", false);
            builder.pop();

            builder.comment("Debugging").push("debug");
            debugPath = builder.comment("Enable showing of entity paths").define("Path Debugging", false);
            builder.pop();

            builder.comment("Settings for mod integration").push("integration");
            useScalingHealthMod = builder.comment("Should the scaling health mods difficulty system be used instead of this ones. (Requires scaling health mod)").define("Use Scaling Health Mod", true);
            useTGunsMod = builder.comment("Should mobs be able to use techguns weapons. (Requires techguns mod)").define("Use Techguns Mod", true);
            useReforgedMod = builder.comment("Should mobs be able to use weapons from the reforged mod. (Requires reforged mod)").define("Use Reforged Mod", true);
            useCoroUtil = builder.comment("Should the coroutils repair block be used. (Requires coroutils mod)").define("Use CoroUtils Mod", true);
            builder.pop();

            builder.comment("Settings regarding custom ai for mobs").push("ai");
            breakableBlocks = builder.comment("Whitelist for blocks, which can be actively broken. " + BreakableBlocks.use()).define("Block Whitelist", Lists.newArrayList("forge:glass", "forge:glass_panes", "minecraft:fence_gates", "forge:fence_gates", "minecraft:wooden_doors"));
            breakingAsBlacklist = builder.comment("Treat Block Whitelist as Blocklist").define("Block as BlacklistBlock as Blacklist", false);
            useBlockBreakSound = builder.comment("Use the block breaking sound instead of a knocking sound").define("Sound", false);
            breakerChance = builder.comment("Chance for a mob to be able to break blocks").defineInRange("Breaker Chance", 0.3, 0, 1);
            stealerChance = builder.comment("Chance for a mob to be able to steal items").defineInRange("Stealer Chance", 0.3, 0, 1);
            breakingItem = builder.comment("Item which will be given to mobs who can break blocks. Set to nothing to not give any items.").define("Breaking item", "minecraft:diamond_pickaxe");
            breakTileEntities = builder.comment("Should mobs be able to break tile entities? Evaluated before the break list").define("Break Tiles", true);
            neutralAggressiv = builder.comment("Chance for neutral mobs to be aggressive").defineInRange("Neutral Aggressive Chance", 0.2, 0, 1);
            autoTargets = builder.comment("List for of pairs containing which mobs auto target others. Syntax is " + MobClassMapConfig.use()).define("Auto Target List", Lists.newArrayList());
            repairTick = builder.comment("Delay for the coroutil repair block. Coroutil integration needs to be enabled").define("Repair Ticks", 500);
            difficultyBreak = builder.comment("Difficulty at which mobs are able to break blocks").define("Difficulty Break AI", 0D);
            difficultySteal = builder.comment("Difficulty at which mobs are able to steal items").define("Difficulty Steal AI", 0D);
            builder.pop();

            builder.comment("Configs regarding mobs spawning with equipment").push("equipment");
            equipmentModBlacklist = builder.comment("Blacklist for mods. Add modid to prevent items from that mod being used. (For individual items use the equipment.json)").define("Item Blacklist", Lists.newArrayList());
            equipmentModWhitelist = builder.comment("Use blacklist as whitelist").define("Item Whitelist", false);
            baseEquipChance = builder.comment("Base chance that a mob can have one piece of armor").defineInRange("Equipment Chance", 0.1, 0, 1);
            baseEquipChanceAdd = builder.comment("Base chance for each additional armor pieces").defineInRange("Additional Equipment Chance", 0.3, 0, 1);
            diffEquipAdd = builder.comment("Adds additional x*difficulty% to base equip chance").define("Equipment Addition", 0.3);
            baseWeaponChance = builder.comment("Chance for mobs to have a weapon").defineInRange("Weapon Chance", 0.05, 0, 1);
            diffWeaponChance = builder.comment("Adds additional x*difficulty% to base weapon chance").define("Weapon Chance Add", 0.3);
            baseEnchantChance = builder.comment("Base chance for each armor pieces to get enchanted").defineInRange("Enchanting Chance", 0.2, 0, 1);
            diffEnchantAdd = builder.comment("Adds additional x*difficulty% to base enchanting chance").define("Enchanting Addition", 0.2);
            enchantCalc = builder.comment("Specify min and max enchanting levels according to difficulty. difficulty-minLevel-maxLevel").define("Enchanting Calc", Lists.newArrayList("0-5-10","25-5-15", "50-10-17", "100-15-25", "200-20-30", "250-30-35"));
            baseItemChance = builder.comment("Chance for mobs to have an item in offhand").defineInRange("Item Equip Chance", 0.05, 0, 1);
            diffItemChanceAdd = builder.comment("Adds additional x*difficulty% to base item chance").define("Item Chance add", 0.2);
            shouldDropEquip = builder.comment("Should mobs drop the armor equipped through this mod? (Other methods e.g. through vanilla is not included)").define("Should drop equipment", false);
            builder.pop();

            builder.comment("Settings for attribute modifiers").push("attributes");
            healthIncrease = builder.comment("Health will be multiplied by difficulty*0.016*x. Set to 0 to disable").define("Health Increase Multiplier", 1.0);
            healthMax = builder.comment("Health will be multiplied by at maximum this. Set to 0 means no limit").define("Max Health Increase", 5.0);
            roundHP = builder.comment("Round health to the nearest x. Set to 0 to disable").define("Round HP", 0.5);
            damageIncrease = builder.comment("Damage will be multiplied by difficulty*0.008*x. Set to 0 to disable").define("Damage Increase Multiplier", 1.0);
            damageMax = builder.comment("Damage will be multiplied by at maximum this. Set to 0 means no limit").define("Max Damage Increase", 3.0);
            speedIncrease = builder.comment("Speed will be increased by difficulty*0.0008*x. Set to 0 to disable").define("Speed Increase", 1.0);
            speedMax = builder.comment("Maximum increase in speed").defineInRange("Max Speed", 0.1, 0, 1);
            knockbackIncrease = builder.comment("Knockback will be increased by difficulty*0.002*x. Set to 0 to disable").define("Knockback Increase", 1.0);
            knockbackMax = builder.comment("Maximum increase in knockback").define("Max Knockback", 0.5);
            magicResIncrease = builder.comment("Magic resistance will be increased by difficulty*0.0016*x. Set to 0 to disable").define("Magic Resistance Increase", 1.0);
            magicResMax = builder.comment("Maximum increase in magic resistance. Magic reduction is percentage").defineInRange("Max Magic Resistance", 0.4, 0, 1);
            projectileIncrease = builder.comment("Projectile Damage will be multiplied by 1+difficulty*0.008*x. Set to 0 to disable").define("Projectile Damage Increase", 1.0);
            projectileMax = builder.comment("Projectile damage will be multiplied by maximum of this").define("Max Projectile Damage", 2.0);
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
