package io.github.flemmli97.improvedmobs.common.entities.ai;

import io.github.flemmli97.improvedmobs.api.datapack.ItemUseLookupManager;
import io.github.flemmli97.improvedmobs.api.item.ItemUseHandler;
import io.github.flemmli97.improvedmobs.api.item.MoveHandler;
import io.github.flemmli97.improvedmobs.common.utils.EntityFlags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;

public class ItemUseGoal extends Goal {

    private final Mob living;
    private int cooldown = -1, attackDelay = -1;
    private ItemUseHandler ai;
    private InteractionHand hand;
    private MoveHandler moveHandler;
    private ItemStack stackMain, stackOff;

    public ItemUseGoal(Mob entity) {
        this.living = entity;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.living.getTarget();
        if (target == null || !target.isAlive() || target.getRandom().nextInt(10) != 0)
            return false;
        this.calculateAi();
        return this.ai != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.living.getTarget();
        if (target == null || !target.isAlive())
            return false;
        return this.ai != null && this.ai.canUse(this.living, this.living.getItemInHand(this.hand), true);
    }

    private void calculateAi() {
        Pair<ItemUseHandler, InteractionHand> pair = ItemUseLookupManager.getInstance().get(this.living);
        this.ai = pair.getKey();
        this.hand = pair.getValue();
        if (this.ai != null) {
            this.moveHandler = this.ai.movementType() != null ? this.ai.movementType().apply(this.living) : null;
            this.setFlags(this.ai.movementType() != null ? EnumSet.of(Flag.MOVE, Flag.LOOK) : EnumSet.noneOf(Flag.class));
        }
        this.stackMain = this.living.getMainHandItem();
        this.stackOff = this.living.getOffhandItem();
        this.living.getNavigation().stop();
        if (this.ai != null) {
            this.living.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.cooldown = -1;
        this.attackDelay = -1;
        this.living.stopUsingItem();
        this.ai = null;
        this.stackMain = null;
        this.stackOff = null;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public void tick() {
        // Move this check here cause ordering of different ai can change the state between #canContinueToUse and #tick
        if (this.stackMain != this.living.getMainHandItem() || this.stackOff != this.living.getOffhandItem()
                || this.ai == null || !this.ai.matches(this.living.getItemInHand(this.hand).getItem())) {
            this.stop();
            this.calculateAi();
            return;
        }
        if (EntityFlags.get(this.living).isShieldDisabled() && this.living.getItemInHand(this.hand).getUseAnimation() == UseAnim.BLOCK) {
            return;
        }
        LivingEntity target = this.living.getTarget();
        if (target != null) {
            boolean canSee = this.living.getSensing().hasLineOfSight(target);
            if (this.moveHandler != null)
                this.moveHandler.move(target, canSee);
            if (this.attackDelay < 0 && --this.cooldown < 0 && canSee) {
                this.ai.start(this.living, target, this.hand);
                this.attackDelay = this.ai.attackDelay(this.living, this.living.getItemInHand(this.hand));
            }
            if (--this.attackDelay == -1) {
                this.cooldown = this.ai.use(this.living, target, this.hand);
            } else if (this.attackDelay > 0) {
                this.ai.onPrepare(this.living, target, this.hand);
            }
        }
    }
}
