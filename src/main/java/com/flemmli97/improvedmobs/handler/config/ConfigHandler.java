package com.flemmli97.improvedmobs.handler.config;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.ConfigAnnotations;
import com.flemmli97.tenshilib.api.config.ItemWrapper;
import com.flemmli97.tenshilib.asm.ConfigUtils.Init;
import com.flemmli97.tenshilib.common.item.Util;
import com.google.common.collect.Lists;

import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;

@Config(modid = ImprovedMobs.MODID, name="improvedmobs/main", category="")
public class ConfigHandler {
	
	public static General general = new General();
	public static Debug debug = new Debug();
	public static Gui gui = new Gui();
	public static Integration integration = new Integration();
	public static AI ai = new AI();
	public static Equipment equipment = new Equipment();
	public static Attributes attributes = new Attributes();	
	
	public static class General
	{
		@Config.Comment(value="Disable/Enables the whole difficulty scaling of this mod")
		@Config.Name(value="Enable difficulty scaling")
		@Config.RequiresMcRestart
		public boolean enableDifficultyScaling=true;
		
		@Config.Comment(value="Mobs to include for the new light spawning rules")
		@Config.Name(value="Light list")
		public String[] mobListLight = new String[0];
		@Config.Comment(value="Turn light whitelist to blacklist")
		@Config.Name(value="Light Blacklist")
		public boolean mobListLightBlackList;
		@Config.Comment(value="Light level, blocks can have at max, so mobs can spawn on them")
		@Config.Name(value="Light")
		public int light=7;
		
		@Config.Comment(value="If sleeping advances difficulty by the amount of time slept")
		@Config.Name(value="Punish Time Skip")
		public boolean shouldPunishTimeSkip = true;
		
		@Config.Comment(value="Disable/Enable friendly fire for owned pets")
		@Config.Name(value="Friendly fire")
		public boolean friendlyFire;
		@Config.Comment(value="Blacklist for pet you should't be able to give armor to. Pets from mods, which have custom armor should be included here")
		@Config.Name(value="Pet Blacklist")
		public String[] petArmorBlackList=new String[0];
		@Config.Comment(value="Treat pet blacklist as whitelist")
		@Config.Name(value="Pet Whitelist")
		public boolean petWhiteList;
		
	}
	public static class Debug
	{
		@Config.Comment(value="Enable showing of entity pathfinding (might/will cause lag)")
		@Config.Name(value="Path Debugging")
		public boolean debugPath;
	}
	
	public static class Gui
	{
		@Config.Name(value="Gui X")
		public int guiX=5;
		@Config.Name(value="Gui Y")
		public int guiY=5;
		@Config.Comment(value="Textformatting codes for the display of the difficulty")
		@Config.Name(value="Difficulty color")
		public TextFormatting color = TextFormatting.DARK_PURPLE;
	}
	
	public static class Integration
	{
		@Config.Comment(value="Should the scaling health mods difficulty system be used instead of this ones (Requires scaling health mod)")
		@Config.Name(value="Use Scaling Health Mod")
		@Config.RequiresMcRestart
		public boolean useScalingHealthMod=true;
	}
	
	public static class AI
	{
		@Config.Ignore
		@ConfigAnnotations.ConfigValue(getInitTime=Init.INIT)
		@Config.Comment(value="Whitelist for blocks, which can be actively broken")
		@Config.Name(value="Block WhiteList")
		public BreakableBlocks breakableBlocks = new BreakableBlocks(new String[]{"minecraft:glass", "minecraft:stained_glass", "minecraft:fence_gate", "minecraft:wooden_door", "minecraft:spruce_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:acacia_door", "minecraft:dark_oak_door", "minecraft:glass_pane", "minecraft:stained_glass_pane"});
		@Config.Comment(value="Treat Break-Whitelist as Blacklist")
		@Config.Name(value="Block as Blacklist")
		public boolean breakingAsBlacklist;
		@Config.Comment(value="Use the block breaking sound instead of a knocking sound")
		@Config.Name(value="Sound")
		public boolean useBlockBreakSound;
		@Config.Comment(value="Chance for a mob to be able to break blocks")
		@Config.Name(value="Breaker Chance")
		public float breakerChance=0.3F;
		@Config.Comment(value="Blacklist for mobs, which can never break blocks")
		@Config.Name(value="Breaker Blacklist")
		public String[] mobListBreakBlacklist = new String[]{"minecraft:creeper"};
		@Config.Comment(value="Treat breaker blacklist as whitelist")
		@Config.Name(value="Breaker Whitelist")
		public boolean mobListBreakWhitelist;
		@Config.Ignore
		@ConfigAnnotations.ConfigValue(getInitTime=Init.POST)
		@Config.Comment(value="Item which will be given to mobs who can break blocks. Set to minecraft:air to not give any items")
		@Config.Name(value="Breaking item")
		public ItemWrapper breakingItem = new ItemWrapper(Items.DIAMOND_SWORD);
		
