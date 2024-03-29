package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.mixinhelper.IClipContxt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClipContext.class)
public abstract class ClipContextMixin implements IClipContxt {

    @Unique
    private boolean IM_checkSeeThrough;

    @Inject(method = "getBlockShape", at = @At("HEAD"), cancellable = true)
    private void checkSeeThrough(BlockState blockState, BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> info) {
        if (this.IM_checkSeeThrough && blockState.is(ImprovedMobs.SEE_THROUGH))
            info.setReturnValue(Shapes.empty());
    }

    @Override
    public void checkSeeThrough() {
        this.IM_checkSeeThrough = true;
    }
}
