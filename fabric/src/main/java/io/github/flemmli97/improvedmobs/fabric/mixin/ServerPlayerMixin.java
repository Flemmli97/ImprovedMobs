package io.github.flemmli97.improvedmobs.fabric.mixin;

import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IPlayerDifficulty {

    @Unique
    private float imDifficultyLevel;

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void loadData(CompoundTag compound, CallbackInfo info) {
        this.load(compound);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void saveData(CompoundTag compound, CallbackInfo info) {
        this.save(compound);
    }

    @Override
    public void setDifficultyLevel(float level) {
        this.imDifficultyLevel = level;
    }

    @Override
    public float getDifficultyLevel() {
        return this.imDifficultyLevel;
    }

    @Override
    public void load(CompoundTag nbt) {
        this.imDifficultyLevel = nbt.getFloat("IMDifficulty");
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putFloat("IMDifficulty", this.imDifficultyLevel);
        return compound;
    }
}
