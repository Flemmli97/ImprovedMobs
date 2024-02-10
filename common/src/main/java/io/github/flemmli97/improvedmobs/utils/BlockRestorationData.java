package io.github.flemmli97.improvedmobs.utils;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.config.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockRestorationData extends SavedData {

    private static final String ID = "ImprovedMobsRestoration";

    private final Long2ObjectMap<Long2ObjectMap<SavedBlock>> toRestore = new Long2ObjectOpenHashMap<>();

    public BlockRestorationData() {
    }

    private BlockRestorationData(CompoundTag tag) {
        this.load(tag);
    }

    public static BlockRestorationData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlockRestorationData::new, BlockRestorationData::new, ID);
    }

    public void restore(ServerLevel level, BlockState state, BlockPos pos, @Nullable Entity entity) {
        Long2ObjectMap<SavedBlock> chunk = this.toRestore.computeIfAbsent(ChunkPos.asLong(pos), o -> new Long2ObjectOpenHashMap<>());
        SavedBlock current = chunk.get(pos.asLong());
        if (current != null) {
            current.toDrop.forEach(stack -> Block.popResource(level, pos, stack));
        }
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, entity, ItemStack.EMPTY);
        chunk.put(pos.asLong(), new SavedBlock(state, level.getGameTime(), drops));
        this.setDirty();
    }

    public void tick(ServerLevel level) {
        LongSet toRemove = new LongArraySet();
        long time = level.getGameTime();
        boolean particle = time % 5 == 0;
        this.toRestore.forEach((chunk, data) -> {
            ChunkPos chunkPos = new ChunkPos(chunk);
            if (level.hasChunk(chunkPos.x, chunkPos.z)) {
                LongSet toRemoveBlocks = new LongArraySet();
                LevelChunk levelChunk = level.getChunk(chunkPos.x, chunkPos.z);
                data.forEach((packedPos, replace) -> {
                    BlockPos pos = BlockPos.of(packedPos);
                    BlockState current = levelChunk.getBlockState(pos);
                    if (current.getBlock() == Blocks.AIR) {
                        if (Math.abs(time - replace.time) > Config.CommonConfig.restoreDelay) {
                            level.setBlock(pos, replace.state, Block.UPDATE_ALL);
                            toRemoveBlocks.add(packedPos.longValue());
                        } else if (particle)
                            level.sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0, 0, 0);
                    } else {
                        replace.toDrop.forEach(stack -> Block.popResource(level, pos, stack));
                        toRemoveBlocks.add(packedPos.longValue());
                    }
                });
                toRemoveBlocks.forEach(data::remove);
                if (data.isEmpty())
                    toRemove.add(chunk.longValue());
                if (!toRemoveBlocks.isEmpty())
                    this.setDirty();
            }
        });
        toRemove.forEach(this.toRestore::remove);
    }

    public void load(CompoundTag data) {
        data.getAllKeys().forEach((chunk) -> {
            Long2ObjectMap<SavedBlock> blocksMap = new Long2ObjectOpenHashMap<>();
            ListTag blocks = data.getList(chunk, Tag.TAG_COMPOUND);
            blocks.forEach(t -> {
                CompoundTag blockSave = (CompoundTag) t;
                List<ItemStack> stacks = new ArrayList<>();
                blockSave.getList("Drops", Tag.TAG_COMPOUND)
                        .forEach(st -> stacks.add(ItemStack.CODEC.parse(NbtOps.INSTANCE, st).getOrThrow(false, ImprovedMobs.logger::error)));
                blocksMap.put(blockSave.getLong("Pos"),
                        new SavedBlock(BlockState.CODEC.parse(NbtOps.INSTANCE, blockSave.get("State")).getOrThrow(false, ImprovedMobs.logger::error), blockSave.getLong("Time"), stacks));
            });
            this.toRestore.put(Long.parseLong(chunk), blocksMap);
        });
    }

    @Override
    public CompoundTag save(CompoundTag data) {
        this.toRestore.forEach((chunk, blocks) -> {
            ListTag list = new ListTag();
            blocks.forEach((pos, d) -> {
                CompoundTag blockSave = new CompoundTag();
                blockSave.putLong("Pos", pos);
                blockSave.put("State", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, d.state).getOrThrow(false, ImprovedMobs.logger::error));
                blockSave.putLong("Time", d.time);
                ListTag stacks = new ListTag();
                d.toDrop.forEach(s -> stacks.add(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, s).getOrThrow(false, ImprovedMobs.logger::error)));
                blockSave.put("Drops", stacks);
                list.add(blockSave);
            });
            data.put(String.valueOf(chunk), list);
        });
        return data;
    }

    public record SavedBlock(BlockState state, long time, List<ItemStack> toDrop) {

    }
}
