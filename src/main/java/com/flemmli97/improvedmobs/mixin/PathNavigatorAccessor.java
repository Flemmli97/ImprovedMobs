package com.flemmli97.improvedmobs.mixin;

import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathNavigator.class)
public interface PathNavigatorAccessor {

    @Accessor
    NodeProcessor getNodeProcessor();
}
