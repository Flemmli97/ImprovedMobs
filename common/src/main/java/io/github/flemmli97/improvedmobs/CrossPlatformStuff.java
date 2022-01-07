package io.github.flemmli97.improvedmobs;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
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

import java.nio.file.Path;

public class CrossPlatformStuff {

    @ExpectPlatform
    public static ITileOpened getTileData(BlockEntity blockEntity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path configDirPath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static AbstractArrow customBowArrow(BowItem item, AbstractArrow def) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker) {
        throw new AssertionError();
    }
}
