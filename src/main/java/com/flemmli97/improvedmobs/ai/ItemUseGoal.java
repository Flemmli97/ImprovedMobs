package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.utils.ItemAITasks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;

public class ItemUseGoal extends Goal {

    private final MobEntity living;
    private final float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise, strafingBackwards;
    private int strafingTime = -1;
    private ItemAITasks.ItemAI ai;
    private Hand hand;
    private boolean hasBowAI, hasCrossBowAI;
    private ItemStack stackMain, stackOff;

    public ItemUseGoal(MobEntity entity, float maxDistance) {
        this.living = entity;
        float follow = maxDistance;
        if (entity.getAttribute(Attributes.GENERIC_FOLLOW_RANGE) != null)
            follow = (float) entity.getAttribute(Attributes.GENERIC_FOLLOW_RANGE).getValue();
        maxDistance = Math.min(follow - 3, maxDistance);
        this.maxAttackDistance = maxDistance * maxDistance;
        ((IGoalModifier) entity.goalSelector).modifyGoal(Goal.class, g -> {
            if (g instanceof RangedBowAttackGoal || this.living.getType().getRegistryName().toString().equals("primitivemobs:skeleton_warrior"))
                this.hasBowAI = true;
            if(g instanceof RangedCrossbowAttackGoal)
                this.hasCrossBowAI = true;
        });
    }

    @Override
    public boolean shouldExecute() {
        LivingEntity target = this.living.getAttackTarget();
        if (target == null || !target.isAlive() || this.canAlreadyUse())
            return false;
        Pair<ItemAITasks.ItemAI, Hand> pair = ItemAITasks.getAI(this.living);
        this.ai = pair.getKey();
        this.hand = pair.getValue();
        return this.ai != null;
    }

    @Override
    public void startExecuting() {
        this.setMutexFlags(this.ai.type() != ItemAITasks.ItemType.NONSTRAFINGITEM ? EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK) : EnumSet.noneOf(Goal.Flag.class));
        this.stackMain = this.living.getHeldItemMainhand();
        this.stackOff = this.living.getHeldItemOffhand();
    }

    @Override
    public boolean shouldContinueExecuting() {
        LivingEntity target = this.living.getAttackTarget();
        if (target == null || !target.isAlive() || this.canAlreadyUse())
            return false;
        if (this.stackMain != this.living.getHeldItemMainhand() || this.stackOff != this.living.getHeldItemOffhand()) {
            Pair<ItemAITasks.ItemAI, Hand> pair = ItemAITasks.getAI(this.living);
            this.ai = pair.getKey();
            this.hand = pair.getValue();
        }
        return this.ai != null;
    }

    @Override
    public void resetTask() {
        this.seeTime = 0;
        this.attackTime = -1;
        this.living.resetActiveHand();
        this.ai.onReset(this.living, this.hand);
        this.ai = null;
        this.stackMain = null;
        this.stackOff = null;
        this.setMutexFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public void tick() {
        LivingEntity target = this.living.getAttackTarget();
        if (target != null) {
            boolean flag = this.living.getEntitySenses().canSee(target);
            if (this.ai.type() == ItemAITasks.ItemType.STRAFINGITEM)
               this.moveStrafing(target, flag);
            else if(this.ai.type() == ItemAITasks.ItemType.STANDING)
                this.moveToRange(target, flag);
            if (this.living.isHandActive() || !this.ai.useHand()) {
                if (!flag && this.seeTime < -60) {
                    this.living.resetActiveHand();
                    this.ai.onReset(this.living, this.hand);
                } else if (flag) {
                    if (this.ai.useHand()) {
                        int i = this.living.getItemInUseMaxCount();
                        if (i >= this.ai.maxUseCount(this.living, this.hand)) {
                            this.living.stopActiveHand();
                            this.ai.attack(this.living, target, this.hand);
                            this.attackTime = this.ai.cooldown();
                        }
                    } else if (--this.attackTime <= 0) {
                        this.ai.attack(this.living, target, this.hand);
                        this.living.stopActiveHand();
                        this.attackTime = this.ai.cooldown();
                    }
                }
            } else if (--this.attackTime < 0 && this.seeTime >= -60) {
                this.living.setActiveHand(this.hand);
            }
        }
    }

    private void moveStrafing(LivingEntity target, boolean canSee) {
        double dist = this.living.getDistanceSq(target.getX(), target.getY(), target.getZ());
        boolean flag1 = this.seeTime > 0;
        if (canSee != flag1)
            this.seeTime = 0;
        if (canSee)
            ++this.seeTime;
        else
            --this.seeTime;
        if (dist <= this.maxAttackDistance && this.seeTime >= 20) {
            this.living.getNavigator().clearPath();
            ++this.strafingTime;
        } else {
            this.living.getNavigator().tryMoveToEntityLiving(target, 1);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.living.getRNG().nextFloat() < 0.3D)
                this.strafingClockwise = !this.strafingClockwise;

            if (this.living.getRNG().nextFloat() < 0.3D)
                this.strafingBackwards = !this.strafingBackwards;

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (dist > this.maxAttackDistance * 0.75)
                this.strafingBackwards = false;
            else if (dist < this.maxAttackDistance * 0.25)
                this.strafingBackwards = true;

            this.living.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.living.faceEntity(target, 30.0F, 30.0F);
        } else {
            this.living.getLookController().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }
    }

    private void moveToRange(LivingEntity target, boolean canSee){
        double dist = this.living.getDistanceSq(target.getX(), target.getY(), target.getZ());
        if (canSee)
            ++this.seeTime;
        else
            this.seeTime = 0;

        if (dist <= this.maxAttackDistance && this.seeTime >= 5)
            this.living.getNavigator().clearPath();
        else
            this.living.getNavigator().tryMoveToEntityLiving(target, 1);

        this.living.getLookController().setLookPositionWithEntity(target, 30.0F, 30.0F);    }

    /**
     * Specific mobs here. like for skeletons with bows
     */
    private boolean canAlreadyUse() {
        if (this.living instanceof DrownedEntity && this.living.getHeldItemMainhand().getItem() instanceof TridentItem)
            return true;
        if(this.hasCrossBowAI && this.living.getHeldItemMainhand().getItem() instanceof CrossbowItem)
            return true;
        return this.hasBowAI && this.living.getHeldItemMainhand().getItem() instanceof BowItem;
    }
}
