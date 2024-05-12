package io.github.flemmli97.improvedmobs.config;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BreakableBlocks {

    private final Set<String> blocks = new HashSet<>();
    private List<String> configString = new ArrayList<>();
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
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (Config.CommonConfig.breakingAsBlacklist) {
            return !this.blocks.contains(id.toString())
                    && !this.blocks.contains(id.getNamespace());
        }
        return this.blocks.contains(id.getNamespace()) || this.blocks.contains(id.toString());
    }

    public BreakableBlocks readFromString(List<String> arr) {
        this.blocks.clear();
        this.configString = arr;
        this.initialized = false;
        return this;
    }

    public void initialize() {
        this.initialized = true;
        this.blocks.clear();
        Set<String> blackList = new HashSet<>();
        for (String s : this.configString) {
            if (s.startsWith("!"))
                addBlocks(s.substring(1), blackList);
            else
                addBlocks(s, this.blocks);
        }
        this.blocks.removeAll(blackList);
    }

    private static void addBlocks(String s, Set<String> blocks) {
        if (s.startsWith("#")) {
            Iterable<Holder<Block>> tag = BuiltInRegistries.BLOCK.getTagOrEmpty(TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(s.substring(1))));
            tag.forEach(h -> blocks.add(BuiltInRegistries.BLOCK.getKey(h.value()).toString()));
        } else
            blocks.add(s);
    }

    public List<String> writeToString() {
        return this.configString;
    }

    public static String use() {
        return "Usage: id|namespace|#tag. Put \"!\" infront to exclude blocks. E.g. \"minecraft\", \"minecraft:dirt\" or \"#minecraft:planks\"";
    }
}
