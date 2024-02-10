package io.github.flemmli97.improvedmobs.forge.events;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.events.EventCalls;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DifficultyHandler {

    @SubscribeEvent
    public void worldJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.getEntity().level().isClientSide) {
            EventCalls.worldJoin(player, player.getServer());
        }
    }

    @SubscribeEvent
    public void increaseDifficulty(TickEvent.LevelTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.level instanceof ServerLevel world) {
            EventCalls.tick(world);
        }
    }

    @SubscribeEvent
    public void readOnDeath(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            boolean rev = CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) event.getOriginal()).isPresent();
            if (!rev)
                event.getOriginal().reviveCaps();
            CrossPlatformStuff.INSTANCE.getPlayerDifficultyData(serverPlayer)
                    .ifPresent(data -> data.setDifficultyLevel(CrossPlatformStuff.INSTANCE.getPlayerDifficultyData((ServerPlayer) event.getOriginal())
                            .map(IPlayerDifficulty::getDifficultyLevel).orElse(0f)));
            CrossPlatformStuff.INSTANCE.sendDifficultyData(DifficultyData.get(serverPlayer.getServer()), serverPlayer.getServer());
            if (!rev)
                event.getOriginal().invalidateCaps();
        }
    }
}
