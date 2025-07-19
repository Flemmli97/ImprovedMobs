package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.LivingSensingExt;
import io.github.flemmli97.improvedmobs.mixinhelper.SensingExt;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Sensing.class)
public abstract class EntitySensingMixin implements SensingExt {

    @Final
    @Shadow
    private Mob mob;
    @Unique
    private final IntSet improvedMobs$seen = new IntOpenHashSet();
    @Unique
    private final IntSet improvedMobs$unseen = new IntOpenHashSet();
    @Unique
    private boolean improvedmobs_extended_los;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        this.improvedMobs$seen.clear();
        this.improvedMobs$unseen.clear();
    }

    @Inject(method = "hasLineOfSight", at = @At("HEAD"), cancellable = true)
    private void onHasLoS(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (this.improvedmobs_extended_los) {
            info.setReturnValue(this.hasLineOfSightExt(entity));
            this.improvedmobs_extended_los = false;
        }
    }

    /**
     * Does a custom LoS check for e.g. see through blocks. We use a custom method here instead of inject at the original due to caching.
     */
    private boolean hasLineOfSightExt(Entity entity) {
        int i = entity.getId();
        if (this.improvedMobs$seen.contains(i)) {
            return true;
        } else if (this.improvedMobs$unseen.contains(i)) {
            return false;
        } else {
            this.mob.level().getProfiler().push("hasLineOfSight");
            ((LivingSensingExt) this.mob).improvedMobs$doExtendedLOSCheck();
            boolean bl = this.mob.hasLineOfSight(entity);
            this.mob.level().getProfiler().pop();
            if (bl) {
                this.improvedMobs$seen.add(i);
            } else {
                this.improvedMobs$unseen.add(i);
            }
            return bl;
        }
    }

    @Override
    public void improvedMobs$doLineOfSightExt() {
        this.improvedmobs_extended_los = true;
    }
}
