package io.github.flemmli97.improvedmobs.fabric.platform.integration;

import com.github.clevernucleus.playerex.api.ExAPI;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.platform.integration.DifficultyValues;
import net.levelz.access.PlayerStatsManagerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DifficultyValuesImpl implements DifficultyValues {

    @Override
    public float getDifficulty(Level level, BlockPos pos, Supplier<Float> defaultVal) {
        if (Config.CommonConfig.usePlayerEXMod)
            return getDifficulty(level, pos, p -> (float) ExAPI.PLAYER_DATA.get(p).get(ExAPI.LEVEL));
        if (Config.CommonConfig.useLevelZMod)
            return getDifficulty(level, pos, p -> (float) ((PlayerStatsManagerAccess) p).getPlayerStatsManager().overallLevel);
        return defaultVal.get();
    }

    public static float getDifficulty(Level level, BlockPos pos, Function<ServerPlayer, Float> getter) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        return switch (Config.CommonConfig.difficultyType) {
            case PLAYERMAX -> {
                float diff = 0;
                for (Player player : DifficultyData.playersIn(level, vec3, 256)) {
                    float pD = getter.apply((ServerPlayer) player);
                    if (pD > diff)
                        diff = pD;
                }
                yield diff;
            }
            case PLAYERMEAN, GLOBAL -> {
                float diff = 0;
                List<Player> list = DifficultyData.playersIn(level, vec3, 256);
                if (list.isEmpty())
                    yield 0f;
                for (Player player : list) {
                    diff += getter.apply((ServerPlayer) player);
                }
                yield diff / list.size();
            }
        };
    }
}
