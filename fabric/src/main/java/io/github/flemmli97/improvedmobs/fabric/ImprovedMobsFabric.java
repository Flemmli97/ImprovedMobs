package io.github.flemmli97.improvedmobs.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.datapack.EntityOverridesManager;
import io.github.flemmli97.improvedmobs.api.datapack.ItemUseLookupManager;
import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.api.item.ItemUseRegistry;
import io.github.flemmli97.improvedmobs.common.commands.ImprovedMobsCommand;
import io.github.flemmli97.improvedmobs.common.config.holder.ConfigLoader;
import io.github.flemmli97.improvedmobs.common.config.holder.ConfigSpecs;
import io.github.flemmli97.improvedmobs.common.datapack.DifficultyAttributeConfig;
import io.github.flemmli97.improvedmobs.common.events.EventCalls;
import io.github.flemmli97.improvedmobs.common.network.S2CDiffcultyValue;
import io.github.flemmli97.improvedmobs.common.network.S2CShowDifficulty;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import io.github.flemmli97.improvedmobs.fabric.events.EventHandler;
import io.github.flemmli97.improvedmobs.fabric.integration.difficulty.LevelZDifficulty;
import io.github.flemmli97.improvedmobs.fabric.integration.difficulty.PlayerEXDifficulty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.config.ModConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ImprovedMobsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ImprovedMobsAttachments.ATTACHMENTS.registerContent();
        ItemUseRegistry.initBuiltin();
        ServerTickEvents.END_WORLD_TICK.register(EventCalls::tick);
        ServerWorldEvents.LOAD.register(EventHandler::worldLoad);
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> ImprovedMobsCommand.register(dispatcher));
        ServerEntityEvents.ENTITY_LOAD.register(EventHandler::onEntityLoad);
        UseBlockCallback.EVENT.register(EventHandler::openTile);
        UseEntityCallback.EVENT.register(EventHandler::equipPet);
        ServerPlayConnectionEvents.JOIN.register(EventHandler::worldJoin);

        registerPacket();
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

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(DifficultyAttributeConfig.ID, reg -> new IdentifiableResourceReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return DifficultyAttributeConfig.create(reg)
                        .reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }

            @Override
            public ResourceLocation getFabricId() {
                return DifficultyAttributeConfig.ID;
            }
        });
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(EntityOverridesManager.ID, reg -> new IdentifiableResourceReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return EntityOverridesManager.create(reg)
                        .reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }

            @Override
            public ResourceLocation getFabricId() {
                return EntityOverridesManager.ID;
            }
        });
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ItemUseLookupManager.ID, reg -> new IdentifiableResourceReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return ItemUseLookupManager.create(reg).reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }

            @Override
            public ResourceLocation getFabricId() {
                return ItemUseLookupManager.ID;
            }
        });
        if (FabricLoader.getInstance().isModLoaded("playerex"))
            DifficultyFetcher.add(ImprovedMobs.modRes("player_ex_integration"), new PlayerEXDifficulty());
        if (FabricLoader.getInstance().isModLoaded("levelz"))
            DifficultyFetcher.add(ImprovedMobs.modRes("level_z_integration"), new LevelZDifficulty());
    }

    public static void registerPacket() {
        PayloadTypeRegistry.playS2C().register(S2CDiffcultyValue.TYPE, S2CDiffcultyValue.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CShowDifficulty.TYPE, S2CShowDifficulty.STREAM_CODEC);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(S2CDiffcultyValue.TYPE, (pkt, ctx) -> S2CDiffcultyValue.handle(pkt));
            ClientPlayNetworking.registerGlobalReceiver(S2CShowDifficulty.TYPE, (pkt, ctx) -> S2CShowDifficulty.handle(pkt));
        }
    }
}
