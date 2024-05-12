package io.github.flemmli97.improvedmobs.forge;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.improvedmobs.client.ClientEvents;
import io.github.flemmli97.improvedmobs.config.Config;
import io.github.flemmli97.improvedmobs.config.EquipmentList;
import io.github.flemmli97.improvedmobs.forge.capability.Attachments;
import io.github.flemmli97.improvedmobs.forge.client.ClientEventHandler;
import io.github.flemmli97.improvedmobs.forge.config.ConfigLoader;
import io.github.flemmli97.improvedmobs.forge.config.ConfigSpecs;
import io.github.flemmli97.improvedmobs.forge.events.DifficultyHandler;
import io.github.flemmli97.improvedmobs.forge.events.EventHandler;
import io.github.flemmli97.improvedmobs.network.S2CDiffcultyValue;
import io.github.flemmli97.improvedmobs.network.S2CShowDifficulty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.File;

@Mod(value = ImprovedMobs.MODID)
public class ImprovedMobsForge {

    public ImprovedMobsForge(IEventBus modBus) {
        File file = FMLPaths.CONFIGDIR.get().resolve("improvedmobs").toFile();
        if (!file.exists())
            file.mkdir();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, ConfigSpecs.CLIENT_SPEC, "improvedmobs/client.toml");
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, ConfigSpecs.COMMON_SPEC, "improvedmobs/common.toml");
        modBus.addListener(ImprovedMobsForge::setup);
        modBus.addListener(ImprovedMobsForge::conf);
        modBus.addListener(ImprovedMobsForge::registerPackets);
        Attachments.ATTACHMENT_TYPES.register(modBus);
        if (FMLEnvironment.dist == Dist.CLIENT)
            ClientEventHandler.setup(modBus);
        NeoForge.EVENT_BUS.register(new EventHandler());
        NeoForge.EVENT_BUS.addListener(ImprovedMobsForge::serverStart);
    }

    static void setup(FMLCommonSetupEvent event) {
        ItemAITasks.initAI();
        NeoForge.EVENT_BUS.register(new DifficultyHandler());
    }

    static void serverStart(ServerStartedEvent event) {
        EquipmentList.initEquip(event.getServer().registryAccess());
    }

    static void conf(ModConfigEvent event) {
        if (event.getConfig().getSpec() == ConfigSpecs.CLIENT_SPEC)
            ConfigLoader.loadClient();
        else if (event.getConfig().getSpec() == ConfigSpecs.COMMON_SPEC)
            ConfigLoader.loadCommon();
    }

    static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ImprovedMobs.MODID);
        registrar.playToClient(S2CDiffcultyValue.TYPE, S2CDiffcultyValue.STREAM_CODEC, ImprovedMobsForge::difficultyHandlerPacket);
        registrar.playToClient(S2CShowDifficulty.TYPE, S2CShowDifficulty.STREAM_CODEC, ImprovedMobsForge::handleConfig);
    }

    private static void difficultyHandlerPacket(S2CDiffcultyValue pkt, IPayloadContext ctx) {
        ClientEvents.updateClientDifficulty(pkt.difficulty());
    }

    private static void handleConfig(S2CShowDifficulty pkt, IPayloadContext ctx) {
        Config.ClientConfig.showDifficultyServerSync = pkt.showDifficulty();
    }
}
