package com.flemmli97.improvedmobs.difficulty;

import com.flemmli97.improvedmobs.capability.PlayerDifficultyData;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

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
        if (Config.CommonConfig.useScalingHealthMod)
            return (float) SHDifficulty.areaDifficulty(world, e.getPosition());
        BlockPos pos = e.getPosition();
        switch (Config.CommonConfig.difficultyType) {
            case GLOBAL:
                return DifficultyData.get(world).getDifficulty();
            case PLAYERMAX:
                float diff = 0;
                for (PlayerEntity player : world.getTargettablePlayersWithinAABB(EntityPredicate.DEFAULT.allowInvulnerable(), null, new AxisAlignedBB(-128, -128, -128, 128, 128, 128).offset(pos))) {
                    float pD = TileCapProvider.getPlayerDifficultyData((ServerPlayerEntity) player).getDifficultyLevel();
                    if (pD > diff)
                        diff = pD;
                }
                return diff;
            case PLAYERMEAN:
                diff = 0;
                List<PlayerEntity> list = world.getTargettablePlayersWithinAABB(EntityPredicate.DEFAULT.allowInvulnerable(), null, new AxisAlignedBB(-128, -128, -128, 128, 128, 128).offset(pos));
                for (PlayerEntity player : list) {
                    diff += TileCapProvider.getPlayerDifficultyData((ServerPlayerEntity) player).getDifficultyLevel();
                }
                return diff / list.size();
        }
        return 0;
    }

    public void increaseDifficultyBy(Function<Float, Float> increase, long time, MinecraftServer server) {
        this.difficultyLevel += increase.apply(this.getDifficulty());
        this.prevTime = time;
        server.getPlayerList().getPlayers()
                .forEach(player -> {
                    PlayerDifficultyData pd = TileCapProvider.getPlayerDifficultyData(player);
                    pd.setDifficultyLevel(pd.getDifficultyLevel() + increase.apply(pd.getDifficultyLevel()));
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
