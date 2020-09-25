package com.flemmli97.improvedmobs.mixin;

import com.flemmli97.improvedmobs.events.EventHandler;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.SwimNodeProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {

    @ModifyVariable(method = "getPathPriority", at = @At(value = "HEAD"), ordinal = 0)
    private PathNodeType path(PathNodeType type) {
        return this.getActualType(type);
    }

    private PathNodeType getActualType(PathNodeType type) {
        MobEntity e = ((MobEntity) (Object) this);
        if (((PathNavigatorAccessor) e.getNavigator()).getNodeProcessor() instanceof SwimNodeProcessor)
            return type;
        if (type == PathNodeType.BREACH && !e.getPersistentData().getBoolean(EventHandler.breaker))
            return PathNodeType.BLOCKED;
        return type;
    }
}