		//Item use config
		@Config.Comment(value="Blacklist for items given to mobs")
		@Config.Name(value="Item Blacklist")
		public String[] itemUseBlackList = new String[0];
		@Config.Comment(value="Blacklist for mobs which can't use items")
		@Config.Name(value="Item Mob-Blacklist")
		public String[] mobListUseBlacklist = new String[0];
		@Config.Comment(value="Treat item mob-blacklist as whitelist")
		@Config.Name(value="Item Mob-Whitelist")
		public boolean mobListUseWhitelist;
		
		//Ladder config
		@Config.Comment(value="Blacklist for entities which can't climb ladder")
		@Config.Name(value="Ladder Blacklist")
		public String[] mobListLadderBlacklist = new String[] {"minecraft:creeper"};
		@Config.Comment(value="Treat ladder blacklist as whitelist")
		@Config.Name(value="Ladder Whitelist")
		public boolean mobListLadderWhitelist;
		
		//Steal config
		@Config.Comment(value="Blacklist for mobs who can't steal from inventory")
		@Config.Name(value="Steal Blacklist")
		public String[] mobListStealBlacklist = new String[0];
		@Config.Comment(value="Treat steal blacklist as whitelist")
		@Config.Name(value="Steal Whitelist")
		public boolean mobListStealWhitelist;
		
		//Boat config
		@Config.Comment(value="Blacklist for mobs who can't ride a boat")
		@Config.Name(value="Boat Blacklist")
		public String[] mobListBoatBlacklist = new String[0];
		@Config.Comment(value="Treat boat blacklist as whitelist")
		@Config.Name(value="Boat Whitelist")
		public boolean mobListBoatWhitelist;
		
		@Config.Comment(value="Chance for neutral mobs to be aggressive")
		@Config.Name(value="Neutral Aggressive Chance")
		public float neutralAggressiv=0.2F;
		@Config.Comment(value="Should mobs target villagers? RIP Villagers")
		@Config.Name(value="Villager Target")
		public boolean targetVillager=true;
	}
	
	public static class Attributes
	{
		@Config.Comment(value="Blacklist for mobs which should not have their attributes modified")
		@Config.Name(value="Attribute Blacklist")
		public String[] mobAttributeBlackList = new String[0];
		@Config.Comment(value="Treat attribute blacklist as whitelist")
		@Config.Name(value="Attribute Whitelist")
		public boolean mobAttributeWhitelist;
		@Config.Comment(value="Health will be multiplied by difficulty*0.016*x. Set to 0 to disable")
		@Config.Name(value="Health Increase Multiplier")
		public float healthIncrease=1;
		@Config.Comment(value="Health will be multiplied by at maximum this. Set to 0 means no limit")
		@Config.Name(value="Max Health Increase")
		public float healthMax=5;
		@Config.Comment(value="Damage will be multiplied by difficulty*0.008*x. Set to 0 to disable")
		@Config.Name(value="Damage Increase Multiplier")
		public float damageIncrease=1;
		@Config.Comment(value="Damage will be multiplied by at maximum this. Set to 0 means no limit")
		@Config.Name(value="Max Damage Increase")
		public float damageMax=3;
		@Config.Comment(value="Speed will be increased by difficulty*0.0008*x. Set to 0 to disable")
		@Config.Name(value="Speed Increase")
		public float speedIncrease=1;
		@Config.Comment(value="Maximum increase in speed")
		@Config.Name(value="Max Speed")
		@Config.RangeDouble(min=0,max=1)
		public float speedMax=0.1F;
		@Config.Comment(value="Knockback will be increased by difficulty*0.002*x. Set to 0 to disable")
		@Config.Name(value="Knockback Increase")
		public float knockbackIncrease=1;
		@Config.Comment(value="Maximum increase in knockback")
		@Config.Name(value="Max Knockback")
		@Config.RangeDouble(min=0,max=1)
		public float knockbackMax=0.5F;
	}

