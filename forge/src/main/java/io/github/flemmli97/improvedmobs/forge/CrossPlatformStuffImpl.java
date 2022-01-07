package io.github.flemmli97.improvedmobs.forge;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.forge.capability.TileCapProvider;
import io.github.flemmli97.improvedmobs.forge.network.PacketDifficulty;
import io.github.flemmli97.improvedmobs.forge.network.PacketHandler;
import io.github.flemmli97.improvedmobs.utils.ITileOpened;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class CrossPlatformStuffImpl {

    public static ITileOpened getTileData(BlockEntity blockEntity) {
        return blockEntity.getCapability(TileCapProvider.CAP).orElseThrow(() -> new NullPointerException("Capability null. This shouldn't be"));
    }

    public static boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.isLadder(entity.level, pos, entity);
    }

    public static SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos) {
        return state.getSoundType(entity.level, pos, entity);
    }

    public static void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        PacketHandler.sendToAll(new PacketDifficulty(data));
    }

    public static Path configDirPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static AbstractArrow customBowArrow(BowItem item, AbstractArrow def) {
        return item.customArrow(def);
    }

    public static boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker) {
        return attackingStack.canDisableShield(held, entity, attacker);
    }
}
