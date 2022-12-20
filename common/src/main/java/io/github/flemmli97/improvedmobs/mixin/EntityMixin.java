package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.IEntityData;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityData {

    @Unique
    private final EntityFlags imFlags = new EntityFlags();

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void readData(CompoundTag compoundTag, CallbackInfo info) {
        this.imFlags.load(compoundTag.getCompound("IMFlags"));
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void saveData(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> info) {
        compoundTag.put(EntityFlags.TAG_ID, this.imFlags.save());
    }

    @Override
    public EntityFlags getFlags() {
        return this.imFlags;
    }
}
