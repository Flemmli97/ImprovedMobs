package com.flemmli97.improvedmobs.handler.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.silvercatcher.reforged.items.weapons.ItemBlowGun;
import org.silvercatcher.reforged.items.weapons.ItemJavelin;

import com.flemmli97.improvedmobs.handler.helper.AIUseHelper;
import com.flemmli97.tenshilib.api.config.ItemWrapper;
import com.flemmli97.tenshilib.common.config.ConfigUtils;
import com.flemmli97.tenshilib.common.config.ConfigUtils.LoadState;
import com.flemmli97.tenshilib.common.item.ItemUtil;
import com.google.common.collect.Lists;

import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import techguns.items.guns.GenericGun;

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
	
	//Debug
	public static boolean debugPath;

	//Gui
	public static int guiX;
	public static int guiY;
	public static TextFormatting color;
	
	//Integration
	public static boolean useScalingHealthMod=true;
	public static boolean useTGunsMod=true;
	public static boolean useReforgedMod=true;

	//AI
	public static BreakableBlocks breakableBlocks = new BreakableBlocks(new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "BlockDoor","!minecraft:iron_door", "minecraft:glass_pane", "minecraft:stained_glass_pane"});
	public static boolean breakingAsBlacklist;
	public static boolean useBlockBreakSound;
	public static float breakerChance;
	public static String[] mobListBreakBlacklist;
	public static boolean mobListBreakWhitelist;
	public static ItemWrapper breakingItem = new ItemWrapper(Items.DIAMOND_PICKAXE);
	public static String[] itemUseBlackList;
	public static String[] mobListUseBlacklist;
	public static boolean mobListUseWhitelist;
	public static String[] mobListLadderBlacklist;
	public static boolean mobListLadderWhitelist;
	public static String[] mobListStealBlacklist;
	public static boolean mobListStealWhitelist;
	public static String[] mobListBoatBlacklist;
	public static boolean mobListBoatWhitelist;
	public static float neutralAggressiv;
	public static boolean targetVillager;
	public static MobClassMapConfig autoTargets = new MobClassMapConfig(new String[] {});
	
	//Equipment
	public static String[] equipmentBlacklist = new String[] {"techguns:nucleardeathray", "techguns:grenadelauncher","techguns:tfg","techguns:guidedmissilelauncher","techguns:rocketlauncher"};
	public static boolean equipmentWhitelist;
	public static String[] armorMobBlacklist;
	public static boolean armorMobWhiteList;
	public static float baseEquipChance;
	public static float baseEquipChanceAdd;
	public static float diffEquipAdd;
	public static float baseWeaponChance;
	public static float diffWeaponChance;
	public static float baseEnchantChance;
	public static float diffEnchantAdd;
	public static float baseItemChance;
	public static boolean shouldDropEquip;
	
	//Attributes
	public static String[] mobAttributeBlackList;
	public static boolean mobAttributeWhitelist;
	public static float healthIncrease;
	public static float healthMax;
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
		mobListBreakBlacklist = config.getStringList("Breaker Blacklist", "ai", new String[]{"minecraft:creeper"}, "Blacklist for mobs, which can never break blocks");
		mobListBreakWhitelist = config.getBoolean("Breaker Whitelist", "ai", false, "Use the AI Blacklist as Whitelist");
		if(state==LoadState.SYNC||state==LoadState.POSTINIT)
			breakingItem.readFromString(config.getString("Breaking item", "ai", "minecraft:diamond_pickaxe", "Item which will be given to mobs who can break blocks. Set to nothing to not give any items."));
		itemUseBlackList = config.getStringList("Item Blacklist", "ai", new String[0], "Blacklist for items given to mobs. Use @[modid] to add all items from that mod");
		mobListUseBlacklist = config.getStringList("Item Mob-Blacklist", "ai", new String[0], "Blacklist for mobs which can't use items");
		mobListUseWhitelist = config.getBoolean("Item Mob-Whitelist", "ai", false, "Treat Item Mob-Blacklist as Whitelist");
		mobListLadderBlacklist = config.getStringList("Ladder Blacklist", "ai", new String[] {"minecraft:creeper"}, "Blacklist for entities which can't climb ladder");
		mobListLadderWhitelist = config.getBoolean("Ladder Whitelist", "ai", false, "Treat Ladder Blacklist as Whitelist");
		mobListStealBlacklist = config.getStringList("Steal Blacklist", "ai", new String[0], "Blacklist for mobs who can't steal from inventory");
		mobListStealWhitelist = config.getBoolean("Steal Whitelist", "ai", false, "Treat Steal Blacklist as Whitelist");
		mobListBoatBlacklist = config.getStringList("Boat Blacklist", "ai", new String[0], "Blacklist for mobs who can't ride a boat");
		mobListBoatWhitelist = config.getBoolean("Boat Whitelist", "ai", false, "Treat Boat Blacklist as Whitelist");
		neutralAggressiv = config.getFloat("Neutral Aggressive Chance", "ai", 0.2F, 0, 1, "Chance for neutral mobs to be aggressive"); 
		targetVillager = config.getBoolean("Villager Target", "ai", true, "Should mobs target villagers? RIP Villagers");
		if(state==LoadState.SYNC||state==LoadState.POSTINIT)
			autoTargets.readFromString(config.getStringList("Auto Target List", "ai", autoTargets.writeToString(), "List for of pairs containing which mobs auto target others. Syntax is " + autoTargets.usage()+" where the class name is the target"));
		ConfigCategory equipment = config.getCategory("equipment");
		equipment.setLanguageKey("improvedmobs.equipment");
		equipment.setComment("Configs regarding mobs spawning with equipment");
		equipmentBlacklist = config.getStringList("Equipment Blacklist", "equipment", equipmentBlacklist, "Blacklist for mob equipments");
		equipmentWhitelist = config.getBoolean("Equipment Whitelist", "equipment", false, "Use blacklist as whitelist");
		armorMobBlacklist = config.getStringList("Armor Mob-Blacklist", "equipment", new String[0], "Blacklist for mobs, which shouldn't get armor equiped");
		armorMobWhiteList = config.getBoolean("Armor Mob-Whitelist", "equipment", false, "Use blacklist as whitelist");
		baseEquipChance = config.getFloat("Equipment Chance", "equipment", 0.1F, 0, 1, "Base chance that a mob can have one piece of armor");
		baseEquipChanceAdd = config.getFloat("Additional Equipment Chance", "equipment", 0.3F, 0, 1, "Base chance for each additional armor pieces");
		diffEquipAdd = ConfigUtils.getFloatConfig(config, "Equipment Addition", "equipment", 0.3F,  "Adds additional x*difficulty% to base equip chance");
		baseWeaponChance = config.getFloat("Weapon Chance", "equipment", 0.05F, 0, 1, "Chance for mobs to have a weapon.");
		diffWeaponChance = ConfigUtils.getFloatConfig(config, "Weapon Chance Add", "equipment", 0.3F, "Adds additional x*difficulty% to base weapon chance");
		baseEnchantChance = config.getFloat("Enchanting Chance", "equipment", 0.2F, 0, 1, "Base chance for each armor pieces to get enchanted.");
		diffEnchantAdd = ConfigUtils.getFloatConfig(config, "Enchanting Addition", "equipment", 0.3F,  "Adds additional x*difficulty% to base enchanting chance");
		baseItemChance = config.getFloat("Item Equip Chance", "equipment", 0.05F, 0, 1, "Chance for mobs to have an item. Higher priority than weapons");
		shouldDropEquip = config.getBoolean("Should drop equipment", "equipment", false, "Should mobs drop the armor equipped through this mod? (Other methods e.g. through vanilla is not included)");

		ConfigCategory attributes = config.getCategory("attributes");
		attributes.setLanguageKey("improvedmobs.attributes");
		attributes.setComment("Settings for attribute modifiers");
		mobAttributeBlackList = config.getStringList("Attribute Blacklist", "attributes", new String[0], "Blacklist for mobs which should not have their attributes modified");
		mobAttributeWhitelist = config.getBoolean("Attribute Whitelist", "attributes", false, "Treat Attribute Blacklist as Whitelist");
		healthIncrease = ConfigUtils.getFloatConfig(config, "Health Increase Multiplier", "attributes", 1.0F, "Health will be multiplied by difficulty*0.016*x. Set to 0 to disable.");
		healthMax = ConfigUtils.getFloatConfig(config, "Max Health Increase", "attributes", 5.0F, "Health will be multiplied by at maximum this. Set to 0 means no limit");
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
		
		config.save();
	}
	
	private static List<Item> boots = Lists.newArrayList();
	private static List<Item> legs = Lists.newArrayList();
	private static List<Item> chest = Lists.newArrayList();
	private static List<Item> helmet = Lists.newArrayList();
	private static List<Item> weapon = Lists.newArrayList();
	
	public static void initEquipment()
	{
		List<String> conf = Arrays.asList(ConfigHandler.equipmentBlacklist);
		for(Item item : ItemUtil.getList(EntityEquipmentSlot.FEET))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipmentWhitelist && conf.contains(item.getRegistryName().toString())))
				boots.add(item);
		for(Item item : ItemUtil.getList(EntityEquipmentSlot.CHEST))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipmentWhitelist && conf.contains(item.getRegistryName().toString())))
				chest.add(item);
		for(Item item : ItemUtil.getList(EntityEquipmentSlot.HEAD))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipmentWhitelist && conf.contains(item.getRegistryName().toString())))
				helmet.add(item);
		for(Item item : ItemUtil.getList(EntityEquipmentSlot.LEGS))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipmentWhitelist && conf.contains(item.getRegistryName().toString())))
				legs.add(item);
		for(Item item : ItemUtil.getList(EntityEquipmentSlot.MAINHAND))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipmentWhitelist && conf.contains(item.getRegistryName().toString())))
				weapon.add(item);
		ForgeRegistries.ITEMS.forEach(item->{
			if((!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipmentWhitelist && conf.contains(item.getRegistryName().toString()))))
			{
				if(ConfigHandler.useTGunsMod)
					if(item instanceof GenericGun)
						weapon.add(item);
				if(ConfigHandler.useReforgedMod)
					if(item instanceof ItemBlowGun || item instanceof ItemJavelin)
						weapon.add(item);
				if(item instanceof ItemBow)
					weapon.add(item);
			}
		});
		if(ConfigHandler.useReforgedMod)
			AIUseHelper.initReforgedStuff();
	}
	
	public static ItemStack randomWeapon()
	{
		return new ItemStack(weapon.get(new Random().nextInt(weapon.size())));
	}
	
	@SuppressWarnings("incomplete-switch")
	public static ItemStack getEquipment(EntityEquipmentSlot slot)
	{
		Random rand = new Random();
		try
		{
	        switch(slot)
	        {
				case CHEST: return new ItemStack(chest.get(rand.nextInt(chest.size())));
				case FEET: return new ItemStack(boots.get(rand.nextInt(boots.size())));
				case HEAD: return new ItemStack(helmet.get(rand.nextInt(helmet.size())));
				case LEGS: return new ItemStack(legs.get(rand.nextInt(legs.size())));
	        }
		}
		//In case list is empty
		catch(IllegalArgumentException e){}
		return ItemStack.EMPTY;
	}
}
