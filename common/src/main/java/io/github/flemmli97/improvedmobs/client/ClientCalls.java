package io.github.flemmli97.improvedmobs.client;

import io.github.flemmli97.improvedmobs.common.config.Config;

public class ClientCalls {

    public static void disconnect() {
        ClientEvents.updateClientDifficulty(0);
        Config.ClientConfig.showDifficultyServerSync = false;
    }
}
