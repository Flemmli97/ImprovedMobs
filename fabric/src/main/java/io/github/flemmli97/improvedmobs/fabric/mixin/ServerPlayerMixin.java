package io.github.flemmli97.improvedmobs.fabric.mixin;

import com.mojang.authlib.GameProfile;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.registry.ImprovedMobsAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    private ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    // Legacy handling
    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadData(CompoundTag tag, CallbackInfo info) {
        CompoundTag data = null;
        if (tag.contains("IMDifficulty")) {
            data = tag;
        } else if (tag.contains(ImprovedMobs.MODID + ":difficulty_data")) {
            data = tag.getCompound(ImprovedMobs.MODID + ":difficulty_data");
        }
        if (data != null) {
            ImprovedMobsAttachments.PLAYER_DIFFICULTY.get().get(this).read(data, this.registryAccess());
        }
    }
}
