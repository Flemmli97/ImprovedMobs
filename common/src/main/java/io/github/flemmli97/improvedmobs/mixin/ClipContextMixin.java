package io.github.flemmli97.improvedmobs.mixin;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.mixinhelper.SeeThroughContext;
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
public abstract class ClipContextMixin implements SeeThroughContext {

    @Unique
    private boolean improvedMobs$checkSeeThrough;

    @Inject(method = "getBlockShape", at = @At("HEAD"), cancellable = true)
    private void checkSeeThrough(BlockState blockState, BlockGetter level, BlockPos pos, CallbackInfoReturnable<VoxelShape> info) {
        if (this.improvedMobs$checkSeeThrough && blockState.is(ImprovedMobs.SEE_THROUGH))
            info.setReturnValue(Shapes.empty());
    }

    @Override
    public void improvedMobs$checkSeeThrough() {
        this.improvedMobs$checkSeeThrough = true;
    }
}
