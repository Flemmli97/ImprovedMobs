package io.github.flemmli97.improvedmobs.fabric;

import io.github.flemmli97.improvedmobs.commands.IMCommand;
import io.github.flemmli97.improvedmobs.events.EventCalls;
import io.github.flemmli97.improvedmobs.fabric.config.ConfigSpecs;
import io.github.flemmli97.improvedmobs.fabric.events.EventHandler;
import io.github.flemmli97.improvedmobs.fabric.network.PacketRegister;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ImprovedMobsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(EventCalls::tick);
        ServerWorldEvents.LOAD.register(EventHandler::worldLoad);
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> IMCommand.register(dispatcher));
        ServerEntityEvents.ENTITY_LOAD.register(EventHandler::onEntityLoad);
        UseBlockCallback.EVENT.register(EventHandler::openTile);
        UseEntityCallback.EVENT.register(EventHandler::equipPet);
        ServerPlayConnectionEvents.JOIN.register(EventHandler::worldJoin);
        ServerLifecycleEvents.SERVER_STARTING.register(EventHandler::serverStart);

        ConfigSpecs.initCommonConfig();
        PacketRegister.register();
    }
}
