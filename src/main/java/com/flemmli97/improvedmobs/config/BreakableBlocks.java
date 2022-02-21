package com.flemmli97.improvedmobs.config;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.tenshilib.api.config.IConfigListValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BreakableBlocks implements IConfigListValue<BreakableBlocks> {

    private final Set<String> blocks = new HashSet<>();
    private List<String> configString = new ArrayList<>();
    private final Set<ITag<Block>> tags = new HashSet<>();
    private boolean initialized;

    public boolean canBreak(BlockState state, BlockPos pos, IBlockReader getter, ISelectionContext forEntity) {
        if (!this.initialized)
            this.initialize();
        if (state.getCollisionShape(getter, pos, forEntity).isEmpty())
            return false;
        if (!Config.CommonConfig.breakTileEntities && state.getBlock().hasTileEntity(state))
            return false;
        if (Config.CommonConfig.breakingAsBlacklist) {
            return this.tags.stream().noneMatch(state.getBlock()::isIn) && !this.blocks.contains(state.getBlock().getRegistryName().getNamespace()) && !this.blocks.contains(state.getBlock().getRegistryName().toString());
        }
        return this.tags.stream().anyMatch(state.getBlock()::isIn) || this.blocks.contains(state.getBlock().getRegistryName().getNamespace()) || this.blocks.contains(state.getBlock().getRegistryName().toString());
    }

    @Override
    public BreakableBlocks readFromString(List<String> arr) {
        this.blocks.clear();
        this.configString = arr;
        this.initialized = false;
        return this;
    }

    private void initialize() {
        this.initialized = true;
        Set<String> blackList = new HashSet<>();
        Set<ITag<Block>> blackListTags = new HashSet<>();
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
            if (tag != null)
                tags.add(tag);
            else
                list.add(s);
        } else {
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

    public static String use() {
        return "Usage: <registry name;classname;tag;namespace> put \"!\" infront to exclude blocks";
    }
}
