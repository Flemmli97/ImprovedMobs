package io.github.flemmli97.improvedmobs.forge.platform;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import io.github.flemmli97.improvedmobs.forge.capability.TileCapProvider;
import io.github.flemmli97.improvedmobs.forge.network.PacketDifficulty;
import io.github.flemmli97.improvedmobs.forge.network.PacketHandler;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
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

public class CrossPlatformStuffImpl extends CrossPlatformStuff {

    public void init() {
        INSTANCE = new CrossPlatformStuffImpl();
    }

    @Override
    public ITileOpened getTileData(BlockEntity blockEntity) {
        return blockEntity.getCapability(TileCapProvider.CAP).orElseThrow(() -> new NullPointerException("Capability null. This shouldn't be. BlockEntite " + blockEntity));
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
    public void sendDifficultyData(DifficultyData data, MinecraftServer server) {
        PacketHandler.sendToAll(new PacketDifficulty(data), server);
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
}
