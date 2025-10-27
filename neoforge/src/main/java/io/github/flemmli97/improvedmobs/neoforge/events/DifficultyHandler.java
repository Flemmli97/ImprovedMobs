package io.github.flemmli97.improvedmobs.neoforge.events;

import io.github.flemmli97.improvedmobs.common.events.EventCalls;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import io.github.flemmli97.improvedmobs.neoforge.AttachmentsRegister;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class DifficultyHandler {

    @SubscribeEvent
    public void worldJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.getEntity().level().isClientSide) {
            player.getExistingData(AttachmentsRegister.PLAYER_DIFFICULTY.get())
                    .ifPresent(d -> ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(player)
                            .read(d.write(player.registryAccess()), player.registryAccess()));
            EventCalls.levelJoin(player, player.getServer());
        }
    }

    @SubscribeEvent
    public void increaseDifficulty(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            EventCalls.tick(level);
        }
    }
}
