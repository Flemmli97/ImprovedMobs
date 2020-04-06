package com.flemmli97.improvedmobs.handler.config;

import java.util.List;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.google.common.collect.Lists;

import CoroUtil.block.BlockRepairingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public class BreakableBlocks implements IConfigArrayValue<BreakableBlocks> {

	private List<String> blocks = Lists.newArrayList();
	private String[] configString;

	public BreakableBlocks(String[] strings) {
		this.readFromString(strings);
	}

	public boolean canBreak(IBlockState state) {
		if(state.getMaterial() == Material.AIR || (ConfigHandler.useCoroUtil && state.getBlock() instanceof BlockRepairingBlock))
			return false;
		if(!ConfigHandler.breakTileEntities && state.getBlock().hasTileEntity(state))
			return false;
		if(ConfigHandler.breakingAsBlacklist){
			return !blocks.contains(state.getBlock().getRegistryName().toString());
		}
		return blocks.contains(state.getBlock().getRegistryName().toString());
	}

	@Override
	public BreakableBlocks readFromString(String[] arr) {
		this.blocks.clear();
		this.configString = arr;
		List<String> blackList = Lists.newArrayList();
		for(String s : arr){
			if(s.startsWith("!"))
				addBlocks(s, blackList);
			else
				addBlocks(s, this.blocks);
		}
		this.blocks.removeAll(blackList);
		return this;
	}

	private static void addBlocks(String s, List<String> list) {
		if(s.contains(":"))
			list.add(s);
		else{
			NonNullList<ItemStack> ores = OreDictionary.getOres(s);
			if(!ores.isEmpty()){
				OreDictionary.getOres(s).forEach(stack -> {
					Block block = Block.getBlockFromItem(stack.getItem());
					if(block != Blocks.AIR)
						list.add(block.getRegistryName().toString());
				});
				return;
			}
			Class<?> clss = null;
			try{
				clss = Class.forName("net.minecraft.block." + s);
			}catch(ClassNotFoundException e){
				try{
					clss = Class.forName(s);
				}catch(ClassNotFoundException e1){
					ImprovedMobs.logger.error("Couldn't find class for " + s);
				}
			}
			if(clss != null)
				for(Block block : ForgeRegistries.BLOCKS){
					if(clss.isInstance(block))
						list.add(block.getRegistryName().toString());
				}
		}
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
