package io.github.flemmli97.improvedmobs.fabric.platform;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.fabric.ImprovedMobsFabric;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
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

public class CrossPlatformStuffImpl extends CrossPlatformStuff {

    public static void init() {
        INSTANCE = new CrossPlatformStuffImpl();
    }

    @Override
    public ITileOpened getTileData(BlockEntity blockEntity) {
        return (ITileOpened) blockEntity;
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
    public void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        ImprovedMobsFabric.sendDifficultyPacketToAll(data, server);
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
}
