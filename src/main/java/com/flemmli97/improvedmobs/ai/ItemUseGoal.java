package com.flemmli97.improvedmobs.ai;

import com.flemmli97.improvedmobs.utils.ItemAITasks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;

public class ItemUseGoal extends Goal {

    private final MobEntity living;
    private float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise, strafingBackwards;
    private int strafingTime = -1;
    private ItemAITasks.ItemAI ai;
    private Hand hand;
    private boolean hasBowAI, hasRangedAttack;
    private ItemStack stackMain, stackOff;

    public ItemUseGoal(MobEntity entity, float maxDistance) {
        this.living = entity;
        float follow = maxDistance;
        if (entity.getAttribute(Attributes.GENERIC_FOLLOW_RANGE) != null)
            follow = (float) entity.getAttribute(Attributes.GENERIC_FOLLOW_RANGE).getValue();
        maxDistance = Math.min(follow - 3, maxDistance);
        this.maxAttackDistance = maxDistance * maxDistance;
        ((IGoalModifier) entity.goalSelector).modifyGoal(Goal.class, g -> {
            if (g instanceof RangedBowAttackGoal || g instanceof RangedCrossbowAttackGoal || this.living.getType().getRegistryName().toString().equals("primitivemobs:skeleton_warrior"))
                this.hasBowAI = true;
            if (g instanceof RangedAttackGoal)
                this.hasRangedAttack = true;
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
        this.setMutexFlags(this.ai.type() == ItemAITasks.ItemType.NONSTRAFINGITEM ? EnumSet.noneOf(Goal.Flag.class) : EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
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

            if (this.living.isHandActive() || !this.ai.useHand()) {
                if (!flag && this.seeTime < -60) {
                    this.living.resetActiveHand();
                } else if (flag) {
                    if (this.ai.useHand()) {
                        int i = this.living.getItemInUseMaxCount();
                        if (i >= this.ai.maxUseCount()) {
                            this.living.resetActiveHand();
                            this.ai.attack(this.living, target, this.hand);
                            this.attackTime = this.ai.cooldown() * (this.hasRangedAttack ? 2 : 1);
                        }
                    } else if (--this.attackTime <= 0) {
                        this.ai.attack(this.living, target, this.hand);
                        this.living.resetActiveHand();
                        this.attackTime = this.ai.cooldown() * (this.hasRangedAttack ? 2 : 1);
                    }
                }
            } else if (--this.attackTime < 0 && this.seeTime >= -60) {
                this.living.setActiveHand(this.hand);
            }
        }
    }

    private void moveStrafing(LivingEntity target, boolean canSee) {
        double d0 = this.living.getDistanceSq(target.getX(), target.getBoundingBox(target.getPose()).minY, target.getZ());
        boolean flag1 = this.seeTime > 0;

        if (canSee != flag1) {
            this.seeTime = 0;
        }

        if (canSee) {
            ++this.seeTime;
        } else {
            --this.seeTime;
        }

        if (d0 <= this.maxAttackDistance && this.seeTime >= 20) {
            this.living.getNavigator().clearPath();
            ++this.strafingTime;
        } else {
            this.living.getNavigator().tryMoveToEntityLiving(target, 1);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.living.getRNG().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if (this.living.getRNG().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (d0 > (this.maxAttackDistance * 0.75F)) {
                this.strafingBackwards = false;
            } else if (d0 < (this.maxAttackDistance * 0.25F)) {
                this.strafingBackwards = true;
            }

            this.living.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.living.faceEntity(target, 30.0F, 30.0F);
        } else {
            this.living.getLookController().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }
    }

    /**
     * Specific mobs here. like for skeletons with bows
     */
    private boolean canAlreadyUse() {
        if (this.hasRangedAttack && this.living instanceof DrownedEntity && this.living.getHeldItemMainhand().getItem() == Items.TRIDENT)
            return true;
        return this.hasBowAI && this.living.getHeldItemMainhand().getItem() == Items.BOW;
    }
}
