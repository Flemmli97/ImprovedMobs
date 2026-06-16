package io.github.flemmli97.improvedmobs.common.difficulty;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.config.values.DifficultyExpressionConfig;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DifficultyData extends SavedData {

    private static final String IDENTIFIER = "Difficulty";
    private static final SavedData.Factory<DifficultyData> FACTORY = new Factory<>(DifficultyData::new, DifficultyData::new, DataFixTypes.LEVEL);

    private int difficultyIndex;
    private double difficultyLevel;
    private long prevTime;

    private boolean paused;

    public DifficultyData() {
    }

    private DifficultyData(CompoundTag tag, HolderLookup.Provider provider) {
        this.load(tag);
    }

    public static DifficultyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, IDENTIFIER);
    }

    public static double getDifficulty(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel))
            return 0;
        return DifficultyFetcher.getDifficulty(serverLevel, entity.position());
    }

    public static List<Player> playersIn(EntityGetter getter, Vec3 pos, double radius) {
        ArrayList<Player> list = Lists.newArrayList();
        for (Player player : getter.players()) {
            if (player.position().closerThan(pos, radius))
                list.add(player);
        }
        return list;
    }

    public void increaseDifficulty(boolean shouldIncrease, long time, MinecraftServer server) {
        if (shouldIncrease) {
            VariableMap vars = new VariableMap();
            if (!this.paused) {
                this.increaseCurrent(vars);
            }
            server.getPlayerList().getPlayers()
                    .forEach(player -> {
                        PlayerDifficulty data = ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player);
                        if (!data.paused()) {
                            Config.apply(vars, player, data.getDifficultyLevel());
                            data.increaseCurrent(vars);
                        }
                    });
        }
        this.prevTime = time;
        this.setDirty();
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
    }

    public void increaseCurrent(VariableMap vars) {
        double current = this.getDifficulty();
        DifficultyExpressionConfig.DifficultyExpression difficulty = Config.CommonConfig.difficultyIncrease.get(current, this.difficultyIndex);
        this.difficultyIndex = difficulty.getIndex();
        this.difficultyLevel = difficulty.get(vars.setVariable("difficulty", current));
    }

    public void updateTime(MinecraftServer server) {
        this.prevTime = server.overworld().getDayTime();
        this.setDirty();
    }

    public void setDifficulty(double level, MinecraftServer server) {
        this.difficultyLevel = level;
        this.difficultyIndex = 0;
        this.prevTime = server.overworld().getDayTime();
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
        this.setDirty();
    }

    public void addDifficulty(double level, MinecraftServer server) {
        this.difficultyLevel += level;
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
        this.setDirty();
    }

    public double getDifficulty() {
        return this.difficultyLevel;
    }

    public long getPrevTime() {
        return this.prevTime;
    }

    public static double getDifficultyFromDist(ServerLevel level, Vec3 pos) {
        double dist;
        if (Config.CommonConfig.difficultyType == Config.DifficultyType.DISTANCESPAWN) {
            dist = Math.sqrt(pos.distanceToSqr(level.getSharedSpawnPos().getX() + 0.5, pos.y(), level.getSharedSpawnPos().getZ() + 0.5));
        } else {
            dist = Math.sqrt(pos.distanceToSqr(Config.CommonConfig.centerPos.getPos().x() + 0.5, pos.y(), Config.CommonConfig.centerPos.getPos().z() + 0.5));
        }
        DifficultyExpressionConfig.DifficultyExpression value = Config.CommonConfig.difficultyIncrease.get(dist, 0);
        VariableMap map = new VariableMap();
        Config.apply(map, level.getRandom(), level.getSharedSpawnPos(), pos, 0);
        return value.get(map);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void load(CompoundTag tag) {
        this.difficultyLevel = tag.getDouble("Difficulty");
        this.difficultyIndex = tag.getInt("DifficultyIndex");
        this.prevTime = tag.getLong("Time");
        this.paused = tag.getBoolean("Paused");
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putDouble("Difficulty", this.difficultyLevel);
        tag.putInt("DifficultyIndex", this.difficultyIndex);
        tag.putLong("Time", this.prevTime);
        tag.putBoolean("Paused", this.paused);
        return tag;
    }
}
