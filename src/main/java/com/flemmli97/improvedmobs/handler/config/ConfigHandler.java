package com.flemmli97.improvedmobs.handler.config;

import java.io.File;
import java.util.List;

import com.flemmli97.tenshilib.api.config.ItemWrapper;
import com.flemmli97.tenshilib.common.config.ConfigUtils;
import com.flemmli97.tenshilib.common.config.ConfigUtils.LoadState;
import com.google.common.collect.Lists;

import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

public class ConfigHandler {
	
	public static Configuration config;
	
	//General
	public static boolean enableDifficultyScaling;
	public static String[] mobListLight;
	public static boolean mobListLightBlackList;
	public static int light;
	public static boolean shouldPunishTimeSkip;
	public static boolean friendlyFire;
	public static String[] petArmorBlackList;
	public static boolean petWhiteList;
	
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

	//Gui
	public static int guiX;
	public static int guiY;
	public static TextFormatting color;
	public static float scale;
	
	//Integration
	public static boolean useScalingHealthMod=true;
	public static boolean useTGunsMod=true;
	public static boolean useReforgedMod=true;

	//AI
	public static BreakableBlocks breakableBlocks = new BreakableBlocks(new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "BlockDoor","!minecraft:iron_door", "minecraft:glass_pane", "minecraft:stained_glass_pane"});
	public static boolean breakingAsBlacklist;
	public static boolean useBlockBreakSound;
	public static float breakerChance;
	public static ItemWrapper breakingItem = new ItemWrapper(Items.DIAMOND_PICKAXE);
	public static float neutralAggressiv;
	public static MobClassMapConfig autoTargets = new MobClassMapConfig(new String[0]);
	
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
	public static float baseItemChanceAdd;
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
	
	public static void load(LoadState state)
	{
		if(config==null)
		{
			config=new Configuration(new File(Loader.instance().getConfigDir(), "improvedmobs/main.cfg"));
			config.load();
		}
		ConfigCategory general = config.getCategory("general");
		general.setLanguageKey("improvedmobs.general");
		general.setComment("With default value every difficulty perk maxes out at difficulty 250");
		Property prop = config.get("general", "Enable difficulty scaling", true);
		prop.setComment("Disable/Enables the whole difficulty scaling of this mod");
		enableDifficultyScaling = prop.setRequiresMcRestart(true).getBoolean();
		mobListLight = config.getStringList("Light list", "general", new String[0], "Mobs to include for the new light spawning rules.");
		mobListLightBlackList = config.getBoolean("Light list blacklist", "general", false, "Turn the list list whitelist to blacklist");
		light = config.getInt("Light", "general", 7, 0, 15, "Light level, blocks can have at max, so mobs can spawn on them.");
		shouldPunishTimeSkip = config.getBoolean("Punish Time Skip", "general", true, "Should punish time skipping with e.g. bed, commands? If false, difficulty will increase by 0.1 regardless of skipped time.");
		friendlyFire = config.getBoolean("FriendlyFire", "general", false, "Disable/Enable friendly fire for owned pets.");
		petArmorBlackList = config.getStringList("Pet Blacklist", "general", new String[0], "Blacklist for pet you should't be able to give armor to. Pets from mods, which have custom armor should be included here.");
		petWhiteList = config.getBoolean("Pet Whitelist", "general", false, "Treat pet blacklist as whitelist");

		ConfigCategory list = config.getCategory("list");
		list.setLanguageKey("improvedmobs.list");
		list.setComment("Black/Whitelist for various stuff");
		if(state==LoadState.SYNC||state==LoadState.POSTINIT) {
			if(entityBlacklist==null)
	    		entityBlacklist = new EntityModifyFlagConfig();
	    	entityBlacklist.readFromString(config.getStringList("More Entities", "list", entityBlacklist.writeToString(), "By default the mod only modifies EntityMobs. Add other entities here if you want to apply modifications to them. Usage: " + entityBlacklist.usage()));
		}
		mobAttributeWhitelist = config.getBoolean("Attribute Whitelist", "list", false, "Treat ATTRIBUTES flags as whitelist");
		armorMobWhitelist = config.getBoolean("Armor Equip Whitelist", "list", false, "Treat ARMOR flags as whitelist");
		heldMobWhitelist = config.getBoolean("Held Equip Whitelist", "list", false, "Treat HELDITEMS flags as whitelist");
		mobListBreakWhitelist = config.getBoolean("Breaker Whitelist", "list", false, "Treat BLOCKBREAK flags as whitelist");
		mobListUseWhitelist = config.getBoolean("Item Use Whitelist", "list", false, "Treat USEITEM flags as whitelist");
		mobListLadderWhitelist = config.getBoolean("Ladder Whitelist", "list", false, "Treat LADDER flags as whitelist");
		mobListStealWhitelist = config.getBoolean("Steal Whitelist", "list", false, "Treat STEAL flags as whitelist");
		mobListBoatWhitelist = config.getBoolean("Boat Whitelist", "list", false, "Treat SWIMMRIDE flags as whitelist");
		targetVillagerWhitelist = config.getBoolean("Villager Whitelist", "list", false, "Treat TARGETVILLAGER flags as whitelist");

		ConfigCategory debug = config.getCategory("debug");
		debug.setLanguageKey("improvedmobs.debug");
		debug.setComment("Debugging");
		debugPath = config.getBoolean("Path Debugging", "debug", false, "Enable showing of entity paths (might/will cause lag)");
		
		ConfigCategory gui = config.getCategory("gui");
		gui.setLanguageKey("improvedmobs.gui");
		gui.setComment("Gui Configs");
		guiX = config.get("gui", "Gui X", 5).getInt();
		guiY = config.get("gui", "Gui Y", 5).getInt();
		List<String> lst = Lists.newArrayList();
        for(TextFormatting form : TextFormatting.values())
        	if(form.isColor())
        		lst.add(form.name());
		color = ConfigUtils.getEnumVal(config, "gui", "Difficulty color", "Textformatting codes for the display of the difficulty", TextFormatting.DARK_PURPLE, lst);
		scale = config.getFloat("Text Scale", "gui", 1, 0, 5, "Scaling of the difficulty text"); 

		
		ConfigCategory integration = config.getCategory("integration");
		integration.setLanguageKey("improvedmobs.integration");
		integration.setComment("Settings for mod integration");
		prop = config.get("integration", "Use Scaling Health Mod", useScalingHealthMod);
		prop.setComment("Should the scaling health mods difficulty system be used instead of this ones. (Requires scaling health mod)");
		if(state==LoadState.PREINIT)
			useScalingHealthMod = prop.setRequiresMcRestart(true).getBoolean();
		prop = config.get("integration", "Use Techguns Mod", useTGunsMod);
		prop.setComment("Should mobs be able to use techguns weapons. (Requires techguns mod)");
		if(state==LoadState.PREINIT)
			useTGunsMod = prop.setRequiresMcRestart(true).getBoolean();
		prop = config.get("integration", "Use Reforged Mod", useTGunsMod);
		prop.setComment("Should mobs be able to use weapons from the reforged mod. (Requires reforged mod)");
		if(state==LoadState.PREINIT)
			useReforgedMod = prop.setRequiresMcRestart(true).getBoolean();
		
		ConfigCategory ai = config.getCategory("ai");
		ai.setLanguageKey("improvedmobs.ai");
		ai.setComment("Settings regarding custom ai for mobs");
		if(state==LoadState.SYNC||state==LoadState.POSTINIT)
			breakableBlocks.readFromString(config.getStringList("Block Whitelist", "ai", breakableBlocks.writeToString(), "Whitelist for blocks, which can be actively broken. " + breakableBlocks.usage()));
		breakingAsBlacklist = config.getBoolean("Block as Blacklist", "ai", false, "Treat Block Whitelist as Blocklist");
		useBlockBreakSound = config.getBoolean("Sound", "ai", false, "Use the block breaking sound instead of a knocking sound");
		breakerChance = config.getFloat("Breaker Chance", "ai", 0.3F, 0, 1, "Chance for a mob to be able to break blocks."); 
		if(state==LoadState.SYNC||state==LoadState.POSTINIT)
			breakingItem.readFromString(config.getString("Breaking item", "ai", "minecraft:diamond_pickaxe", "Item which will be given to mobs who can break blocks. Set to nothing to not give any items."));
		neutralAggressiv = config.getFloat("Neutral Aggressive Chance", "ai", 0.2F, 0, 1, "Chance for neutral mobs to be aggressive"); 
		if(state==LoadState.SYNC||state==LoadState.POSTINIT)
			autoTargets.readFromString(config.getStringList("Auto Target List", "ai", autoTargets.writeToString(), "List for of pairs containing which mobs auto target others. Syntax is " + autoTargets.usage()+" where the class name is the target"));
		
		ConfigCategory equipment = config.getCategory("equipment");
		equipment.setLanguageKey("improvedmobs.equipment");
		equipment.setComment("Configs regarding mobs spawning with equipment");
		equipmentModBlacklist = config.getStringList("Item Blacklist", "equipment", equipmentModBlacklist, "Blacklist for mods. Add modid to prevent items from that mod being used. (For individual items use the equipment.json)");
		equipmentModWhitelist = config.getBoolean("Item Whitelist", "equipment", false, "Use blacklist as whitelist");
		baseEquipChance = config.getFloat("Equipment Chance", "equipment", 0.1F, 0, 1, "Base chance that a mob can have one piece of armor");
		baseEquipChanceAdd = config.getFloat("Additional Equipment Chance", "equipment", 0.3F, 0, 1, "Base chance for each additional armor pieces");
		diffEquipAdd = ConfigUtils.getFloatConfig(config, "Equipment Addition", "equipment", 0.3F,  "Adds additional x*difficulty% to base equip chance");
		baseWeaponChance = config.getFloat("Weapon Chance", "equipment", 0.05F, 0, 1, "Chance for mobs to have a weapon.");
		diffWeaponChance = ConfigUtils.getFloatConfig(config, "Weapon Chance Add", "equipment", 0.3F, "Adds additional x*difficulty% to base weapon chance");
		baseEnchantChance = config.getFloat("Enchanting Chance", "equipment", 0.2F, 0, 1, "Base chance for each armor pieces to get enchanted.");
		diffEnchantAdd = ConfigUtils.getFloatConfig(config, "Enchanting Addition", "equipment", 0.3F,  "Adds additional x*difficulty% to base enchanting chance");
		baseItemChance = config.getFloat("Item Equip Chance", "equipment", 0.05F, 0, 1, "Chance for mobs to have an item in offhand.");
		baseItemChance = ConfigUtils.getFloatConfig(config, "Item Chance add", "equipment", 0.2F, "Adds additional x*difficulty% to base item chance");
		shouldDropEquip = config.getBoolean("Should drop equipment", "equipment", false, "Should mobs drop the armor equipped through this mod? (Other methods e.g. through vanilla is not included)");

		ConfigCategory attributes = config.getCategory("attributes");
		attributes.setLanguageKey("improvedmobs.attributes");
		attributes.setComment("Settings for attribute modifiers");
		healthIncrease = ConfigUtils.getFloatConfig(config, "Health Increase Multiplier", "attributes", 1.0F, "Health will be multiplied by difficulty*0.016*x. Set to 0 to disable.");
		healthMax = ConfigUtils.getFloatConfig(config, "Max Health Increase", "attributes", 5.0F, "Health will be multiplied by at maximum this. Set to 0 means no limit");
	    roundHP = ConfigUtils.getFloatConfig(config, "Round HP", "attributes", 0.5F, "Round health to the nearest x. Set to 0 to disable.");
		damageIncrease = ConfigUtils.getFloatConfig(config, "Damage Increase Multiplier", "attributes", 1.0F, "Damage will be multiplied by difficulty*0.008*x. Set to 0 to disable.");
		damageMax = ConfigUtils.getFloatConfig(config, "Max Damage Increase", "attributes", 3.0F, "Damage will be multiplied by at maximum this. Set to 0 means no limit. ");
		speedIncrease = ConfigUtils.getFloatConfig(config, "Speed Increase", "attributes", 1.0F, "Speed will be increased by difficulty*0.0008*x. Set to 0 to disable."); 
		speedMax = config.getFloat("Max Speed", "attributes", 0.1F, 0, 1, "Maximum increase in speed."); 
		knockbackIncrease = ConfigUtils.getFloatConfig(config, "Knockback Increase", "attributes", 1.0F, "Knockback will be increased by difficulty*0.002*x. Set to 0 to disable."); 
		knockbackMax = config.getFloat("Max Knockback", "attributes", 0.5F, 0, 1, "Maximum increase in knockback."); 
		magicResIncrease = ConfigUtils.getFloatConfig(config, "Magic Resistance Increase", "attributes", 1.0F, "Magic resistance will be increased by difficulty*0.0016*x. Set to 0 to disable."); 
		magicResMax = config.getFloat("Max Magic Resistance", "attributes", 0.4F, 0, 1, "Maximum increase in magic resistance. Magic reduction is percentage"); 
		projectileIncrease = ConfigUtils.getFloatConfig(config, "Projectile Damage Increase", "attributes", 1.0F, "Projectile Damage will be multiplied by 1+difficulty*0.008*x. Set to 0 to disable."); 
		projectileMax = ConfigUtils.getFloatConfig(config, "Max Projectile Damage", "attributes", 2F, "Projectile damage will be multiplied by maximum of this."); 
		
		if(state==LoadState.SYNC||state==LoadState.POSTINIT) {
		    config.save();
			EquipmentList.initEquip(config.getConfigFile().getParentFile());
		}
	}
}
