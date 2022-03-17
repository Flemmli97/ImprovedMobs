package com.flemmli97.improvedmobs.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import java.util.Optional;

public class TileCapProvider {

    @CapabilityInject(ITileOpened.class)
    public static final Capability<ITileOpened> OpenedCap = null;
    @CapabilityInject(PlayerDifficultyData.class)
    public static final Capability<PlayerDifficultyData> PlayerCap = null;

    public static Optional<PlayerDifficultyData> getPlayerDifficultyData(ServerPlayerEntity player) {
        return player.getCapability(PlayerCap).resolve();
    }
}
