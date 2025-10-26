package io.github.flemmli97.improvedmobs.api.difficulty;

import com.google.common.collect.Lists;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.difficulty.DifficultyData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public interface DifficultyGetter {

    static List<Player> playersIn(EntityGetter getter, Vec3 pos, double radius) {
        ArrayList<Player> list = Lists.newArrayList();
        for (Player player : getter.players()) {
            if (player.position().closerThan(pos, radius))
                list.add(player);
        }
        return list;
    }

    static double getDifficulty(Level level, Vec3 pos, ToDoubleFunction<ServerPlayer> getter) {
        return switch (Config.CommonConfig.difficultyType) {
            case PLAYERMAX -> {
                double diff = 0;
                for (Player player : DifficultyData.playersIn(level, pos, 256)) {
                    double pD = getter.applyAsDouble((ServerPlayer) player);
                    if (pD > diff)
                        diff = pD;
                }
                yield diff;
            }
            case PLAYERSUM -> {
                double diff = 0;
                for (Player player : DifficultyData.playersIn(level, pos, 256)) {
                    diff += getter.applyAsDouble((ServerPlayer) player);
                }
                yield diff;
            }
            case PLAYERMEAN, GLOBAL, DISTANCE, DISTANCESPAWN -> {
                double diff = 0;
                List<Player> list = DifficultyData.playersIn(level, pos, 256);
                if (list.isEmpty())
                    yield 0f;
                for (Player player : list) {
                    diff += getter.applyAsDouble((ServerPlayer) player);
                }
                yield diff / list.size();
            }
        };
    }

    double getDifficulty(ServerLevel level, Vec3 pos);

    Config.IntegrationType getType();

    default boolean hasOwnDisplay() {
        return false;
    }
}
