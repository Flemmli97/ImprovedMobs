package io.github.flemmli97.improvedmobs.fabric.mixin;

import io.github.flemmli97.improvedmobs.events.EventCalls;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "HEAD"), argsOnly = true)
    private float onDamage(float amount, DamageSource source) {
        return EventCalls.hurtEvent((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void livingTick(CallbackInfo info) {
        EventCalls.entityTick((LivingEntity) (Object) this);
    }

    @Inject(method = "hurt", at = @At(value = "HEAD"), cancellable = true)
    private void hurtCheck(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (EventCalls.onAttackEvent((LivingEntity) (Object) this, source)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
