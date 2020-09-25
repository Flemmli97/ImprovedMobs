package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.config.Config;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeProcessor.class)
public abstract class PathFinderMixin {

    @Inject(method = "getCommonNodeType", at = @At(value = "HEAD"), cancellable = true)
    private static void canBreak(IBlockReader reader, BlockPos pos, CallbackInfoReturnable<PathNodeType> info) {
        if (Config.CommonConfig.breakableBlocks.canBreak(reader.getBlockState(pos))) {
            info.setReturnValue(PathNodeType.BREACH);
            info.cancel();
        }
    }

}
