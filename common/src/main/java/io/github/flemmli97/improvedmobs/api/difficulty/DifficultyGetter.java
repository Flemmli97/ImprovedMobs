package io.github.flemmli97.improvedmobs.api.difficulty;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface DifficultyGetter {

    static List<Player> playersIn(EntityGetter getter, Vec3 pos, double radius) {
        ArrayList<Player> list = Lists.newArrayList();
        for (Player player : getter.players()) {
            if (player.position().closerThan(pos, radius))
                list.add(player);
        }
        return list;
    }

    static float getDifficulty(Level level, Vec3 pos, Function<ServerPlayer, Float> getter) {
        return switch (Config.CommonConfig.difficultyType) {
            case PLAYERMAX -> {
                float diff = 0;
                for (Player player : DifficultyData.playersIn(level, pos, 256)) {
                    float pD = getter.apply((ServerPlayer) player);
                    if (pD > diff)
                        diff = pD;
                }
                yield diff;
            }
            case PLAYERMEAN, GLOBAL, DISTANCE, DISTANCESPAWN -> {
                float diff = 0;
                List<Player> list = DifficultyData.playersIn(level, pos, 256);
                if (list.isEmpty())
                    yield 0f;
                for (Player player : list) {
                    diff += getter.apply((ServerPlayer) player);
                }
                yield diff / list.size();
            }
        };
    }

    float getDifficulty(ServerLevel level, Vec3 pos);

    Config.IntegrationType getType();

    default boolean hasOwnDisplay() {
        return false;
    }
}
