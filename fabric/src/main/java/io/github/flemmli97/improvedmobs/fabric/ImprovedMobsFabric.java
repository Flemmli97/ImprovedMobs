package io.github.flemmli97.improvedmobs.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.commands.IMCommand;
import io.github.flemmli97.improvedmobs.config.holder.ConfigLoader;
import io.github.flemmli97.improvedmobs.config.holder.ConfigSpecs;
import io.github.flemmli97.improvedmobs.events.EventCalls;
import io.github.flemmli97.improvedmobs.fabric.events.EventHandler;
import io.github.flemmli97.improvedmobs.fabric.integration.difficulty.LevelZDifficulty;
import io.github.flemmli97.improvedmobs.fabric.integration.difficulty.PlayerEXDifficulty;
import io.github.flemmli97.improvedmobs.fabric.network.PacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.config.ModConfig;

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

        PacketHandler.register();
        NeoForgeModConfigEvents.loading(ImprovedMobs.MODID).register(config -> {
            if (config.getSpec() == ConfigSpecs.CLIENT_SPEC)
                ConfigLoader.loadClient();
            if (config.getSpec() == ConfigSpecs.COMMON_SPEC)
                ConfigLoader.loadCommon();
        });
        NeoForgeModConfigEvents.reloading(ImprovedMobs.MODID).register(config -> {
            if (config.getSpec() == ConfigSpecs.CLIENT_SPEC)
                ConfigLoader.loadClient();
            if (config.getSpec() == ConfigSpecs.COMMON_SPEC)
                ConfigLoader.loadCommon();
        });
        NeoForgeConfigRegistry.INSTANCE.register(ImprovedMobs.MODID, ModConfig.Type.CLIENT, ConfigSpecs.CLIENT_SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(ImprovedMobs.MODID, ModConfig.Type.COMMON, ConfigSpecs.COMMON_SPEC);
        DifficultyFetcher.register();
        if (FabricLoader.getInstance().isModLoaded("playerex"))
            DifficultyFetcher.add(ImprovedMobs.modRes("player_ex_integration"), new PlayerEXDifficulty());
        if (FabricLoader.getInstance().isModLoaded("levelz"))
            DifficultyFetcher.add(ImprovedMobs.modRes("level_z_integration"), new LevelZDifficulty());
    }
}
