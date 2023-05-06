package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.mixinhelper.ITNTThrowable;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class TNTEntityMixin extends Entity implements ITNTThrowable {

    private TNTEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "RETURN"), cancellable = true)
    private void modifyExplosion(CallbackInfo info) {
        PrimedTnt tnt = (PrimedTnt) (Object) this;
        if (EntityFlags.get(tnt).isThrownEntity && tnt.getFuse() == 2) {
            info.cancel();
            tnt.remove(RemovalReason.KILLED);
            if (!tnt.level.isClientSide)
                tnt.level.explode(tnt, tnt.getX(), tnt.getY(0.0625D), tnt.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
        }
    }

    @Override
    public void shootFromEntity(Entity shooter, float pitch, float yaw, float delta, float velocity, float accuracy) {
        PrimedTnt tnt = (PrimedTnt) (Object) this;
        float x = -Mth.sin(yaw * (float) Math.PI / 180F) * Mth.cos(pitch * (float) Math.PI / 180F);
        float y = -Mth.sin((pitch + delta) * (float) Math.PI / 180F);
        float z = Mth.cos(yaw * (float) Math.PI / 180F) * Mth.cos(pitch * (float) Math.PI / 180F);
        Vec3 newMotion = new Vec3(x, y, z).normalize()
                .add(this.random.nextGaussian() * 0.0075F * accuracy, this.random.nextGaussian() * 0.0075F * accuracy, this.random.nextGaussian() * 0.0075F * accuracy).scale(velocity);
        tnt.setDeltaMovement(newMotion);
        double f3 = Math.sqrt(newMotion.x * newMotion.x + newMotion.z * newMotion.z);
        tnt.setYRot((float) (Mth.atan2(newMotion.x, newMotion.z) * (180F / (float) Math.PI)));
        tnt.setXRot((float) (Mth.atan2(newMotion.y, f3) * (180F / (float) Math.PI)));
        tnt.yRotO = tnt.getYRot();
        tnt.xRotO = tnt.getXRot();
        Vec3 shooterMotion = shooter.getDeltaMovement();
        tnt.setDeltaMovement(tnt.getDeltaMovement().add(shooterMotion.x, shooter.isOnGround() ? 0.0D : shooterMotion.y, shooterMotion.z));
    }
}
