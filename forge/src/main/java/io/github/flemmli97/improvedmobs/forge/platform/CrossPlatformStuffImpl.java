package io.github.flemmli97.improvedmobs.forge.platform;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.forge.capability.TileCapProvider;
import io.github.flemmli97.improvedmobs.forge.network.PacketHandler;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.utils.ITileOpened;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.items.CapabilityItemHandler;

import java.nio.file.Path;
import java.util.Random;

public class CrossPlatformStuffImpl extends CrossPlatformStuff {

    public static void init() {
        INSTANCE = new CrossPlatformStuffImpl();
    }

    @Override
    public void onPlayerOpen(BlockEntity blockEntity) {
        blockEntity.getCapability(TileCapProvider.CAP)
                .ifPresent(cap -> cap.setOpened(blockEntity));
    }

    @Override
    public boolean canLoot(BlockEntity blockEntity) {
        if (blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent())
            return blockEntity.getCapability(TileCapProvider.CAP).map(ITileOpened::playerOpened).orElse(false) &&
                    blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(cap -> {
                        for (int i = 0; i < cap.getSlots(); i++)
                            if (!cap.getStackInSlot(i).isEmpty())
                                return true;
                        return false;
                    }).orElse(false);
        return false;
    }

    @Override
    public ItemStack lootRandomItem(BlockEntity blockEntity, Random rand) {
        return blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .map(cap -> {
                    ItemStack drop = cap.extractItem(rand.nextInt(cap.getSlots()), 1, false);
                    int tries = 0;
                    while (drop.isEmpty() && tries < 10) {
                        drop = cap.extractItem(rand.nextInt(cap.getSlots()), 1, false);
                        tries++;
                    }
                    return drop;
                }).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.isLadder(entity.level, pos, entity);
    }

    @Override
    public SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.getSoundType(entity.level, pos, entity);
    }

    @Override
    public void sendDifficultyDataTo(ServerPlayer player, MinecraftServer server) {
        PacketHandler.sendDifficultyToClient(DifficultyData.get(player.getServer()), player);
    }

    @Override
    public void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        PacketHandler.sendDifficultyToAll(data, server);
    }

    @Override
    public void sendConfigSync(ServerPlayer player) {
        PacketHandler.sendConfigSync(player);
    }

    @Override
    public Path configDirPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public AbstractArrow customBowArrow(BowItem item, AbstractArrow def) {
        return item.customArrow(def);
    }

    @Override
    public boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker) {
        return attackingStack.canDisableShield(held, entity, attacker);
    }

    @Override
    public IPlayerDifficulty getPlayerDifficultyData(ServerPlayer player) {
        return player.getCapability(TileCapProvider.PLAYER_CAP).orElseThrow(() -> new NullPointerException("Player difficulty capability not present!!!"));
    }
}
