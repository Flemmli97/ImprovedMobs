package io.github.flemmli97.improvedmobs.difficulty;

import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.platform.integration.DifficultyValues;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DifficultyData extends SavedData {

    private static final String identifier = "Difficulty";
    private float difficultyLevel;
    private long prevTime;

    public DifficultyData() {
    }

    private DifficultyData(CompoundTag tag) {
        this.load(tag);
    }

    public static DifficultyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(DifficultyData::new, DifficultyData::new, identifier);
    }

    public static float getDifficulty(Level world, LivingEntity e) {
        if (!(world instanceof ServerLevel))
            return 0;
        BlockPos pos = e.blockPosition();
        Supplier<Float> sup = switch (Config.CommonConfig.difficultyType) {
            case GLOBAL -> () -> DifficultyData.get(world.getServer()).getDifficulty();
            case PLAYERMAX -> () -> {
                float diff = 0;
                for (Player player : world.getNearbyPlayers(TargetingConditions.forNonCombat(), null, new AABB(-128, -128, -128, 128, 128, 128).move(pos))) {
                    float pD = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) player).getDifficultyLevel();
                    if (pD > diff)
                        diff = pD;
                }
                return diff;
            };
            case PLAYERMEAN -> () -> {
                float diff = 0;
                List<Player> list = world.getNearbyPlayers(TargetingConditions.forNonCombat(), null, new AABB(-128, -128, -128, 128, 128, 128).move(pos));
                for (Player player : list) {
                    diff += CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) player).getDifficultyLevel();
                }
                return diff / list.size();
            };
        };
        return DifficultyValues.INSTANCE.getDifficulty(world, e.blockPosition(), sup);
    }

    public void increaseDifficultyBy(Function<Float, Float> increase, long time, MinecraftServer server) {
        this.difficultyLevel += increase.apply(this.getDifficulty());
        this.prevTime = time;
        server.getPlayerList().getPlayers()
                .forEach(player -> {
                    IPlayerDifficulty pd = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
                    pd.setDifficultyLevel(pd.getDifficultyLevel() + increase.apply(pd.getDifficultyLevel()));
                });
        this.setDirty();
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
    }

    public void setDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel = level;
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
        this.setDirty();
    }

    public void addDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel += level;
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
        this.setDirty();
    }

    public float getDifficulty() {
        return this.difficultyLevel;
    }

    public long getPrevTime() {
        return this.prevTime;
    }

    public void load(CompoundTag nbt) {
        this.difficultyLevel = nbt.getFloat("Difficulty");
        this.prevTime = nbt.getLong("Time");
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putFloat("Difficulty", this.difficultyLevel);
        compound.putLong("Time", this.prevTime);
        return compound;
    }
}
