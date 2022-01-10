package io.github.flemmli97.improvedmobs.fabric.events;

import com.mojang.brigadier.CommandDispatcher;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.improvedmobs.commands.IMCommand;
import io.github.flemmli97.improvedmobs.config.EquipmentList;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.events.EventCalls;
import io.github.flemmli97.improvedmobs.fabric.ImprovedMobsFabric;
import io.github.flemmli97.improvedmobs.fabric.config.ConfigLoader;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EventHandler {

    public static final ResourceLocation tileCap = new ResourceLocation(ImprovedMobs.MODID, "opened_flag");

    public static void worldJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        ImprovedMobsFabric.sendDifficultyPacket(DifficultyData.get(server), handler.player);
    }

    public static void serverStart(MinecraftServer server) {
        ConfigLoader.loadCommon();
        ItemAITasks.initAI();
        try {
            EquipmentList.initEquip();
        } catch (EquipmentList.InvalidItemNameException e) {
            ImprovedMobs.logger.error(e.getMessage());
        }
    }

    public static void worldLoad(MinecraftServer server, ServerLevel world) {
        if (world.dimension() == Level.OVERWORLD)
            ConfigLoader.serverInit(world);
    }

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        IMCommand.register(dispatcher);
    }

    public static void onEntityLoad(Entity entity, ServerLevel world) {
        if (entity instanceof Mob mob)
            EventCalls.onEntityLoad(mob);
    }

    public static InteractionResult openTile(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        EventCalls.openTile(player, hitResult.getBlockPos());
        return InteractionResult.PASS;
    }

    public static InteractionResult equipPet(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (EventCalls.equipPet(player, hand, entity))
            return InteractionResult.CONSUME;
        return InteractionResult.PASS;
    }

    public static List<Entity> explosion(Explosion explosion, Entity source, List<Entity> affectedEntities) {
        EventCalls.explosion(explosion, source, affectedEntities);
        return affectedEntities;
    }
}
