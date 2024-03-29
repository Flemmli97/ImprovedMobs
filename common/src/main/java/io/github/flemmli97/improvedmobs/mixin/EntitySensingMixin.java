package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.LivingSensingExt;
import io.github.flemmli97.improvedmobs.mixinhelper.SensingExt;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Sensing.class)
public abstract class EntitySensingMixin implements SensingExt {

    @Shadow
    private Mob mob;
    @Unique
    private IntSet improvedmobs_seen = new IntOpenHashSet();
    @Unique
    private final IntSet improvedmobs_unseen = new IntOpenHashSet();
    @Unique
    private boolean improvedmobs_extended_los;

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        this.improvedmobs_seen.clear();
        this.improvedmobs_unseen.clear();
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
        if (this.improvedmobs_seen.contains(i)) {
            return true;
        } else if (this.improvedmobs_unseen.contains(i)) {
            return false;
        } else {
            this.mob.level().getProfiler().push("hasLineOfSight");
            ((LivingSensingExt) this.mob).doExtendedLOSCheck();
            boolean bl = this.mob.hasLineOfSight(entity);
            this.mob.level().getProfiler().pop();
            if (bl) {
                this.improvedmobs_seen.add(i);
            } else {
                this.improvedmobs_unseen.add(i);
            }
            return bl;
        }
    }

    @Override
    public void doLineOfSightExt() {
        this.improvedmobs_extended_los = true;
    }
}
