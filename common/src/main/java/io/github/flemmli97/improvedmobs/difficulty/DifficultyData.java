package io.github.flemmli97.improvedmobs.difficulty;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.platform.integration.DifficultyValues;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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
        if (!(world instanceof ServerLevel serverLevel))
            return 0;
        Vec3 pos = e.position();
        Supplier<Float> sup = switch (Config.CommonConfig.difficultyType) {
            case GLOBAL -> () -> DifficultyData.get(serverLevel.getServer()).getDifficulty();
            case PLAYERMAX -> () -> {
                float diff = 0;
                for (Player player : playersIn(serverLevel, pos, 256)) {
                    float pD = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) player).map(IPlayerDifficulty::getDifficultyLevel).orElse(0f);
                    if (pD > diff)
                        diff = pD;
                }
                return diff;
            };
            case PLAYERMEAN -> () -> {
                float diff = 0;
                List<Player> list = playersIn(serverLevel, pos, 256);
                if (list.isEmpty())
                    return 0f;
                for (Player player : list) {
                    diff += CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) player).map(IPlayerDifficulty::getDifficultyLevel).orElse(0f);
                }
                return diff / list.size();
            };
            case DISTANCE, DISTANCESPAWN -> () -> getDifficultyFromDist(serverLevel, e.position());
        };
        return DifficultyValues.INSTANCE.getDifficulty(serverLevel, e.blockPosition(), sup);
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
                .forEach(player -> CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(player).ifPresent(pd -> pd.setDifficultyLevel(pd.getDifficultyLevel() + increase.apply(pd.getDifficultyLevel()))));
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
        if (Config.CommonConfig.difficultyType == Config.DifficultyType.DISTANCESPAWN)
            return Config.CommonConfig.increaseHandler.get(Mth.sqrt((float) pos.distanceToSqr(level.getSharedSpawnPos().getX() + 0.5, pos.y(), level.getSharedSpawnPos().getZ() + 0.5)));
        return Config.CommonConfig.increaseHandler.get(Mth.sqrt((float) pos.distanceToSqr(Config.CommonConfig.centerPos.getPos().getX() + 0.5, pos.y(), Config.CommonConfig.centerPos.getPos().getZ() + 0.5)));
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
