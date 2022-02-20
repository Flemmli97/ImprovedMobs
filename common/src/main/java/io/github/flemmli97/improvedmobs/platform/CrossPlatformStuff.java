package io.github.flemmli97.improvedmobs.platform;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
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
import java.util.Random;

public abstract class CrossPlatformStuff {

    protected static CrossPlatformStuff INSTANCE;

    public static CrossPlatformStuff instance() {
        return INSTANCE;
    }

    public abstract void onPlayerOpen(BlockEntity blockEntity);

    public abstract boolean canLoot(BlockEntity blockEntity);

    public abstract ItemStack lootRandomItem(BlockEntity blockEntity, Random rand);

    public abstract boolean isLadder(BlockState state, LivingEntity entity, BlockPos pos);

    public abstract SoundType blockSound(BlockState state, LivingEntity entity, BlockPos pos);

    public abstract void sendDifficultyData(DifficultyData data, MinecraftServer server);

    public abstract Path configDirPath();

    public abstract AbstractArrow customBowArrow(BowItem item, AbstractArrow def);

    public abstract boolean canDisableShield(ItemStack attackingStack, ItemStack held, LivingEntity entity, LivingEntity attacker);
}
