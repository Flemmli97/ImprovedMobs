package com.flemmli97.improvedmobs.events;

import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DifficultyHandler {

    @SubscribeEvent
    public void worldJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity && !event.getEntity().world.isRemote) {
            PacketHandler.sendDifficultyToClient(DifficultyData.get(event.getEntity().world), (ServerPlayerEntity) event.getEntity());
        }
    }

    @SubscribeEvent
    public void increaseDifficulty(TickEvent.WorldTickEvent e) {
        if (e.phase == TickEvent.Phase.END && !e.world.isRemote && e.world.getDimensionKey() == World.OVERWORLD) {
            boolean shouldIncrease = (Config.CommonConfig.ignorePlayers || !e.world.getServer().getPlayerList().getPlayers().isEmpty()) && e.world.getDayTime() > Config.CommonConfig.difficultyDelay;
            DifficultyData data = DifficultyData.get(e.world);
            if (Config.CommonConfig.shouldPunishTimeSkip) {
                long timeDiff = (int) Math.abs(e.world.getDayTime() - data.getPrevTime());
                if (timeDiff > 2400) {
                    long i = timeDiff / 2400;
                    if (timeDiff - i * 2400 > (i + 1) * 2400 - timeDiff)
                        i += 1;
                    while (i > 0) {
                        data.increaseDifficultyBy(current -> shouldIncrease ? Config.CommonConfig.doIMDifficulty ? Config.CommonConfig.increaseHandler.get(current) : 0 : 0, e.world.getDayTime(), e.world.getServer());
                        i--;
                    }//data.increaseDifficultyBy(shouldIncrease ? e.world.getGameRules().getBoolean("doIMDifficulty") ? i / 24000F : 0 : 0, e.world.getDayTime());
                }
            } else {
                if (e.world.getDayTime() - data.getPrevTime() > 2400) {
                    data.increaseDifficultyBy(current -> shouldIncrease ? Config.CommonConfig.doIMDifficulty ? Config.CommonConfig.increaseHandler.get(current) : 0 : 0, e.world.getDayTime(), e.world.getServer());
                    //data.increaseDifficultyBy(shouldIncrease ? e.world.getGameRules().getBoolean("doIMDifficulty") ? 0.1F : 0 : 0, e.world.getDayTime());
                }
            }
        }
    }
}
