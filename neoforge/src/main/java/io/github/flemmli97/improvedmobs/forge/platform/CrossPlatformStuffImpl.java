package io.github.flemmli97.improvedmobs.forge.platform;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.forge.capability.Attachments;
import io.github.flemmli97.improvedmobs.network.PacketHandler;
import io.github.flemmli97.improvedmobs.network.S2CDiffcultyValue;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class CrossPlatformStuffImpl implements CrossPlatformStuff {

    @Override
    public void onPlayerOpen(BlockEntity blockEntity) {
        blockEntity.getData(Attachments.HAS_BEEN_OPENED.get())
                .setOpened(blockEntity);
    }

    @Override
    public boolean canLoot(BlockEntity blockEntity) {
        if (blockEntity.hasData(Attachments.HAS_BEEN_OPENED.get()))
            return blockEntity.getData(Attachments.HAS_BEEN_OPENED.get()).playerOpened();
        return false;
    }

    @Override
    public ItemStack lootRandomItem(BlockEntity blockEntity, RandomSource rand) {
        @Nullable IItemHandler cap = blockEntity.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), null);
        if (cap != null) {
            ItemStack drop = cap.extractItem(rand.nextInt(cap.getSlots()), 1, false);
            int tries = 0;
            while (drop.isEmpty() && tries < 10) {
                drop = cap.extractItem(rand.nextInt(cap.getSlots()), 1, false);
                tries++;
            }
            return drop;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.isLadder(entity.level(), pos, entity);
    }

    @Override
    public SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.getSoundType(entity.level(), pos, entity);
    }

    @Override
    public void sendClientboundPacket(CustomPacketPayload payload, ServerPlayer player) {
        if (player.connection.hasChannel(payload))
            player.connection.send(payload);
    }

    @Override
    public void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> {
            if (player.connection.hasChannel(S2CDiffcultyValue.TYPE)) {
                player.connection.send(PacketHandler.createDifficultyPacket(data, player));
            }
        });
    }

    @Override
    public Path configDirPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public AbstractArrow customBowArrow(BowItem item, ItemStack stack, AbstractArrow def) {
        return item.customArrow(def, stack);
    }

    @Override
    public boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker) {
        return attackingStack.canDisableShield(held, entity, attacker);
    }

    @Override
    public IPlayerDifficulty getPlayerDifficultyData(ServerPlayer player) {
        return player.getData(Attachments.PLAYER_DIFFICULTY.get());
    }
}
