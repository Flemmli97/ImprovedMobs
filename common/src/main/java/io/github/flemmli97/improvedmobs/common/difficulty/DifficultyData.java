package io.github.flemmli97.improvedmobs.common.difficulty;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.config.values.StepExpressionConfig;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.loader.registry.AttachmentRegister;
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

import java.util.ArrayList;
import java.util.List;

public class DifficultyData extends SavedData {

    private static final String IDENTIFIER = "Difficulty";
    private static final SavedData.Factory<DifficultyData> FACTORY = new Factory<>(DifficultyData::new, DifficultyData::new, DataFixTypes.LEVEL);

    private float difficultyLevel;
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

    public static float getDifficulty(Level level, LivingEntity entity) {
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
                float current = this.getDifficulty();
                this.difficultyLevel = (float) Config.CommonConfig.difficultyIncrease.get(current)
                        .expression().get(vars.setVariable("difficulty", current));
            }
            server.getPlayerList().getPlayers()
                    .forEach(player -> {
                        PlayerDifficulty data = AttachmentRegister.INSTANCE.getAttachment(player, ImprovedMobsAttachments.PLAYER_DIFFICULTY);
                        if (!data.paused()) {
                            float current = data.getDifficultyLevel();
                            Config.apply(vars, player, current);
                            data.setDifficultyLevel((float) Config.CommonConfig.difficultyIncrease.get(current).expression().get(vars));
                        }
                    });
        }
        this.prevTime = time;
        this.setDirty();
        CrossPlatformStuff.INSTANCE.sendDifficultyData(this, server);
    }

    public void updateTime(MinecraftServer server) {
        this.prevTime = server.overworld().getDayTime();
        this.setDirty();
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
            dist = Mth.sqrt((float) pos.distanceToSqr(Config.CommonConfig.centerPos.getPos().x() + 0.5, pos.y(), Config.CommonConfig.centerPos.getPos().z() + 0.5));
        }
        StepExpressionConfig.Value value = Config.CommonConfig.difficultyIncrease.get(dist);
        VariableMap map = new VariableMap();
        Config.apply(map, level.getRandom(), level.getSharedSpawnPos(), pos, 0);
        return (float) value.expression().get(map);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void load(CompoundTag nbt) {
        this.difficultyLevel = nbt.getFloat("Difficulty");
        this.prevTime = nbt.getLong("Time");
        this.paused = nbt.getBoolean("Paused");
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {
        compound.putFloat("Difficulty", this.difficultyLevel);
        compound.putLong("Time", this.prevTime);
        compound.putBoolean("Paused", this.paused);
        return compound;
    }
}
