package io.github.flemmli97.improvedmobs.fabric.mixin;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.difficulty.PlayerDifficulty;
import io.github.flemmli97.improvedmobs.fabric.mixinutil.PlayerDifficultyAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerDifficultyAccess {

    @Unique
    private final PlayerDifficulty improvedmobs$Difficulty = new PlayerDifficulty();

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadData(CompoundTag compound, CallbackInfo info) {
        CompoundTag data;
        if (compound.contains("IMDifficulty")) {
            data = compound;
        } else {
            data = compound.getCompound(ImprovedMobs.MODID + ":container");
        }
        this.improvedmobs$Difficulty.load(data);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveData(CompoundTag compound, CallbackInfo info) {
        compound.put(ImprovedMobs.MODID + ":difficulty_data", this.improvedmobs$Difficulty.save(new CompoundTag()));
    }

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void copyOld(ServerPlayer oldPlayer, boolean alive, CallbackInfo info) {
        this.improvedmobs$Difficulty.copyFrom(((PlayerDifficultyAccess) oldPlayer).improvedMobs$getDifficulty());
    }

    @Override
    public PlayerDifficulty improvedMobs$getDifficulty() {
        return this.improvedmobs$Difficulty;
    }
}
