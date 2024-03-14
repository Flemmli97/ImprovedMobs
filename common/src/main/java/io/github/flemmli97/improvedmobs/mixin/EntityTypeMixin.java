package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.entities.ServersideRegister;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EntityType.class)
public class EntityTypeMixin {

    @Inject(method = "create(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private static void injectServerEntityOnLoad(CompoundTag compound, Level level, CallbackInfoReturnable<Optional<Entity>> info) {
        CompoundTag data = compound.getCompound(EntityFlags.TAG_ID);
        if (data.contains(EntityFlags.SERVER_ENTITY_TAG_ID)) {
            ResourceLocation id = new ResourceLocation(data.getString(EntityFlags.SERVER_ENTITY_TAG_ID));
            Optional<Entity> opt = ServersideRegister.createOf(id, level, compound);
            if (opt.isPresent())
                info.setReturnValue(opt);
        }
    }
}
