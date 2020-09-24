package com.flemmli97.improvedmobs.events;

import com.flemmli97.improvedmobs.config.Config;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.flemmli97.improvedmobs.network.PacketDifficulty;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DifficultyHandler {

    @SubscribeEvent
    public void worldJoin(EntityJoinWorldEvent event) {
        if(event.getEntity() instanceof PlayerEntity && !event.getEntity().world.isRemote){
            PacketHandler.sendToClient(new PacketDifficulty(DifficultyData.get(event.getEntity().world)), (ServerPlayerEntity) event.getEntity());
        }
    }

    @SubscribeEvent
    public void increaseDifficulty(TickEvent.WorldTickEvent e) {
        if(e.phase == TickEvent.Phase.END && !e.world.isRemote && e.world.getRegistryKey() == World.OVERWORLD){
            boolean shouldIncrease = (Config.ServerConfig.ignorePlayers || !e.world.getServer().getPlayerList().getPlayers().isEmpty()) && e.world.getGameTime() > Config.ServerConfig.difficultyDelay;
            DifficultyData data = DifficultyData.get(e.world);
            if(Config.ServerConfig.shouldPunishTimeSkip){
                long timeDiff = (int) Math.abs(e.world.getGameTime() - data.getPrevTime());
                if(timeDiff > 2400){
                    long i = timeDiff / 2400;
                    if(timeDiff - i * 2400 < (i + 1) * 2400 - timeDiff)
                        i *= 2400;
                    else
                        i *= 2400 + 2400;
                    data.increaseDifficultyBy((ServerWorld) e.world, shouldIncrease ? Config.ServerConfig.doIMDifficulty ? i / 24000F : 0 : 0, e.world.getGameTime());
                    //data.increaseDifficultyBy(shouldIncrease ? e.world.getGameRules().getBoolean("doIMDifficulty") ? i / 24000F : 0 : 0, e.world.getGameTime());
                }
            }else{
                if(e.world.getGameTime() - data.getPrevTime() > 2400){
                    data.increaseDifficultyBy((ServerWorld) e.world, shouldIncrease ? Config.ServerConfig.doIMDifficulty  ? 0.1F : 0 : 0, e.world.getGameTime());
                    //data.increaseDifficultyBy(shouldIncrease ? e.world.getGameRules().getBoolean("doIMDifficulty") ? 0.1F : 0 : 0, e.world.getGameTime());
                }
            }
        }
    }
}
