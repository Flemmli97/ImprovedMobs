package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.tenshilib.api.config.IConfigListValue;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BreakableBlocks implements IConfigListValue<BreakableBlocks> {

    private final Set<String> blocks = new HashSet<>();
    private List<String> configString = new ArrayList<>();
    private final Set<HolderSet<Block>> tags = new HashSet<>();
    private boolean initialized;

    public boolean canBreak(BlockState state, BlockPos pos, BlockGetter level, @Nullable Entity entity, CollisionContext ctx) {
        if (!this.initialized)
            this.initialize();
        if (!Config.CommonConfig.idleBreak && entity instanceof Mob mob && mob.getTarget() == null)
            return false;
        if (state.getCollisionShape(level, pos, ctx).isEmpty())
            return false;
        if (!Config.CommonConfig.breakTileEntities && state.hasBlockEntity())
            return false;
        if (Config.CommonConfig.breakingAsBlacklist) {
            return this.tags.stream().noneMatch(state::is) && !this.blocks.contains(PlatformUtils.INSTANCE.blocks().getIDFrom(state.getBlock()).toString())
                    && !this.blocks.contains(PlatformUtils.INSTANCE.blocks().getIDFrom(state.getBlock()).getNamespace());
        }
        return this.tags.stream().anyMatch(state::is) || this.blocks.contains(PlatformUtils.INSTANCE.blocks().getIDFrom(state.getBlock()).getNamespace()) || this.blocks.contains(PlatformUtils.INSTANCE.blocks().getIDFrom(state.getBlock()).toString());
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
        Set<HolderSet<Block>> blackListTags = new HashSet<>();
        for (String s : this.configString) {
            if (s.startsWith("!"))
                addBlocks(s.substring(1), blackList, blackListTags);
            else
                addBlocks(s, this.blocks, this.tags);
        }
        this.blocks.removeAll(blackList);
        this.tags.removeAll(blackListTags);
    }

    private static void addBlocks(String s, Set<String> list, Set<HolderSet<Block>> tags) {
        if (s.contains(":")) {
            Optional<HolderSet.Named<Block>> tag = Registry.BLOCK.getTag(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(s)));
            if (tag.isPresent())
                tags.add(tag.get());
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
                for (Block block : PlatformUtils.INSTANCE.blocks().getIterator()) {
                    if (clss.isInstance(block))
                        list.add(PlatformUtils.INSTANCE.blocks().getIDFrom(block).toString());
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
