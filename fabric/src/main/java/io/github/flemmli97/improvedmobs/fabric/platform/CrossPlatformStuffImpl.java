package io.github.flemmli97.improvedmobs.fabric.platform;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.fabric.ImprovedMobsFabric;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.utils.ITileOpened;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.Random;

public class CrossPlatformStuffImpl extends CrossPlatformStuff {

    public static void init() {
        INSTANCE = new CrossPlatformStuffImpl();
    }

    @Override
    public void onPlayerOpen(BlockEntity blockEntity) {
        ((ITileOpened) blockEntity).setOpened(blockEntity);
    }

    @Override
    public boolean canLoot(BlockEntity blockEntity) {
        if (blockEntity instanceof Container container)
            return ((ITileOpened) blockEntity).playerOpened() && !container.isEmpty();
        return false;
    }

    @Override
    public ItemStack lootRandomItem(BlockEntity blockEntity, Random rand) {
        if (blockEntity instanceof Container inv) {
            try {
                if (!inv.isEmpty()) {
                    ItemStack drop = inv.removeItem(rand.nextInt(inv.getContainerSize()), 1);
                    int tries = 0;
                    while (drop.isEmpty() && tries < 10) {
                        drop = inv.removeItem(rand.nextInt(inv.getContainerSize()), 1);
                        tries++;
                    }
                    return drop;
                }
                return ItemStack.EMPTY;
            } catch (Exception e) {
                ImprovedMobs.logger.error("#getSizeInventory and actual size of the inventory (" + inv + ") is not the same.");
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.is(BlockTags.CLIMBABLE);
    }

    @Override
    public SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.getSoundType();
    }

    @Override
    public void sendDifficultyDataTo(ServerPlayer player, MinecraftServer server) {
        ImprovedMobsFabric.sendDifficultyPacket(DifficultyData.get(server), player);
    }

    @Override
    public void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        ImprovedMobsFabric.sendDifficultyPacketToAll(data, server);
    }

    @Override
    public void sendConfigSync(ServerPlayer player) {
        ImprovedMobsFabric.sendConfigCync(player);
    }

    @Override
    public Path configDirPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public AbstractArrow customBowArrow(BowItem item, AbstractArrow def) {
        return def;
    }

    @Override
    public boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker) {
        return (attackingStack.getItem() instanceof AxeItem || attackingStack.is(FabricToolTags.AXES)) && held.getItem() instanceof ShieldItem;
    }

    @Override
    public IPlayerDifficulty getPlayerDifficultyData(ServerPlayer player) {
        return ((IPlayerDifficulty) player);
    }
}
