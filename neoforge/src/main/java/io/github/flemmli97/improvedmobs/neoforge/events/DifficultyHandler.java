package io.github.flemmli97.improvedmobs.neoforge.events;

import io.github.flemmli97.improvedmobs.common.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.common.difficulty.PlayerDifficulty;
import io.github.flemmli97.improvedmobs.common.events.EventCalls;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class DifficultyHandler {

    @SubscribeEvent
    public void worldJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.getEntity().level().isClientSide) {
            EventCalls.levelJoin(player, player.getServer());
        }
    }

    @SubscribeEvent
    public void increaseDifficulty(LevelTickEvent.Post e) {
        if (e.getLevel() instanceof ServerLevel level) {
            EventCalls.tick(level);
        }
    }

    @SubscribeEvent
    public void readOnDeath(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(serverPlayer);
            PlayerDifficulty old = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(serverPlayer);
            data.copyFrom(old);
            CrossPlatformStuff.INSTANCE.sendDifficultyData(DifficultyData.get(serverPlayer.getServer()), serverPlayer.getServer());
        }
    }
}
