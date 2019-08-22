package com.flemmli97.improvedmobs.handler.config;

import java.util.List;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public class BreakableBlocks implements IConfigArrayValue{
	
	private List<String> blocks = Lists.newArrayList();
	private String[] configString;
	public BreakableBlocks(String[] strings)
	{	
		this.readFromString(strings);
	}
	
	public boolean canBreak(Block block)
	{
		if(ConfigHandler.breakingAsBlacklist)
		{
			return !blocks.contains(block.getRegistryName().toString());
		}
		return blocks.contains(block.getRegistryName().toString());
	}

	@Override
	public IConfigArrayValue readFromString(String[] arr) {
		this.blocks.clear();
		this.configString=arr;
		for(String s : arr)
		{
			List<String> blackList = Lists.newArrayList();
			if(s.startsWith("!"))
			{
				s = s.substring(1);
				if(s.contains(":"))
					blackList.add(s);
				else
				{
					Class<?> clss = null;
					try 
					{
						clss = Class.forName("net.minecraft.block."+s);
					} 
					catch (ClassNotFoundException e) 
					{
						try 
						{
							clss = Class.forName(s);
						} catch (ClassNotFoundException e1) {
							ImprovedMobs.logger.error("Couldn't find class for "+s);
						}
					}
					if(clss!=null)
						for(Block block : ForgeRegistries.BLOCKS)
						{
							if(clss.isInstance(block))
								blackList.add(block.getRegistryName().toString());
						}
					else
					{
						OreDictionary.getOres(s).forEach(stack->{
							Block block = Block.getBlockFromItem(stack.getItem());
							if(block!=Blocks.AIR)
								blackList.add(block.getRegistryName().toString());
						});;
					}
				}
			}
			else
			{
				if(s.contains(":"))
					this.blocks.add(s);
				else
				{
					Class<?> clss = null;
					try 
					{
						clss = Class.forName("net.minecraft.block."+s);
					} 
					catch (ClassNotFoundException e) 
					{
						try 
						{
							clss = Class.forName(s);
						} catch (ClassNotFoundException e1) {
							ImprovedMobs.logger.error("Couldn't find class for "+s);
						}
					}
					if(clss!=null)
						for(Block block : ForgeRegistries.BLOCKS)
						{
							if(clss.isInstance(block))
								this.blocks.add(block.getRegistryName().toString());
						}
					else
					{
						OreDictionary.getOres(s).forEach(stack->{
							Block block = Block.getBlockFromItem(stack.getItem());
							if(block!=Blocks.AIR)
								this.blocks.add(block.getRegistryName().toString());
						});;
					}
				}
			}
			this.blocks.removeAll(blackList);
		}
		System.out.println(this.blocks);
		return this;
	}

	@Override
	public String[] writeToString() {
		return this.configString;
	}

	@Override
	public String usage() {
		return "Usage: <registry name;classname;oredict> put \"!\" infront to exclude blocks";
	}

}
