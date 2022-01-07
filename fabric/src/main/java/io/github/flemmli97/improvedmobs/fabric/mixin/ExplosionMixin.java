package io.github.flemmli97.improvedmobs.fabric.mixin;

import io.github.flemmli97.improvedmobs.fabric.events.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    private Entity source;

    @ModifyVariable(method = "explode", at = @At(value = "STORE"))
    private List<Entity> removeUnaffected(List<Entity> list) {
        return EventHandler.explosion((Explosion) (Object) this, this.source, list);
    }
}
