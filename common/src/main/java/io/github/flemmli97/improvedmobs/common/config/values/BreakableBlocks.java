package io.github.flemmli97.improvedmobs.common.config.values;

import io.github.flemmli97.improvedmobs.common.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

    private final List<String> config = new ArrayList<>();
    private boolean initialized;

    private final Set<Block> blocks = new HashSet<>();

    public BreakableBlocks(String... defaultVal) {
        this.config.addAll(List.of(defaultVal));
    }

    public boolean canBreak(BlockState state, BlockPos pos, BlockGetter level, @Nullable Entity entity, CollisionContext ctx) {
        this.initialize();
        if (!Config.CommonConfig.idleBreak && entity instanceof Mob mob && mob.getTarget() == null)
            return false;
        if (state.getCollisionShape(level, pos, ctx).isEmpty())
            return false;
        if (!Config.CommonConfig.breakBlockEntities && state.hasBlockEntity())
            return false;
        if (Config.CommonConfig.breakingAsBlacklist) {
            return !this.blocks.contains(state.getBlock());
        }
        return this.blocks.contains(state.getBlock());
    }

    private void initialize() {
        if (this.initialized)
            return;
        this.initialized = true;
        BuiltInRegistries.BLOCK.holders().forEach(block -> {
            ResourceLocation id = block.key().location();
            boolean contains = this.config.contains(id.toString()) || this.config.contains(id.getNamespace())
                    || block.tags().anyMatch(tag -> this.config.contains("#" + tag.location()));
            if (contains) {
                boolean blocked = this.config.contains("!" + id) || this.config.contains("!" + id.getNamespace())
                        || block.tags().anyMatch(tag -> this.config.contains("!#" + tag.location()));
                if (!blocked) {
                    this.blocks.add(block.value());
                }
            }
        });
    }

    public void tagReloaded() {
        this.blocks.clear();
        this.initialized = false;
        this.initialize();
    }

    public void read(List<String> arr) {
        this.config.clear();
        this.config.addAll(arr);
        this.blocks.clear();
        this.initialized = false;
    }

    public List<String> write() {
        return List.copyOf(this.config);
    }

    public static String use() {
        return "Usage: id|namespace|#tag. Put \"!\" infront to exclude blocks. E.g. \"minecraft\", \"minecraft:dirt\" or \"#minecraft:planks\"";
    }
}
