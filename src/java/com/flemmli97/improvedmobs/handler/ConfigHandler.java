package com.flemmli97.improvedmobs.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ConfigHandler {

	public static Configuration configuration;
	public static String[] mobListLight = new String[]{};
	public static int light;
	//public static String[] modName = new String[]{};
	public static String[] armorBlacklist = new String[]{};
	public static String[] armorMobBlacklist = new String[]{};
	
	public static float equipChance;
	public static float addChance;
	public static float multiplierChance;
	public static float enchantChance;
	public static float itemChance;
	
	public static boolean friendlyFire;
	public static String[] petArmorBlackList = new String[] {};
	
	public static String[] blockBreakName = new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "minecraft:wooden_door", "minecraft:glass_pane", "minecraft:stained_glass_pane"};
	public static String[] mobListAIBlacklist = new String[]{"EntityCreeper"}; //TODO
	public static String[] itemUseBlackList = new String[] {};
	public static float breakerChance;
	public static float neutralAggressiv;
	public static boolean targetVillager;

	public static float healthIncrease;
	public static float healthMax;
	public static float damageIncrease;
	public static float damageMax;
	public static float speedIncrease;
	public static float speedMax;
	public static float knockbackIncrease;
	public static float knockbackMax;
		
	public static void loadConfig(Configuration config) {
		config.load();
		configuration = config;
		config.addCustomCategoryComment("general", "");
		config.addCustomCategoryComment("mob ai", "Settings regarding custom ai for mobs");
		config.addCustomCategoryComment("mob attributes", "Settings for attribute modifiers");
		config.addCustomCategoryComment("integration", "Settings for mod integration");

		mobListLight = config.getStringList("Mob List", "general", mobListLight, "Mobs to include for the new light spawning rules.");
		light = config.getInt("Light", "general", 7, 0, 15, "Light level, blocks can have at max, so mobs can spawn on them.");
		
		armorBlacklist = config.getStringList("Armor Blacklist", "general", armorBlacklist, "Blacklist for armor");
		armorMobBlacklist = config.getStringList("Armor-Buff Blacklist", "general", armorMobBlacklist, "Blacklist for mobs, which shouldn't get armor equiped");
		
		equipChance = config.getFloat("Equipment Chance", "general", 0.1F, 0, 1, "Chance that a mob have one piece of armor");
		addChance = config.getFloat("Equipment Chance Addition", "general", 0.3F, 0, 1, "Chance for each additional armor pieces");
		multiplierChance = config.getFloat("Difficulty Multiplier", "general", 1,0,10, "Multiplier on difficulty increase. Set to 0 to disable. At 1 increases chances for armor events by 1 % each minecraft day");
		enchantChance = config.getFloat("Enchanting Chance", "general", 0.1F, 0, 1, "Chance for each armor pieces to get enchanted. Level increases with difficulty.");
		itemChance = config.getFloat("Item Equip Chance", "general", 0.05F, 0, 1, "Chance for mobs to have an item. Level increases with difficulty.");
		
		friendlyFire = config.getBoolean("FriendlyFire", "general", false, "Disable/Enable friendly fire for owned pets.");
		petArmorBlackList = config.getStringList("Pet Blacklist", "general", petArmorBlackList, "Blacklist for pet you should't be able to give armor to");

		blockBreakName = config.getStringList("Block WhiteList", "mob ai", blockBreakName, "Whitelist for blocks, which can be actively broken.");
		mobListAIBlacklist = config.getStringList("AI Blacklist", "mob ai", mobListAIBlacklist, "Blacklist for mobs, which should not be able to use items");
		itemUseBlackList = config.getStringList("Item Blacklist", "mob ai", itemUseBlackList, "Blacklist for items given to mobs (WIP)");
		breakerChance = config.getFloat("Breaker Chance", "mob ai", 0.3F, 0, 1, "Chance for a mob to be able to break blocks."); 
		neutralAggressiv = config.getFloat("Neutral Aggressive Chance", "mob ai", 0.2F, 0, 0, "Chance for neutral mobs to be aggressive"); 
		targetVillager = config.getBoolean("Villager Target", "mob ai", true, "Should mobs target villagers? RIP Villagers");
		
		healthIncrease = getFloatConfig(config, "Health Increase Multiplier", "mob attributes", 7.0F, "Health will be multiplied by (difficulty value*this) every day. Set to 0 to disable.");
		healthMax = getFloatConfig(config, "Max Health Increase", "mob attributes", 5.0F, "Health will be multiplied by at maximum this. Set to 0 means no limit");
		damageIncrease = getFloatConfig(config, "Damage Increase Multiplier", "mob attributes", 5.0F, "Damage will be multiplied by (difficulty value*this) every day. Set to 0 to disable.");
		damageMax = getFloatConfig(config, "Max Damage Increase", "mob attributes", 3.0F, "Damage will be multiplied by at maximum this. Set to 0 means no limit. ");
		speedIncrease = getFloatConfig(config, "Speed Increase", "mob attributes", 0.25F, "Knockback will be increased by (difficulty value*this) every day. Set to 0 to disable."); 
		speedMax = config.getFloat("Max Speed", "mob attributes", 0.2F, 0, 1, "Maximum increase in speed."); 
		knockbackIncrease = getFloatConfig(config, "Knockback Increase", "mob attributes", 0.5F, "Knockback will be increased by (difficulty value*this) every day. Set to 0 to disable."); 
		knockbackMax = config.getFloat("Max Knockback", "mob attributes", 0.5F, 0, 1, "Maximum increase in knockback."); 
		config.save();
	 }
	
	private static String bootsFile = "config/improvedmobs/boots.txt";
	private static String leggingFile = "config/improvedmobs/leggings.txt";
	private static String chestplateFile = "config/improvedmobs/chestplate.txt";
	private static String helmetFile = "config/improvedmobs/helmet.txt";
	private static File[] file = new File[]{new File(bootsFile),new File(leggingFile),
			new File(chestplateFile),new File(helmetFile)};
	
	public static void write()
	{
		try
		{
			FileWriter writer1 = new FileWriter(file[0]);
			FileWriter writer2 = new FileWriter(file[1]);
			FileWriter writer3 = new FileWriter(file[2]);
			FileWriter writer4 = new FileWriter(file[3]);

			BufferedWriter[] buf = new BufferedWriter[]{new BufferedWriter(writer1),new BufferedWriter(writer2),new BufferedWriter(writer3),new BufferedWriter(writer4)};

			Iterator<Item> it = ForgeRegistries.ITEMS.iterator();
			while(it.hasNext())
			{
				Item item = it.next();			
				if(item instanceof ItemArmor)
				{				
					buf[((ItemArmor) item).armorType.getIndex()].write(item.getRegistryName().toString());
					buf[((ItemArmor) item).armorType.getIndex()].newLine();
				}
			}
			
			buf[0].close();
			buf[1].close();
			buf[2].close();
			buf[3].close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}		
	}

	/** type: 0 = boots, 1 = leggings, 2 = chestplate, 3 = helmet*/
	public static ItemStack getArmor(int type)
	{
		Random rand = new Random();
		Item armor = null;
        String line = null;
        List<String> list = new ArrayList<String>();

        try {
            FileReader read = new FileReader(file[type]);

            BufferedReader buf = new BufferedReader(read);

            while((line = buf.readLine()) != null) {
            	list.add(line);
            }
            String[] regName = list.get(rand.nextInt(list.size())).split(":");
            armor = ForgeRegistries.ITEMS.getValue(new ResourceLocation(regName[0], regName[1]));
            buf.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file");                
        }
        catch(IOException ex) {
        	ex.printStackTrace();
        }
        ItemStack stack = new ItemStack(armor);
        return stack;
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
            FMLLog.getLogger().error("Failed to get float for {}/{}", name, category, e);
        }
        return defaultValue;
	}
}