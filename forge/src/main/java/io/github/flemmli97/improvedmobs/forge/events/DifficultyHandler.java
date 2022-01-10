package io.github.flemmli97.improvedmobs.forge.events;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.events.EventCalls;
import io.github.flemmli97.improvedmobs.forge.network.PacketDifficulty;
import io.github.flemmli97.improvedmobs.forge.network.PacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DifficultyHandler {

    @SubscribeEvent
    public void worldJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide) {
            PacketHandler.sendToClient(new PacketDifficulty(DifficultyData.get(event.getEntity().getServer())), (ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public void increaseDifficulty(TickEvent.WorldTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.world instanceof ServerLevel world && world.dimension() == Level.OVERWORLD) {
            EventCalls.increaseDifficulty(world);
        }
    }
}