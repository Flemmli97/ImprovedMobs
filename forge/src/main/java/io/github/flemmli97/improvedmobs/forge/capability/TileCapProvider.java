package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.improvedmobs.utils.ITileOpened;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class TileCapProvider {

    public static final Capability<ITileOpened> CAP = CapabilityManager.get(new CapabilityToken<ITileOpened>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ITileOpened.class);
    }
}
