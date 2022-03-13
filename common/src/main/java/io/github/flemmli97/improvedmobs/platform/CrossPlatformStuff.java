package io.github.flemmli97.improvedmobs.platform;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import io.github.flemmli97.tenshilib.platform.InitUtil;
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

import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;

public interface CrossPlatformStuff {

    CrossPlatformStuff INSTANCE = InitUtil.getPlatformInstance(CrossPlatformStuff.class,
            "io.github.flemmli97.improvedmobs.fabric.platform.CrossPlatformStuffImpl",
            "io.github.flemmli97.improvedmobs.forge.platform.CrossPlatformStuffImpl");

    void onPlayerOpen(BlockEntity blockEntity);

    boolean canLoot(BlockEntity blockEntity);

    ItemStack lootRandomItem(BlockEntity blockEntity, Random rand);

    boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos);

    SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos);

    void sendDifficultyDataTo(ServerPlayer player, MinecraftServer server);

    void sendDifficultyData(DifficultyData data, MinecraftServer server);

    void sendConfigSync(ServerPlayer player);

    Path configDirPath();

    AbstractArrow customBowArrow(BowItem item, AbstractArrow def);

    boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker);

    Optional<IPlayerDifficulty> getPlayerDifficultyData(ServerPlayer player);
}
