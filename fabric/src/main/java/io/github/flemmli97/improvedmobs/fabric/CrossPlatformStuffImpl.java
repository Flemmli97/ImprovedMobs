package io.github.flemmli97.improvedmobs.fabric;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.fabric.network.PacketHandler;
import io.github.flemmli97.improvedmobs.utils.ITileOpened;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
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

public class CrossPlatformStuffImpl {

    public static ITileOpened getTileData(BlockEntity blockEntity) {
        return (ITileOpened) blockEntity;
    }

    public static boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.is(BlockTags.CLIMBABLE);
    }

    public static SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.getSoundType();
    }

    public static void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        PacketHandler.sendDifficultyPacketToAll(data, server);
    }

    public static Path configDirPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static AbstractArrow customBowArrow(BowItem item, AbstractArrow def) {
        return def;
    }

    public static boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker) {
        return (attackingStack.getItem() instanceof AxeItem || attackingStack.is(FabricToolTags.AXES)) && held.getItem() instanceof ShieldItem;
    }
}
