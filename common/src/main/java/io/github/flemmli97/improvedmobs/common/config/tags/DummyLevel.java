package io.github.flemmli97.improvedmobs.common.config.tags;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class DummyLevel extends Level {

    private final TickRateManager tickRateManager;
    private final MapId mapId;
    private final Scoreboard scoreboard;
    private final RecipeManager recipeManager;

    private final ChunkSource chunkSource;
    private final LevelEntityGetter<Entity> levelEntityGetter;

    public DummyLevel(RegistryAccess registryAccess) {
        super(new DummyLevelData(), ResourceKey.create(Registries.DIMENSION, ImprovedMobs.modRes("dummy_level")),
                registryAccess, registryAccess.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                () -> InactiveProfiler.INSTANCE, true, false, 0, 0);
        this.tickRateManager = new TickRateManager();
        this.mapId = new MapId(0);
        this.scoreboard = new Scoreboard();
        this.recipeManager = new RecipeManager(registryAccess);
        this.chunkSource = new DummyChunkSource();
        this.levelEntityGetter = new EmptyLevelEntityGetter<>();
    }

    @Override
    public void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState1, int i) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, double v, double v1, double v2, Holder<SoundEvent> holder, SoundSource soundSource, float v3, float v4, long l) {
    }

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundSource, float v, float v1, long l) {
    }

    @Override
    public String gatherChunkSourceStats() {
        return "";
    }

    @Override
    public Entity getEntity(int i) {
        return null;
    }

    @Override
    public TickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    @Override
    public MapItemSavedData getMapData(MapId mapId) {
        return null;
    }

    @Override
    public void setMapData(MapId mapId, MapItemSavedData mapItemSavedData) {
    }

    @Override
    public MapId getFreeMapId() {
        return this.mapId;
    }

    @Override
    public void destroyBlockProgress(int i, BlockPos blockPos, int i1) {
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return this.recipeManager;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return this.levelEntityGetter;
    }

    @Override
    public PotionBrewing potionBrewing() {
        return PotionBrewing.EMPTY;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.chunkSource;
    }

    @Override
    public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int i1) {
    }

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context) {
    }

    @Override
    public float getShade(Direction direction, boolean b) {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return List.of();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int i1, int i2) {
        return this.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return FeatureFlags.DEFAULT_FLAGS;
    }

    // Neo Extensions
    public void setDayTimeFraction(float var1) {
    }

    public float getDayTimeFraction() {
        return 0;
    }

    public float getDayTimePerTick() {
        return 0;
    }

    public void setDayTimePerTick(float var1) {
    }

    private class DummyChunkSource extends ChunkSource {

        private final LevelLightEngine levelLightEngine = new LevelLightEngine(this, true, true);

        public DummyChunkSource() {
        }

        @Override
        public ChunkAccess getChunk(int i, int i1, ChunkStatus chunkStatus, boolean b) {
            return null;
        }

        @Override
        public void tick(BooleanSupplier booleanSupplier, boolean b) {
        }

        @Override
        public String gatherStats() {
            return "";
        }

        @Override
        public int getLoadedChunksCount() {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return this.levelLightEngine;
        }

        @Override
        public BlockGetter getLevel() {
            return DummyLevel.this;
        }
    }

    public static class EmptyLevelEntityGetter<T extends EntityAccess> implements LevelEntityGetter<T> {

        @Override
        public @Nullable T get(int i) {
            return null;
        }

        @Override
        public @Nullable T get(UUID uuid) {
            return null;
        }

        @Override
        public Iterable<T> getAll() {
            return List.of();
        }

        @Override
        public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AbortableIterationConsumer<U> abortableIterationConsumer) {
        }

        @Override
        public void get(AABB aabb, Consumer<T> consumer) {
        }

        @Override
        public <U extends T> void get(EntityTypeTest<T, U> entityTypeTest, AABB aabb, AbortableIterationConsumer<U> abortableIterationConsumer) {
        }
    }

    protected static class DummyLevelData implements WritableLevelData {

        private final GameRules gameRules = new GameRules();

        protected DummyLevelData() {
        }

        @Override
        public void setSpawn(BlockPos blockPos, float v) {
        }

        @Override
        public BlockPos getSpawnPos() {
            return BlockPos.ZERO;
        }

        @Override
        public float getSpawnAngle() {
            return 0;
        }

        @Override
        public long getGameTime() {
            return 0;
        }

        @Override
        public long getDayTime() {
            return 0;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return false;
        }

        @Override
        public void setRaining(boolean b) {

        }

        @Override
        public boolean isHardcore() {
            return false;
        }

        @Override
        public GameRules getGameRules() {
            return this.gameRules;
        }

        @Override
        public Difficulty getDifficulty() {
            return Difficulty.HARD;
        }

        @Override
        public boolean isDifficultyLocked() {
            return false;
        }
    }
}
