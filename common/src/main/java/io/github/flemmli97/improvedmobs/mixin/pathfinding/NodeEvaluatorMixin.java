package io.github.flemmli97.improvedmobs.mixin.pathfinding;

import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import io.github.flemmli97.improvedmobs.mixinhelper.NodeExtension;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NodeEvaluator.class)
public abstract class NodeEvaluatorMixin implements NodeExtension {

    @Shadow
    protected Mob mob;

    @Override
    public boolean improvedMobs$canBreakBlocks() {
        if (this.mob == null || (this.mob.getTarget() == null && !Config.CommonConfig.idleBreak))
            return false;
        return EntityFlags.get(this.mob).canBreakBlocks == EntityFlags.FlagType.TRUE
                && this.mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    @Override
    public boolean improvedMobs$canClimb() {
        return EntityFlags.get(this.mob).ladderClimber;
    }
}
