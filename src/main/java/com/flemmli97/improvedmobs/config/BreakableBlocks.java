package com.flemmli97.improvedmobs.config;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigListValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;

public class BreakableBlocks implements IConfigListValue<BreakableBlocks> {

    private final Set<String> blocks = Sets.newHashSet();
    private List<String> configString = Lists.newArrayList();
    private final Set<ITag<Block>> tags = Sets.newHashSet();
    private boolean initialized;

    public boolean canBreak(BlockState state) {
        if(!this.initialized)
            this.initialize();
        if (state.getMaterial() == Material.AIR)// || (Config.ServerConfig.useCoroUtil && state.getBlock() instanceof BlockRepairingBlock))
            return false;
        if (!Config.CommonConfig.breakTileEntities && state.getBlock().hasTileEntity(state))
            return false;
        if (Config.CommonConfig.breakingAsBlacklist) {
            return this.tags.stream().noneMatch(state.getBlock()::isIn) && !this.blocks.contains(state.getBlock().getRegistryName().toString());
        }
        return this.tags.stream().anyMatch(state.getBlock()::isIn) || this.blocks.contains(state.getBlock().getRegistryName().toString());
    }

    @Override
    public BreakableBlocks readFromString(List<String> arr) {
        this.blocks.clear();
        this.configString = arr;
        this.initialized = false;
        return this;
    }

    private void initialize(){
        this.initialized = true;
        Set<String> blackList = Sets.newHashSet();
        Set<ITag<Block>> blackListTags = Sets.newHashSet();
        for (String s : this.configString) {
            if (s.startsWith("!"))
                addBlocks(s.substring(1), blackList, blackListTags);
            else
                addBlocks(s, this.blocks, this.tags);
        }
        this.blocks.removeAll(blackList);
        this.tags.removeAll(blackListTags);
    }

    private static void addBlocks(String s, Set<String> list, Set<ITag<Block>> tags) {
        if (s.contains(":")) {
            ITag<Block> tag = BlockTags.getCollection().get(new ResourceLocation(s));
            if (tag!=null)
                tags.add(tag);
            else if(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s))!=Blocks.AIR)
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
    public List<String> writeToString() {
        return this.configString;
    }

    public static String use(){
        return "Usage: <registry name;classname;tag> put \"!\" infront to exclude blocks";
    }
}
