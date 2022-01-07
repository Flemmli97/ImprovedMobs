package io.github.flemmli97.improvedmobs.ai;

import io.github.flemmli97.improvedmobs.ai.util.ItemAI;
import io.github.flemmli97.improvedmobs.ai.util.ItemAITasks;
import io.github.flemmli97.improvedmobs.utils.EntityFlags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;

public class ItemUseGoal extends Goal {

    private final Mob living;
    private final float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise, strafingBackwards;
    private int strafingTime = -1;
    private ItemAI ai;
    private InteractionHand hand;
    private ItemStack stackMain, stackOff;

    public ItemUseGoal(Mob entity, float maxDistance) {
        this.living = entity;
        float follow = maxDistance;
        if (entity.getAttribute(Attributes.FOLLOW_RANGE) != null)
            follow = (float) entity.getAttribute(Attributes.FOLLOW_RANGE).getValue();
        maxDistance = Math.min(follow - 3, maxDistance);
        this.maxAttackDistance = maxDistance * maxDistance;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.living.getTarget();
        if (target == null || !target.isAlive() || target.getRandom().nextInt(10) != 0)
            return false;
        Pair<ItemAI, InteractionHand> pair = ItemAITasks.getAI(this.living);
        this.ai = pair.getKey();
        this.hand = pair.getValue();
        return this.ai != null;
    }

    @Override
    public void start() {
        this.setFlags(this.ai.type() != ItemAI.ItemType.NONSTRAFINGITEM ? EnumSet.of(Flag.MOVE, Flag.LOOK) : EnumSet.noneOf(Flag.class));
        this.stackMain = this.living.getMainHandItem();
        this.stackOff = this.living.getOffhandItem();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.living.getTarget();
        if (target == null || !target.isAlive())
            return false;
        if (this.stackMain != this.living.getMainHandItem() || this.stackOff != this.living.getOffhandItem()) {
            Pair<ItemAI, InteractionHand> pair = ItemAITasks.getAI(this.living);
            this.ai = pair.getKey();
            this.hand = pair.getValue();
        }
        return this.ai != null;
    }

    @Override
    public void stop() {
        this.seeTime = 0;
        this.attackTime = -1;
        this.living.stopUsingItem();
        //this.ai.onReset(this.living, this.hand);
        this.ai = null;
        this.stackMain = null;
        this.stackOff = null;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public void tick() {
        if (EntityFlags.get(this.living).isShieldDisabled() && this.living.getItemInHand(this.hand).getUseAnimation() == UseAnim.BLOCK) {
            return;
        }
        LivingEntity target = this.living.getTarget();
        if (target != null) {
            boolean flag = this.living.getSensing().hasLineOfSight(target);
            if (this.ai.type() == ItemAI.ItemType.STRAFINGITEM)
                this.moveStrafing(target, flag);
            else if (this.ai.type() == ItemAI.ItemType.STANDING)
                this.moveToRange(target, flag);
            if (this.living.isUsingItem() || !this.ai.useHand()) {
                if (!flag && this.seeTime < -60) {
                    this.living.stopUsingItem();
                    this.ai.onReset(this.living, this.hand);
                } else if (flag) {
                    if (this.ai.useHand()) {
                        int i = this.living.getTicksUsingItem();
                        if (i >= this.ai.maxUseCount(this.living, this.hand)) {
                            this.living.releaseUsingItem();
                            this.ai.attack(this.living, target, this.hand);
                            this.attackTime = this.ai.cooldown();
                        }
                    } else if (--this.attackTime <= 0) {
                        this.ai.attack(this.living, target, this.hand);
                        this.living.releaseUsingItem();
                        this.attackTime = this.ai.cooldown();
                    }
                }
            } else if (--this.attackTime < 0 && this.seeTime >= -60) {
                this.living.startUsingItem(this.hand);
            }
        }
    }

    private void moveStrafing(LivingEntity target, boolean canSee) {
        double dist = this.living.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean flag1 = this.seeTime > 0;
        if (canSee != flag1)
            this.seeTime = 0;
        if (canSee)
            ++this.seeTime;
        else
            --this.seeTime;
        if (dist <= this.maxAttackDistance && this.seeTime >= 20) {
            this.living.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.living.getNavigation().moveTo(target, 1);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.living.getRandom().nextFloat() < 0.3D)
                this.strafingClockwise = !this.strafingClockwise;

            if (this.living.getRandom().nextFloat() < 0.3D)
                this.strafingBackwards = !this.strafingBackwards;

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (dist > this.maxAttackDistance * 0.75)
                this.strafingBackwards = false;
            else if (dist < this.maxAttackDistance * 0.25)
                this.strafingBackwards = true;

            this.living.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.living.lookAt(target, 30.0F, 30.0F);
        } else {
            this.living.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    private void moveToRange(LivingEntity target, boolean canSee) {
        double dist = this.living.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (canSee)
            ++this.seeTime;
        else
            this.seeTime = 0;

        if (dist <= this.maxAttackDistance && this.seeTime >= 5)
            this.living.getNavigation().stop();
        else
            this.living.getNavigation().moveTo(target, 1);

        this.living.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }
}
