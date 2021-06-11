package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.ImprovedMobs;
import com.flemmli97.improvedmobs.utils.ITNTThrowable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TNTEntity.class)
public abstract class TNTEntityMixin extends Entity implements ITNTThrowable {

    @Shadow
    private int fuse;

    private TNTEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "RETURN"), cancellable = true)
    private void modifyExplosion(CallbackInfo info) {
        TNTEntity tnt = (TNTEntity) (Object) this;
        if (tnt.getPersistentData().contains(ImprovedMobs.thrownEntityID) && this.fuse == 2) {
            info.cancel();
            tnt.remove();
            if (!tnt.world.isRemote)
                tnt.world.createExplosion(tnt, tnt.getPosX(), tnt.getPosYHeight(0.0625D), tnt.getPosZ(), 4.0F, Explosion.Mode.BREAK);
        }
    }

    @Override
    public void shootFromEntity(Entity shooter, float pitch, float yaw, float delta, float velocity, float accuracy) {
        TNTEntity tnt = (TNTEntity) (Object) this;
        float x = -MathHelper.sin(yaw * (float) Math.PI / 180F) * MathHelper.cos(pitch * (float) Math.PI / 180F);
        float y = -MathHelper.sin((pitch + delta) * (float) Math.PI / 180F);
        float z = MathHelper.cos(yaw * (float) Math.PI / 180F) * MathHelper.cos(pitch * (float) Math.PI / 180F);
        Vector3d newMotion = new Vector3d(x, y, z).normalize()
                .add(this.rand.nextGaussian() * 0.0075F * accuracy, this.rand.nextGaussian() * 0.0075F * accuracy, this.rand.nextGaussian() * 0.0075F * accuracy).scale(velocity);
        tnt.setMotion(newMotion);
        float f3 = MathHelper.sqrt(newMotion.x * newMotion.x + newMotion.z * newMotion.z);
        tnt.rotationYaw = (float) (MathHelper.atan2(newMotion.x, newMotion.z) * (180F / (float) Math.PI));
        tnt.rotationPitch = (float) (MathHelper.atan2(newMotion.y, f3) * (180F / (float) Math.PI));
        tnt.prevRotationYaw = tnt.rotationYaw;
        tnt.prevRotationPitch = tnt.rotationPitch;
        Vector3d shooterMotion = shooter.getMotion();
        tnt.setMotion(tnt.getMotion().add(shooterMotion.x, shooter.isOnGround() ? 0.0D : shooterMotion.y, shooterMotion.z));
    }
}
