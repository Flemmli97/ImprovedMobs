package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.EntitySpawnReason;
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
public abstract class MobMixin implements EntitySpawnReason {

    @Unique
    private MobSpawnType improvedmobs$spawnreason;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void onFinalize(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData,
                            CallbackInfoReturnable<SpawnGroupData> info) {
        this.improvedmobs$spawnreason = spawnType;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readData(CompoundTag compoundTag, CallbackInfo info) {
        if (compoundTag.contains("MobSpawnReason"))
            this.improvedmobs$spawnreason = MobSpawnType.values()[compoundTag.getInt("MobSpawnReason")];
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveData(CompoundTag compoundTag, CallbackInfo info) {
        if (this.improvedmobs$spawnreason != null)
            compoundTag.putInt("MobSpawnReason", this.improvedmobs$spawnreason.ordinal());
    }

    @Override
    public MobSpawnType improvedMobs$getSpawnReason() {
        return this.improvedmobs$spawnreason;
    }
}
