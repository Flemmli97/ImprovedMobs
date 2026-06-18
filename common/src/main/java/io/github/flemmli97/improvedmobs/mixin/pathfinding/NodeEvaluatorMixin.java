package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.common.utils.Utils;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NodeEvaluator.class)
public abstract class NodeEvaluatorMixin implements NodeExtension {

    @Shadow
    protected Mob mob;

    @Override
    public boolean improvedMobs$canBreakBlocks() {
        return this.mob != null && Utils.canBreakBlocks(this.mob);
    }
}
