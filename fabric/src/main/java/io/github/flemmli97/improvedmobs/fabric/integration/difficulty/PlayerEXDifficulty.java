package io.github.flemmli97.improvedmobs.fabric.integration.difficulty;

import com.bibireden.playerex.api.attribute.PlayerEXAttributes;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyGetter;
import io.github.flemmli97.improvedmobs.common.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class PlayerEXDifficulty implements DifficultyGetter {

    @Override
    public double getDifficulty(ServerLevel level, Vec3 pos) {
        return DifficultyGetter.getDifficulty(level, pos, p -> p.getAttributeValue(Holder.direct(PlayerEXAttributes.LEVEL)) * Config.CommonConfig.playerEXScale);
    }

    @Override
    public Config.IntegrationType getType() {
        return Config.CommonConfig.usePlayerEXMod;
    }
}
