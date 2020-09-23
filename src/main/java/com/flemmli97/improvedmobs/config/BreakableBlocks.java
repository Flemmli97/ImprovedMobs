package com.flemmli97.improvedmobs.config;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class BreakableBlocks implements IConfigArrayValue<BreakableBlocks> {

    private final List<String> blocks = Lists.newArrayList();
    private String[] configString;

    public BreakableBlocks(String[] strings) {
        this.readFromString(strings);
    }

    public boolean canBreak(BlockState state) {
        if (state.getMaterial() == Material.AIR)// || (Config.commonConf.useCoroUtil && state.getBlock() instanceof BlockRepairingBlock))
            return false;
        if (!Config.commonConf.breakTileEntities && state.getBlock().hasTileEntity(state))
            return false;
        if (Config.commonConf.breakingAsBlacklist) {
            return !this.blocks.contains(state.getBlock().getRegistryName().toString());
        }
        return this.blocks.contains(state.getBlock().getRegistryName().toString());
    }

    @Override
    public BreakableBlocks readFromString(String[] arr) {
        this.blocks.clear();
        this.configString = arr;
        List<String> blackList = Lists.newArrayList();
        for (String s : arr) {
            if (s.startsWith("!"))
                addBlocks(s.substring(1), blackList);
            else
                addBlocks(s, this.blocks);
        }
        this.blocks.removeAll(blackList);
        return this;
    }

    private static void addBlocks(String s, List<String> list) {
        if (s.contains(":")) {
            ITag<Item> tags = ItemTags.getCollection().get(new ResourceLocation(s));
            if (tags!=null) {
                tags.values().forEach(item -> {
                    Block block = Block.getBlockFromItem(item);
                    if (block != Blocks.AIR)
                        list.add(block.getRegistryName().toString());
                });
                return;
            }
            list.add(s);
        }
        else {
            Class<?> clss = null;
            try {
                clss = Class.forName("net.minecraft.block." + s);
            } catch (ClassNotFoundException e) {
                try {
                    clss = Class.forName(s);
                } catch (ClassNotFoundException e1) {
                    ImprovedMobs.logger.error("Couldn't find class for " + s);
                }
            }
            if (clss != null)
                for (Block block : ForgeRegistries.BLOCKS) {
                    if (clss.isInstance(block))
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
