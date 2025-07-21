package io.github.flemmli97.improvedmobs.neoforge;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.ai.ItemAITasks;
import io.github.flemmli97.improvedmobs.api.datapack.EntityOverridesManager;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.common.config.EquipmentList;
import io.github.flemmli97.improvedmobs.common.config.holder.ConfigLoader;
import io.github.flemmli97.improvedmobs.common.config.holder.ConfigSpecs;
import io.github.flemmli97.improvedmobs.common.datapack.DifficultyAttributeConfig;
import io.github.flemmli97.improvedmobs.common.network.S2CDiffcultyValue;
import io.github.flemmli97.improvedmobs.common.network.S2CShowDifficulty;
import io.github.flemmli97.improvedmobs.neoforge.client.ClientEventHandler;
import io.github.flemmli97.improvedmobs.neoforge.events.DifficultyHandler;
import io.github.flemmli97.improvedmobs.neoforge.events.EventHandler;
import io.github.flemmli97.improvedmobs.neoforge.integration.difficulty.ScalingHealthDifficulty;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.File;

@Mod(value = ImprovedMobs.MODID)
public class ImprovedMobsNeoForge {

    public ImprovedMobsNeoForge(IEventBus modBus) {
        File file = FMLPaths.CONFIGDIR.get().resolve("improvedmobs").toFile();
        if (!file.exists())
            file.mkdir();
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, ConfigSpecs.CLIENT_SPEC, "improvedmobs/client.toml");
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, ConfigSpecs.COMMON_SPEC, "improvedmobs/common.toml");
        modBus.addListener(ImprovedMobsNeoForge::setup);
        modBus.addListener(ImprovedMobsNeoForge::conf);
        modBus.addListener(ImprovedMobsNeoForge::registerPackets);
        AttachmentsRegister.ATTACHMENT_TYPES.register(modBus);
        if (FMLEnvironment.dist == Dist.CLIENT)
            ClientEventHandler.setup(modBus);
        NeoForge.EVENT_BUS.register(new EventHandler());
        NeoForge.EVENT_BUS.addListener(ImprovedMobsNeoForge::serverStart);
        NeoForge.EVENT_BUS.addListener(ImprovedMobsNeoForge::addReloadListener);

        DifficultyFetcher.register();
        if (ModList.get().isLoaded("scalinghealth"))
            DifficultyFetcher.add(ResourceLocation.fromNamespaceAndPath(ImprovedMobs.MODID, "scalinghealth_integration"), new ScalingHealthDifficulty());
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
        PayloadRegistrar registrar = event.registrar(ImprovedMobs.MODID).optional();
        registrar.playToClient(S2CDiffcultyValue.TYPE, S2CDiffcultyValue.STREAM_CODEC, (pkt, ctx) -> S2CDiffcultyValue.handle(pkt));
        registrar.playToClient(S2CShowDifficulty.TYPE, S2CShowDifficulty.STREAM_CODEC, (pkt, ctx) -> S2CShowDifficulty.handle(pkt));
    }

    static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(DifficultyAttributeConfig.create(event.getRegistryAccess()));
        event.addListener(EntityOverridesManager.create(event.getRegistryAccess()));
    }
}
