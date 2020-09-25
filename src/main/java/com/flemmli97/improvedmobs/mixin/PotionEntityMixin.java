package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.utils.AIUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin {

    @Inject(method = "func_213888_a", at = @At(value = "HEAD"), cancellable = true)
    private void applyPotion(List<EffectInstance> list, @Nullable Entity entity, CallbackInfo info) {
        if (((PotionEntity) (Object) this).getPersistentData().contains(ImprovedMobs.thrownEntityID)) {
            info.cancel();
            AIUtils.applyPotion(((PotionEntity) (Object) this), list, entity);
        }
    }
}