	public static class Equipment
	{
		@Config.Comment(value="Blacklist for mob equipments")
		@Config.Name(value="Equipment Blacklist")
		public String[] equipmentBlacklist = new String[0];
		@Config.Comment(value="Use blacklist as whitelist")
		@Config.Name(value="Equipment Whitelist")
		public boolean armorWhitelist;
		@Config.Comment(value="Blacklist for mobs, which shouldn't get armor equiped")
		@Config.Name(value="Armor Mob-Blacklist")
		public String[] armorMobBlacklist = new String[0];
		@Config.Comment(value="Use blacklist as whitelist")
		@Config.Name(value="Armor Mob-Whitelist")
		public boolean armorMobWhiteList;
		@Config.Comment(value="Base chance that a mob can have one piece of armor")
		@Config.Name(value="Equipment Chance")
		@Config.RangeDouble(min=0,max=1)
		public float baseEquipChance=0.1F;
		@Config.Comment(value="Base chance for each additional armor pieces")
		@Config.Name(value="Additional Equipment Chance")
		@Config.RangeDouble(min=0,max=1)
		public float baseEquipChanceAdd=0.3F;
		@Config.Comment(value="Adds additional x*difficulty% to base equip chance")
		@Config.Name(value="Equipment Addition")
		public float diffEquipAdd=0.3F;
		@Config.Comment(value="Chance for mobs to have a weapon")
		@Config.Name(value="Weapon Chance")
		@Config.RangeDouble(min=0,max=1)
		public float baseWeaponChance=0.05F;
		@Config.Comment(value="Adds additional x*difficulty% to base weapon chance")
		@Config.Name(value="Weapon Chance Add")
		public float diffWeaponChance=0.4F;
		@Config.Comment(value="Base chance for each armor pieces to get enchanted")
		@Config.Name(value="Enchanting Chance")
		public float baseEnchantChance=0.2F;
		@Config.Comment(value="Adds additional x*difficulty% to base enchanting chance")
		@Config.Name(value="Enchanting Addition")
		public float diffEnchantAdd=0.3F;
		@Config.Comment(value="Chance for mobs to have an item. Higher priority than weapons")
		@Config.Name(value="Item Equip Chance")
		public float baseItemChance=0.05F;
		@Config.Comment(value="Should mobs drop the armor equipped through this mod? (Other methods e.g. through vanilla is not included)")
		@Config.Name(value="Should drop equipment")
		public boolean shouldDropEquip;
	}
	
	@Config.Ignore
	private static List<Item> boots = Lists.newArrayList();
	@Config.Ignore
	private static List<Item> legs = Lists.newArrayList();
	@Config.Ignore
	private static List<Item> chest = Lists.newArrayList();
	@Config.Ignore
	private static List<Item> helmet = Lists.newArrayList();
	@Config.Ignore
	private static List<Item> weapon = Lists.newArrayList();
	
	public static void initEquipment()
	{
		List<String> conf = Arrays.asList(ConfigHandler.equipment.equipmentBlacklist);
		for(Item item : Util.getList(EntityEquipmentSlot.FEET))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipment.armorWhitelist && conf.contains(item.getRegistryName().toString())))
				boots.add(item);
		for(Item item : Util.getList(EntityEquipmentSlot.CHEST))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipment.armorWhitelist && conf.contains(item.getRegistryName().toString())))
				chest.add(item);
		for(Item item : Util.getList(EntityEquipmentSlot.HEAD))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipment.armorWhitelist && conf.contains(item.getRegistryName().toString())))
				helmet.add(item);
		for(Item item : Util.getList(EntityEquipmentSlot.LEGS))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipment.armorWhitelist && conf.contains(item.getRegistryName().toString())))
				legs.add(item);
		for(Item item : Util.getList(EntityEquipmentSlot.MAINHAND))
			if(!conf.contains(item.getRegistryName().toString()) || (ConfigHandler.equipment.armorWhitelist && conf.contains(item.getRegistryName().toString())))
				weapon.add(item);
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
