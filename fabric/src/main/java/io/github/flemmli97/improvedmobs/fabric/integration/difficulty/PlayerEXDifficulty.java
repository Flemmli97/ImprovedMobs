package io.github.flemmli97.improvedmobs.fabric.integration.difficulty;

import com.github.clevernucleus.playerex.api.ExAPI;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class PlayerEXDifficulty implements DifficultyGetter {

    @Override
    public float getDifficulty(ServerLevel level, Vec3 pos) {
        return DifficultyGetter.getDifficulty(level, pos, p -> (float) ExAPI.PLAYER_DATA.get(p).get(ExAPI.LEVEL) * Config.CommonConfig.playerEXScale);
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.CommonConfig.usePlayerEXMod;
    }

}
