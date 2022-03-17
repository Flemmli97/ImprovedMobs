package com.flemmli97.improvedmobs.difficulty;

import com.flemmli97.improvedmobs.capability.PlayerDifficultyData;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.network.PacketHandler;
import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DifficultyData extends WorldSavedData {

    private static final String identifier = "Difficulty";
    private float difficultyLevel;
    private long prevTime;

    public DifficultyData() {
        this(identifier);
    }

    private DifficultyData(String id) {
        super(id);
    }

    public static DifficultyData get(World world) {
        return world.getServer().func_241755_D_().getSavedData().getOrCreate(DifficultyData::new, identifier);
    }

    public static float getDifficulty(World world, LivingEntity e) {
        if (Config.CommonConfig.useScalingHealthMod) {
            if (Config.CommonConfig.legacyScalingHealth)
                return (float) net.silentchaos512.scalinghealth.utils.SHDifficulty.areaDifficulty(world, e.getPosition());
            return (float) net.silentchaos512.scalinghealth.utils.config.SHDifficulty.areaDifficulty(world, e.getPosition());
        }
        Vector3d pos = e.getPositionVec();
        switch (Config.CommonConfig.difficultyType) {
            case GLOBAL:
                return DifficultyData.get(world).getDifficulty();
            case PLAYERMAX:
                float diff = 0;
                for (PlayerEntity player : playersIn(world, pos, 256)) {
                    float pD = TileCapProvider.getPlayerDifficultyData((ServerPlayerEntity) player).map(PlayerDifficultyData::getDifficultyLevel).orElse(0f);
                    if (pD > diff)
                        diff = pD;
                }
                return diff;
            case PLAYERMEAN:
                diff = 0;
                List<PlayerEntity> list = playersIn(world, pos, 256);
                if (list.isEmpty())
                    return 0f;
                for (PlayerEntity player : list) {
                    diff += TileCapProvider.getPlayerDifficultyData((ServerPlayerEntity) player).map(PlayerDifficultyData::getDifficultyLevel).orElse(0f);
                }
                return diff / list.size();
        }
        return 0;
    }

    private static List<PlayerEntity> playersIn(IEntityReader getter, Vector3d pos, double radius) {
        ArrayList<PlayerEntity> list = Lists.newArrayList();
        for (PlayerEntity player : getter.getPlayers()) {
            if (player.getPositionVec().isWithinDistanceOf(pos, radius))
                list.add(player);
        }
        return list;
    }

    public void increaseDifficultyBy(Function<Float, Float> increase, long time, MinecraftServer server) {
        this.difficultyLevel += increase.apply(this.getDifficulty());
        this.prevTime = time;
        server.getPlayerList().getPlayers()
                .forEach(player -> {
                    TileCapProvider.getPlayerDifficultyData(player).ifPresent(pd -> pd.setDifficultyLevel(pd.getDifficultyLevel() + increase.apply(pd.getDifficultyLevel())));
                });
        this.markDirty();
        PacketHandler.sendDifficultyToAll(this, server);
    }

    public void setDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel = level;
        PacketHandler.sendDifficultyToAll(this, server);
        this.markDirty();
    }

    public void addDifficulty(float level, MinecraftServer server) {
        this.difficultyLevel += level;
        PacketHandler.sendDifficultyToAll(this, server);
        this.markDirty();
    }

    public float getDifficulty() {
        return this.difficultyLevel;
    }

    public long getPrevTime() {
        return this.prevTime;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.difficultyLevel = nbt.getFloat("Difficulty");
        this.prevTime = nbt.getLong("Time");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putFloat("Difficulty", this.difficultyLevel);
        compound.putLong("Time", this.prevTime);
        return compound;
    }
}
