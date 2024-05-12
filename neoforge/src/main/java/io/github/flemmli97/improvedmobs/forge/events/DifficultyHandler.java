package io.github.flemmli97.improvedmobs.forge.events;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.events.EventCalls;
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
            EventCalls.worldJoin(player, player.getServer());
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
            IPlayerDifficulty data = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(serverPlayer);
            IPlayerDifficulty old = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(serverPlayer);
            data.setDifficultyLevel(old.getDifficultyLevel());
            CrossPlatformStuff.INSTANCE.sendDifficultyData(DifficultyData.get(serverPlayer.getServer()), serverPlayer.getServer());
        }
    }
}
