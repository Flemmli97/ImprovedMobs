package com.flemmli97.improvedmobs.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ConfigHandler {
	
	public static String[] mobListLight = new String[]{};
	public static boolean mobListLightBlackList;
	public static int light;
	//public static String[] modName = new String[]{};
	public static boolean enableDifficultyScaling;
	
	//Equipment config
	public static String[] armorMobBlacklist = new String[]{};
	public static boolean armorMobWhiteList;
	
	public static String[] armorBlacklist = new String[]{};
	public static boolean armorWhitelist;
	
	public static float baseEquipChance;
	public static float baseEquipChanceAdd;
	public static float diffEquipAdd;
	public static float baseWeaponChance;
	public static float diffWeaponChance;

	public static float baseItemChance;
	public static float baseEnchantChance;
	public static float diffEnchantAdd;
	public static boolean shouldDropEquip;
	
	public static boolean friendlyFire;
	public static String[] petArmorBlackList = new String[] {};
	public static boolean petWhiteList;
	
	//Block breaking config
	private static String[] blockBreakName = new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "minecraft:wooden_door", "minecraft:spruce_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:acacia_door", "minecraft:dark_oak_door",   "minecraft:glass_pane", "minecraft:stained_glass_pane"};
	public static boolean blockAsBlacklist;
	public static boolean useBlockBreakSound;
	public static float breakerChance;
	public static String[] mobListBreakBlacklist = new String[]{"EntityCreeper"};
	public static boolean mobListBreakWhitelist;
	public static String breakingItemReg;
	public static Item breakingItem;
	//Item use config
	public static String[] itemUseBlackList = new String[0];
	public static String[] mobListUseBlacklist = new String[0];
	public static boolean mobListUseWhitelist;
	//Ladder config
	public static String[] mobListLadderBlacklist = new String[0];
	public static boolean mobListLadderWhitelist;
	//Steal config
	public static String[] mobListStealBlacklist = new String[0];
	public static boolean mobListStealWhitelist;
	//Boat config
	public static String[] mobListBoatBlacklist = new String[0];
	public static boolean mobListBoatWhitelist;
	
	public static float neutralAggressiv;
	public static boolean targetVillager;
	
	public static boolean shouldPunishTimeSkip;
	public static float healthIncrease;
	public static float healthMax;
	public static float damageIncrease;
	public static float damageMax;
	public static float speedIncrease;
	public static float speedMax;
	public static float knockbackIncrease;
	public static float knockbackMax;
	public static String[] mobAttributeBlackList = new String[0];
	public static boolean mobAttributeWhitelist;
	
	public static int guiX, guiY;
	public static TextFormatting color;
	
	public static boolean debuggingPath;
	
	public static List<String> breakListNames;
	public static List<BlockClassPredicate> breakListClass;
		
	public static boolean useScalingHealthMod;

	public static void loadConfig(Configuration config) {
		config.load();
		config.addCustomCategoryComment("general", "With default value every difficulty perk maxes out at difficulty 250");
		config.addCustomCategoryComment("equipment", "Configs regarding mobs spawning with equipment");

		config.addCustomCategoryComment("mob ai", "Settings regarding custom ai for mobs");
		config.addCustomCategoryComment("mob attributes", "Settings for attribute modifiers");
		config.addCustomCategoryComment("integration", "Settings for mod integration.");
		config.addCustomCategoryComment("gui", "GUI");
		config.addCustomCategoryComment("debug", "Debugging");
		
		enableDifficultyScaling = config.getBoolean("Enable difficulty scaling", "general", true, "Disable/Enables the whole difficulty scaling of this mod");
		mobListLight = config.getStringList("Light list", "general", mobListLight, "Mobs to include for the new light spawning rules.");
		mobListLightBlackList = config.getBoolean("Light list blacklist", "general", false, "Turn the list list whitelist to blacklist");

		useScalingHealthMod = config.getBoolean("Use Scaling Health Mod", "integration", true, "Should the scaling health mods difficulty system be used instead of this ones. (Requires scaling health mod)");
		
		light = config.getInt("Light", "general", 7, 0, 15, "Light level, blocks can have at max, so mobs can spawn on them.");
		shouldPunishTimeSkip = config.getBoolean("Punish Time Skip", "general", true, "Should punish time skipping with e.g. bed, commands? If false, difficulty will increase by 0.1 regardless of skipped time.");
		friendlyFire = config.getBoolean("FriendlyFire", "general", false, "Disable/Enable friendly fire for owned pets.");
		petArmorBlackList = config.getStringList("Pet Blacklist", "general", petArmorBlackList, "Blacklist for pet you should't be able to give armor to. Pets from mods, which have custom armor should be included here.");
		petWhiteList = config.getBoolean("Pet Whitelist", "general", false, "Treat pet blacklist as whitelist");

		armorBlacklist = config.getStringList("Armor Blacklist", "equipment", armorBlacklist, "Blacklist for armor");
		armorWhitelist = config.getBoolean("Armor Whitelist", "equipment", false, "Use blacklist as whitelist");
		armorMobBlacklist = config.getStringList("Armor Mob-Blacklist", "equipment", armorMobBlacklist, "Blacklist for mobs, which shouldn't get armor equiped");
		armorMobWhiteList = config.getBoolean("Armor Mob-Whitelist", "equipment", false, "Use blacklist as whitelist");
		baseEquipChance = config.getFloat("Equipment Chance", "equipment", 0.1F, 0, 1, "Base chance that a mob can have one piece of armor");
		baseEquipChanceAdd = config.getFloat("Additional Equipment Chance", "equipment", 0.3F, 0, 1, "Base chance for each additional armor pieces");
		diffEquipAdd = getFloatConfig(config, "Equipment Addition", "equipment", 0.3F,  "Adds additional x*difficulty% to base equip chance");
		baseWeaponChance = config.getFloat("Weapon Chance", "equipment", 0.05F, 0, 1, "Chance for mobs to have a weapon.");
		diffWeaponChance = config.getFloat("Weapon Chance Add", "equipment", 0.4F, 0, 1, "Adds additional x*difficulty% to base weapon chance");

		baseEnchantChance = config.getFloat("Enchanting Chance", "equipment", 0.2F, 0, 1, "Base chance for each armor pieces to get enchanted.");
		diffEnchantAdd = getFloatConfig(config, "Enchanting Addition", "equipment", 0.3F,  "Adds additional x*difficulty% to base enchanting chance");
		baseItemChance = config.getFloat("Item Equip Chance", "equipment", 0.05F, 0, 1, "Chance for mobs to have an item. Higher priority than weapons");

		shouldDropEquip = config.getBoolean("Should drop equipment", "equipment", false, "Should mobs drop the armor equipped through this mod? (Other methods e.g. through vanilla is not included)");

		blockBreakName = config.getStringList("Block WhiteList", "mob ai", blockBreakName, "Whitelist for blocks, which can be actively broken. Use +Classname to include all blocks of that type and \"!\" to exclude a specific block e.g. \"+BlockDoor!minecraft:iron_door!minecraft:spruce_door\" will make all blocks extending BlockDoor except iron doors and spruce doors breakable.");
		blockAsBlacklist = config.getBoolean("Block as Blacklist", "mob ai", false, "Treat Break-Whitelist as Blacklist");
		useBlockBreakSound = config.getBoolean("Sound", "mob ai", false, "Use the block breaking sound instead of a knocking sound");
		mobListBreakBlacklist = config.getStringList("AI Blacklist", "mob ai", mobListBreakBlacklist, "Blacklist for mobs, which can never break blocks");
		mobListBreakWhitelist = config.getBoolean("Mob as Whitelist", "mob ai", false, "Use the AI Blacklist as Whitelist");
		breakerChance = config.getFloat("Breaker Chance", "mob ai", 0.3F, 0, 1, "Chance for a mob to be able to break blocks."); 
		breakingItemReg = config.getString("Breaking item", "mob ai", "minecraft:diamond_pickaxe", "Item which will be given to mobs who can break blocks. Set to nothing to not give any items.");
		itemUseBlackList = config.getStringList("Item Blacklist", "mob ai", itemUseBlackList, "Blacklist for items given to mobs.");
		mobListUseBlacklist = config.getStringList("Item Mob-Blacklist", "mob ai", mobListUseBlacklist, "Blacklist for mobs which can't use items");
		mobListUseWhitelist = config.getBoolean("Item Mob-Whitelist", "mob ai", false, "Treat Item Mob-Blacklist as Whitelist");

		mobListLadderBlacklist = config.getStringList("Ladder Blacklist", "mob ai", mobListLadderBlacklist, "Blacklist for entities which can't climb ladder");
		mobListLadderWhitelist = config.getBoolean("Ladder Whitelist", "mob ai", false, "Treat Ladder Blacklist as Whitelist");

		mobListStealBlacklist = config.getStringList("Steal Blacklist", "mob ai", mobListStealBlacklist, "Blacklist for mobs who can't steal from inventory");
		mobListStealWhitelist = config.getBoolean("Steal Whitelist", "mob ai", false, "Treat Steal Blacklist as Whitelist");
		
		mobListBoatBlacklist = config.getStringList("Boat Blacklist", "mob ai", mobListBoatBlacklist, "Blacklist for mobs who can't ride a boat");
		mobListBoatWhitelist = config.getBoolean("Boat Whitelist", "mob ai", false, "Treat Boat Blacklist as Whitelist");
		
		neutralAggressiv = config.getFloat("Neutral Aggressive Chance", "mob ai", 0.2F, 0, 1, "Chance for neutral mobs to be aggressive"); 
		targetVillager = config.getBoolean("Villager Target", "mob ai", true, "Should mobs target villagers? RIP Villagers");
		
		mobAttributeBlackList = config.getStringList("Attribute Blacklist", "mob attributes", mobAttributeBlackList, "Blacklist for mobs which should not have their attributes modified");
		mobAttributeWhitelist = config.getBoolean("Attribute Whitelist", "mob attributes", false, "Treat Attribute Blacklist as Whitelist");
		healthIncrease = getFloatConfig(config, "Health Increase Multiplier", "mob attributes", 1.0F, "Health will be multiplied by difficulty*0.016*x. Set to 0 to disable.");
		healthMax = getFloatConfig(config, "Max Health Increase", "mob attributes", 5.0F, "Health will be multiplied by at maximum this. Set to 0 means no limit");
		damageIncrease = getFloatConfig(config, "Damage Increase Multiplier", "mob attributes", 1.0F, "Damage will be multiplied by difficulty*0.008*x. Set to 0 to disable.");
		damageMax = getFloatConfig(config, "Max Damage Increase", "mob attributes", 3.0F, "Damage will be multiplied by at maximum this. Set to 0 means no limit. ");
		speedIncrease = getFloatConfig(config, "Speed Increase", "mob attributes", 1.0F, "Speed will be increased by difficulty*0.0008*x. Set to 0 to disable."); 
		speedMax = config.getFloat("Max Speed", "mob attributes", 0.1F, 0, 1, "Maximum increase in speed."); 
		knockbackIncrease = getFloatConfig(config, "Knockback Increase", "mob attributes", 1.0F, "Knockback will be increased by difficulty*0.002*x. Set to 0 to disable."); 
		knockbackMax = config.getFloat("Max Knockback", "mob attributes", 0.5F, 0, 1, "Maximum increase in knockback."); 
		
		guiPosition(config, "Gui Position", "gui", "TOPLEFT", "Position of the difficulty value on screen. Valid values: TOPLEFT, TOPMIDDLE, TOPRIGHT, MIDDLELEFT, MIDDLERIGHT, BOTTOMLEFT, BOTTOMMIDDLE, BOTTOMRIGHT");
		color = TextFormatting.getValueByName(config.getString("Difficulty color", "gui", "dark_purple","Color for the text of the difficulty. Uses minecraft textformatting codes", new String[] {"black","dark_blue","dark_green","dark_aqua","dark_red","dark_purple","gold","grey","dark_grey","blue","green","aqua","red","light_purple","yellow","white"}));
		
		debuggingPath = config.getBoolean("Path Debugging", "debug", false, "Enable showing of entity paths (might/will cause lag)");

		List<String> registryNames = new LinkedList<String>();
		List<BlockClassPredicate> blockInstances= new LinkedList<BlockClassPredicate>();
		for(String s : blockBreakName)
		{
			if(s.startsWith("+"))
			{
				BlockClassPredicate pred = BlockClassPredicate.fromString(s.substring(1));
				if(pred!=null)
					blockInstances.add(pred);
			}
			else registryNames.add(s);
		}
		breakListNames = ImmutableList.copyOf(registryNames);
		breakListClass =  ImmutableList.copyOf(blockInstances);
		config.save();
	 }
	
	private static String bootsFile = "config/improvedmobs/boots.txt";
	private static String leggingFile = "config/improvedmobs/leggings.txt";
	private static String chestplateFile = "config/improvedmobs/chestplate.txt";
	private static String helmetFile = "config/improvedmobs/helmet.txt";
	private static String weaponsFile = "config/improvedmobs/weapons.txt";

	private static File[] file = new File[]{new File(bootsFile),new File(leggingFile),
			new File(chestplateFile),new File(helmetFile),new File(weaponsFile)};
	
	
	private static List<String> boots = new ArrayList<String>();
	private static List<String> legs = new ArrayList<String>();
	private static List<String> chest = new ArrayList<String>();
	private static List<String> helmet = new ArrayList<String>();
	private static List<String> weapons = new ArrayList<String>();

	public static void initEquipment()
	{
		/*
		 * Delete old files, now unused.
		 */
		for(File f : file)
		{
			if(f.exists())
				f.delete();
		}
		
		Iterator<Item> it = ForgeRegistries.ITEMS.iterator();
		while(it.hasNext())
		{
			Item item = it.next();
			
			if(item instanceof ItemArmor)
			{				
				switch(((ItemArmor) item).armorType.getIndex())
				{
					case 0:
						if(!GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist) || (ConfigHandler.armorWhitelist && GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist)))
				        {
							boots.add(item.getRegistryName().toString());
				        }
						break;
					case 1:
						if(!GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist) || (ConfigHandler.armorWhitelist && GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist)))
				        {
							legs.add(item.getRegistryName().toString());
				        }
						break;
					case 2:
						if(!GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist) || (ConfigHandler.armorWhitelist && GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist)))
				        {
							chest.add(item.getRegistryName().toString());
				        }
						break;
					case 3:
						if(!GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist) || (ConfigHandler.armorWhitelist && GeneralHelperMethods.itemInList(item, ConfigHandler.armorBlacklist)))
				        {
							helmet.add(item.getRegistryName().toString());
				        }
						break;
				}
			}
			else if(item instanceof ItemSword || item instanceof ItemAxe)
			{
				weapons.add(item.getRegistryName().toString());
			}
		}
	}

	/** type: 0 = boots, 1 = leggings, 2 = chestplate, 3 = helmet, 4 = weapons*/
	public static ItemStack getEquipment(int type)
	{
		Random rand = new Random();
        String item = "";
        switch(type)
        {
	        case 0:
					item = boots.get(rand.nextInt(boots.size()));
				break;
			case 1:
					item = legs.get(rand.nextInt(legs.size()));
				break;
			case 2:
					item = chest.get(rand.nextInt(chest.size()));
				break;
			case 3:
					item = helmet.get(rand.nextInt(helmet.size()));
				break;
        }
        ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item)));
        return stack;
	}
	
	public static ItemStack randomWeapon()
	{
		return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(weapons.get(new Random().nextInt(weapons.size())))));
	}
	
	private static float getFloatConfig(Configuration config, String name, String category, float defaultValue, String comment)
	{
		Property prop = config.get(category, name, Float.toString(defaultValue), name);
        prop.setLanguageKey(name);
        prop.setComment(comment + "[default: " + defaultValue + "]");
        try
        {
            float parseFloat = Float.parseFloat(prop.getString());
            return Math.max(0, parseFloat);
        }
        catch (Exception e)
        {
            FMLLog.log.error("Failed to get float for {}/{}", name, category, e);
        }
        return defaultValue;
	}
	
	private static String[] valid = new String[] {"TOPLEFT", "TOPMIDDLE", "TOPRIGHT", "MIDDLELEFT", "MIDDLERIGHT", "BOTTOMLEFT", "BOTTOMMIDDLE", "BOTTOMRIGHT"};
	private static String guiPosition(Configuration config, String name, String category, String defaultValue, String comment)
	{
		Property prop = config.get(category, name, defaultValue, name);
        prop.setLanguageKey(name);
        prop.setComment(comment + "[default: " + defaultValue + "]");
        String p = prop.getString();
        for(int i = 0; i < valid.length;i++)
        		if(valid[i].equals(p))
        		{
        			if(i<3)
        			{
        				guiX = i;
        				guiY = 0;
        			}
    				else if(i==3)
    				{
    					guiX = 0;
    					guiY = 1;
    				}
    				else if(i==4)
    				{
    					guiX = 2;
    					guiY = 1;
    				}
    				else 
    				{
    					guiX = i-5;
    					guiY = 2;
    				}
        			return p;
        		}
        guiX = 0;guiY = 0;
		return defaultValue;
	}
	
	public static class BlockClassPredicate
	{
		private Class<?> clss;
		private String[] exclude;
		public BlockClassPredicate(Class<?> clss, String[] strings)
		{	
			this.clss=clss;
			this.exclude=strings;
		}
		
		public boolean matches(Block block)
		{
			if(this.exclude!=null)
				for(String s : this.exclude)
					if(block.getRegistryName().toString().equals(s))
					{
						return false;
					}
			return clss.isInstance(block);
		}
		
		public static BlockClassPredicate fromString(String string)
		{
			String[] part = string.split("!");
			Class<?> clss = null;
			try 
			{
				clss = Class.forName("net.minecraft.block."+part[0]);
			} catch (ClassNotFoundException e) 
			{
				try 
				{
					clss = Class.forName(part[0]);
				} catch (ClassNotFoundException e1) {
					ImprovedMobs.logger.error("Couldn't find class for "+part[0]);
				}
			}
			return new BlockClassPredicate(clss, part.length>1?Arrays.copyOfRange(part, 1,  part.length):null);
		}
	}
}