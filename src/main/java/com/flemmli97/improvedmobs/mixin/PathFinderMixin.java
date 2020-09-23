package com.flemmli97.improvedmobs.mixin;

import net.minecraft.pathfinding.PathNavigator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PathNavigator.class)
public class PathFinderMixin {

    /*@Inject(method = "<init>", at = @At(value = "RETURN"))
    private void modifyFinder(MobEntity mob, World world){
        NewWalkNodeProcessor walkNode = new NewWalkNodeProcessor();
        walkNode.setBreakBlocks(mob.getTags().contains(EventHandler.breaker));
        walkNode.setCanEnterDoors(true);
        walkNode.setCanOpenDoors(true);
        ReflectionUtils.setFieldValue(this.nodeProcessor, event.getNavigator(), walkNode);
        event.setPathFinder(new PathFinder(walkNode));
    }*/
}
