package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.ISpawnReason;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin implements ISpawnReason {

    @Unique
    private MobSpawnType improved_mobs_spawnreason;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void onFinalize(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData,
                            CallbackInfoReturnable<SpawnGroupData> info) {
        this.improved_mobs_spawnreason = spawnType;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readData(CompoundTag compoundTag, CallbackInfo info) {
        if (compoundTag.contains("MobSpawnReason"))
            this.improved_mobs_spawnreason = MobSpawnType.values()[compoundTag.getInt("MobSpawnReason")];
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveData(CompoundTag compoundTag, CallbackInfo info) {
        if (this.improved_mobs_spawnreason != null)
            compoundTag.putInt("MobSpawnReason", this.improved_mobs_spawnreason.ordinal());
    }

    @Override
    public MobSpawnType getSpawnReason() {
        return this.improved_mobs_spawnreason;
    }
}
