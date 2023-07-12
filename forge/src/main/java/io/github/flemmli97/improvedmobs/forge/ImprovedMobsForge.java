package io.github.flemmli97.improvedmobs.forge;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.improvedmobs.config.EquipmentList;
import io.github.flemmli97.improvedmobs.forge.capability.TileCapProvider;
import io.github.flemmli97.improvedmobs.forge.client.ClientEventHandler;
import io.github.flemmli97.improvedmobs.forge.config.ConfigLoader;
import io.github.flemmli97.improvedmobs.forge.config.ConfigSpecs;
import io.github.flemmli97.improvedmobs.forge.events.DifficultyHandler;
import io.github.flemmli97.improvedmobs.forge.events.EventHandler;
import io.github.flemmli97.improvedmobs.forge.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod.EventBusSubscriber
@Mod(value = ImprovedMobs.MODID)
public class ImprovedMobsForge {

    public ImprovedMobsForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "*", (s1, s2) -> true));
        File file = FMLPaths.CONFIGDIR.get().resolve("improvedmobs").toFile();
        if (!file.exists())
            file.mkdir();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigSpecs.clientSpec, "improvedmobs/client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigSpecs.commonSpec, "improvedmobs/common.toml");
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ImprovedMobsForge::setup);
        modBus.addListener(ImprovedMobsForge::conf);
        modBus.addListener(TileCapProvider::register);
        if (FMLEnvironment.dist == Dist.CLIENT)
            modBus.addListener(ClientEventHandler::setup);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    static void setup(FMLCommonSetupEvent event) {
        PacketHandler.register();
        ItemAITasks.initAI();
        try {
            EquipmentList.initEquip();
        } catch (EquipmentList.InvalidItemNameException e) {
            ImprovedMobs.logger.error(e.getMessage());
        }
        MinecraftForge.EVENT_BUS.register(new DifficultyHandler());
    }

    static void conf(ModConfigEvent event) {
        if (event.getConfig().getSpec() == ConfigSpecs.clientSpec)
            ConfigLoader.loadClient();
        else if (event.getConfig().getSpec() == ConfigSpecs.commonSpec)
            ConfigLoader.loadCommon();
    }
}
