package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.ai.util.AIUtils;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThrownPotion.class)
public abstract class PotionEntityMixin {

    @Inject(method = "applySplash", at = @At(value = "HEAD"), cancellable = true)
    private void applyPotion(List<MobEffectInstance> list, @Nullable Entity entity, CallbackInfo info) {
        if (EntityFlags.get((ThrownPotion) (Object) this).isThrownEntity) {
            info.cancel();
            AIUtils.applyPotion(((ThrownPotion) (Object) this), list, entity);
        }
    }
}
