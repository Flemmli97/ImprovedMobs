package com.flemmli97.improvedmobs.handler.config;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;

public class BreakableBlocks implements IConfigArrayValue{
	
	private List<Pair<Class<?>,List<String>>> clss = Lists.newArrayList();
	private List<String> blocks = Lists.newArrayList();
	
	public BreakableBlocks(String[] strings)
	{	
		this.readFromString(strings);
	}
	
	public boolean canBreak(Block block)
	{
		if(ConfigHandler.ai.breakingAsBlacklist)
		{
			for(Pair<Class<?>, List<String>> pair : clss)
			{
				if(!pair.getLeft().isInstance(block) || pair.getRight().contains(block.getRegistryName().toString()))
				{
					return false;
				}
			}
			return !blocks.contains(block.getRegistryName().toString());
		}
		for(Pair<Class<?>, List<String>> pair : clss)
		{
			if(!pair.getLeft().isInstance(block) || pair.getRight().contains(block.getRegistryName().toString()))
			{
				return false;
			}
		}
		return blocks.contains(block.getRegistryName().toString());
	}

	@Override
	public IConfigArrayValue readFromString(String[] arr) {
		this.clss.clear();
		this.blocks.clear();
		for(String s : arr)
		{
			if(s.startsWith("+"))
			{
				String[] part = s.substring(1).split("!");
				String[] subParts = part.length>1?Arrays.copyOfRange(part, 1,  part.length):new String[0];
				Class<?> clss = null;
				try 
				{
					clss = Class.forName("net.minecraft.block."+part[0]);
					this.clss.add(Pair.of(clss, Lists.newArrayList(subParts)));
				} catch (ClassNotFoundException e) 
				{
					try 
					{
						clss = Class.forName(part[0]);
						this.clss.add(Pair.of(clss, Lists.newArrayList(subParts)));
					} catch (ClassNotFoundException e1) {
						ImprovedMobs.logger.error("Couldn't find class for "+part[0]);
					}
				}
			}
			else this.blocks.add(s);
		}
		return this;
	}

	@Override
	public String[] writeToString() {
		List<String> list = this.blocks;
		for(Pair<Class<?>, List<String>> e : this.clss)
		{
			String clss ="+"+ e.getLeft().getName().replaceAll("net.minecraft.block.", "");
			for(String s : e.getRight())
				clss+="!"+s;
			list.add(clss);
		}
		return list.toArray(new String[0]);
	}

	@Override
	public String usage() {
		return "<registry name; \"+\"+CLASSNAME+(!excluded as registry names)";
	}

}
