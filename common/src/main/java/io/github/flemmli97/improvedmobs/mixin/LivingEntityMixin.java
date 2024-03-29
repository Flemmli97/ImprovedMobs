package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.IClipContxt;
import io.github.flemmli97.improvedmobs.mixinhelper.LivingSensingExt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingSensingExt {

    @Unique
    private boolean improvedmobs_extended_los;

    @ModifyArg(method = "hasLineOfSight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private ClipContext test(ClipContext old) {
        if (this.improvedmobs_extended_los) {
            ((IClipContxt) old).checkSeeThrough();
            this.improvedmobs_extended_los = false;
        }
        return old;
    }

    @Override
    public void doExtendedLOSCheck() {
        this.improvedmobs_extended_los = true;
    }
}
