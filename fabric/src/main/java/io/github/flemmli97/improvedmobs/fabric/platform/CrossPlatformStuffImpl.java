package io.github.flemmli97.improvedmobs.fabric.platform;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.improvedmobs.network.PacketHandler;
import io.github.flemmli97.improvedmobs.network.S2CDiffcultyValue;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.improvedmobs.utils.ContainerOpened;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;

public class CrossPlatformStuffImpl implements CrossPlatformStuff {

    public static final TagKey<Item> fabricAxe = TagKey.create(Registries.ITEM, new ResourceLocation("fabric", "axes"));
    public static final TagKey<Item> commonAxe = TagKey.create(Registries.ITEM, new ResourceLocation("c", "axes"));

    @Override
    public void onPlayerOpen(BlockEntity blockEntity) {
        ((ContainerOpened) blockEntity).setOpened(blockEntity);
    }

    @Override
    public boolean canLoot(BlockEntity blockEntity) {
        if (blockEntity instanceof Container container)
            return ((ContainerOpened) blockEntity).playerOpened() && !container.isEmpty();
        return false;
    }

    @Override
    public ItemStack lootRandomItem(BlockEntity blockEntity, RandomSource rand) {
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
                ImprovedMobs.LOGGER.error("#getSizeInventory and actual size of the inventory (" + inv + ") is not the same.");
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
    public void sendClientboundPacket(CustomPacketPayload payload, ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, payload.type()))
            ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        PlayerLookup.all(server).forEach(player -> {
            if (ServerPlayNetworking.canSend(player, S2CDiffcultyValue.TYPE))
                ServerPlayNetworking.send(player, PacketHandler.createDifficultyPacket(data, player));
        });
    }

    @Override
    public Path configDirPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public AbstractArrow customBowArrow(BowItem item, ItemStack stack, AbstractArrow def) {
        return def;
    }

    @Override
    public boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker) {
        return (attackingStack.getItem() instanceof AxeItem || attackingStack.is(fabricAxe) || attackingStack.is(commonAxe)) && held.getItem() instanceof ShieldItem;
    }

    @Override
    public IPlayerDifficulty getPlayerDifficultyData(ServerPlayer player) {
        return (IPlayerDifficulty) player;
    }
}
