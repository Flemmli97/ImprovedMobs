package io.github.flemmli97.improvedmobs.difficulty;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.DifficultyConfig;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DifficultyData extends SavedData {

    private static final String IDENTIFIER = "Difficulty";
    private static final SavedData.Factory<DifficultyData> FACTORY = new Factory<>(DifficultyData::new, DifficultyData::new, DataFixTypes.LEVEL);
    private float difficultyLevel;
    private long prevTime;

    public DifficultyData() {
    }

    private DifficultyData(CompoundTag tag, HolderLookup.Provider provider) {
        this.load(tag);
    }

    public static DifficultyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, IDENTIFIER);
    }

    public static float getDifficulty(Level level, LivingEntity e) {
        if (!(level instanceof ServerLevel serverLevel))
            return 0;
        return DifficultyFetcher.getDifficulty(serverLevel, e.position());
    }

    public static List<Player> playersIn(EntityGetter getter, Vec3 pos, double radius) {
        ArrayList<Player> list = Lists.newArrayList();
        for (Player player : getter.players()) {
            if (player.position().closerThan(pos, radius))
                list.add(player);
        }
        return list;
    }

    public void increaseDifficultyBy(Function<Float, Float> increase, long time, MinecraftServer server) {
        this.difficultyLevel += increase.apply(this.getDifficulty());
        this.prevTime = time;
        server.getPlayerList().getPlayers()
                .forEach(player -> {
                    IPlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player);
                    data.setDifficultyLevel(data.getDifficultyLevel() + increase.apply(data.getDifficultyLevel()));
                });
        this.setDirty();
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
    }

    public void setDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel = level;
        this.prevTime = server.overworld().getDayTime();
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

    public static float getDifficultyFromDist(ServerLevel level, Vec3 pos) {
        float dist;
        if (Config.CommonConfig.difficultyType == Config.DifficultyType.DISTANCESPAWN) {
            dist = Mth.sqrt((float) pos.distanceToSqr(level.getSharedSpawnPos().getX() + 0.5, pos.y(), level.getSharedSpawnPos().getZ() + 0.5));
        } else {
            dist = Mth.sqrt((float) pos.distanceToSqr(Config.CommonConfig.centerPos.getPos().getX() + 0.5, pos.y(), Config.CommonConfig.centerPos.getPos().getZ() + 0.5));
        }
        Pair<Float, DifficultyConfig.Zone> conf = Config.CommonConfig.increaseHandler.get(dist);
        return conf.getRight().start() + (dist - conf.getLeft()) * conf.getRight().increasePerBlock();
    }

    public void load(CompoundTag nbt) {
        this.difficultyLevel = nbt.getFloat("Difficulty");
        this.prevTime = nbt.getLong("Time");
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putFloat("Difficulty", this.difficultyLevel);
        compound.putLong("Time", this.prevTime);
        return compound;
    }
}
