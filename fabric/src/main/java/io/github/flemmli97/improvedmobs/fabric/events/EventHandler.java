package io.github.flemmli97.improvedmobs.fabric.events;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.ai.ItemAITasks;
import io.github.flemmli97.improvedmobs.common.config.equipment.EquipmentList;
import io.github.flemmli97.improvedmobs.common.config.holder.ConfigLoader;
import io.github.flemmli97.improvedmobs.common.events.EventCalls;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
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

    public static final ResourceLocation tileCap = ImprovedMobs.modRes("opened_flag");

    public static void worldJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        EventCalls.levelJoin(handler.player, server);
    }

    public static void serverStart(MinecraftServer server) {
        ItemAITasks.initAI();
        EquipmentList.initEquip(server.registryAccess());
    }

    public static void worldLoad(MinecraftServer server, ServerLevel world) {
        if (world.dimension() == Level.OVERWORLD)
            ConfigLoader.serverInit(world);
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
