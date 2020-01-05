package com.flemmli97.improvedmobs.entity.ai;

import org.apache.commons.lang3.tuple.Pair;

import com.flemmli97.improvedmobs.handler.helper.AIUseHelper;
import com.flemmli97.improvedmobs.handler.helper.AIUseHelper.ItemAI;
import com.flemmli97.improvedmobs.handler.helper.AIUseHelper.ItemType;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class EntityAIUseItem extends EntityAIBase {

    private EntityLiving living;
    private float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise, strafingBackwards;
    private int strafingTime = -1;
    private ItemAI ai;
    private EnumHand hand;
    private boolean hasBowAI, hasRangedAttack;
    private ItemStack stackMain, stackOff;

    public EntityAIUseItem(EntityLiving entity, float maxDistance) {
        this.living = entity;
        float follow = maxDistance;
        if (entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null)
            follow = (float) entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
        maxDistance = Math.min(follow - 3, maxDistance);
        this.maxAttackDistance = maxDistance * maxDistance;
        this.setMutexBits(8);
        entity.tasks.taskEntries.forEach(entry -> {
            if (entry.action instanceof EntityAIAttackRangedBow)
                this.hasBowAI = true;
            if (entry.action instanceof EntityAIAttackRanged)
                this.hasRangedAttack = true;
        });
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase target = this.living.getAttackTarget();
        if (target == null || !target.isEntityAlive() || this.shouldNotExecute())
            return false;
        Pair<ItemAI,EnumHand> pair = AIUseHelper.getAI(this.living);
        this.ai = pair.getKey();
        this.hand = pair.getValue();
        return this.ai != null;
    }

    @Override
    public void startExecuting() {
        this.setMutexBits(this.ai.type() == ItemType.NONSTRAFINGITEM ? 8 : 3);
        this.stackMain = this.living.getHeldItemMainhand();
        this.stackOff = this.living.getHeldItemOffhand();
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase target = this.living.getAttackTarget();
        if (target == null || !target.isEntityAlive() || this.shouldNotExecute())
            return false;
        if (this.stackMain != this.living.getHeldItemMainhand() || this.stackOff != this.living.getHeldItemOffhand()) {
            Pair<ItemAI,EnumHand> pair = AIUseHelper.getAI(this.living);
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
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = this.living.getAttackTarget();
        if (target != null) {
            boolean flag = this.living.getEntitySenses().canSee(target);
            if (this.ai.type() == ItemType.STRAFINGITEM)
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

    private void moveStrafing(EntityLivingBase target, boolean canSee) {
        double d0 = this.living.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
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
            this.living.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }
    }

    /**
     * Specific mobs here. like for skeletons with bows
     */
    private boolean shouldNotExecute() {
        return this.hasBowAI && this.living.getHeldItemMainhand().getItem() == Items.BOW;
    }
}
