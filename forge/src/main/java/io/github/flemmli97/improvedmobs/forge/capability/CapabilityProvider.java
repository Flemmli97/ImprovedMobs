package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.improvedmobs.difficulty.PlayerDifficulty;
import io.github.flemmli97.improvedmobs.utils.ContainerOpened;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CapabilityProvider {

    public static final Capability<ContainerOpened> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<PlayerDifficulty> PLAYER_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ContainerOpened.class);
        event.register(PlayerDifficulty.class);
    }
}
