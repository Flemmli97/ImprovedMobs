package com.flemmli97.improvedmobs.entity.ai;

import com.flemmli97.improvedmobs.handler.ItemType;
import com.flemmli97.improvedmobs.handler.helper.AIUseHelper;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSplashPotion;
import net.minecraft.item.ItemStack;

public class EntityAIUseItem extends EntityAIBase{
	
	private EntityLiving living;
    double speedToTarget = 1;
	private int attackCooldown=25;
	private int itemUseCount;
	private float maxAttackDistance;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise, strafingBackwards;
    private int strafingTime = -1;
    private ItemType type;
    private boolean hasBowAI;
	public EntityAIUseItem(EntityLiving entity, float maxDistance)
	{
		this.living=entity;
        this.maxAttackDistance = maxDistance * maxDistance;
        this.setMutexBits(8);
        entity.tasks.taskEntries.forEach(entry->{
        	if(entry.action instanceof EntityAIAttackRangedBow || entry.action instanceof EntityAIAttackRanged)
        	{
        		hasBowAI=true;
        	}
        });
	}
	
	@Override
	public boolean shouldExecute() {
		EntityLivingBase target = living.getAttackTarget();
		if (target == null||!target.isEntityAlive()||this.shouldNotExecute())
            return false;
        else 
        {
    		ItemType type =	AIUseHelper.isItemApplicable(living);
    		this.type = type;
            return this.type!=ItemType.NOTHING;
        }
	}
	
	@Override
	public void startExecuting() {
		this.itemUseCount = this.type.getItem().getMaxItemUseDuration(this.type.getStack());
	}

	@Override
	public boolean shouldContinueExecuting()
    {
        return this.shouldExecute();
    }

	@Override
	public void resetTask() {
		this.seeTime = 0;
        this.attackTime = -1;
        this.living.resetActiveHand();
        this.type=null;
	}

	@Override
	public void updateTask() {
		EntityLivingBase target = this.living.getAttackTarget();
		if(target!=null)
		{
            boolean flag = this.living.getEntitySenses().canSee(target);
            if(type == ItemType.BOW || type ==ItemType.STRAFINGITEM)
            		this.moveStrafing(target, flag);
            if (this.living.isHandActive()|| this.itemUseCount==0 || type.getItem() instanceof ItemSplashPotion)
            {
                if (!flag && this.seeTime < -60)
                {
                    this.living.resetActiveHand();                  
                }
                else if (flag)
                {
                    int i = this.living.getItemInUseMaxCount();
                    if(this.itemUseCount==0|| type.getItem() instanceof ItemSplashPotion)
                    {
                    		if(--this.attackTime <= 0 && this.seeTime >= -60)
                    		{                                  			
                    			AIUseHelper.chooseAttack(this.living, target);
                        		this.attackTime = this.setAttackCooldown(this.type.getStack());
                    		}	
                    }
                    else if ((this.type == ItemType.BOW && i >= 20) ||i >= this.itemUseCount)
                    {
                		if(this.type ==ItemType.BOW)
                			AIUseHelper.attackWithArrows(new EntityTippedArrow(living.world, living), living, target, ItemBow.getArrowVelocity(i));
                		else
                			AIUseHelper.chooseAttack(this.living, target);
                		
                		this.living.resetActiveHand();
                		this.attackTime = this.attackCooldown;
                    }
                }
            }
            else if (--this.attackTime <= 0 && this.seeTime >= -60)
            {
        			this.living.setActiveHand(this.type.getHand());
            }
		}
	}

	private void moveStrafing(EntityLivingBase target, boolean canSee) 
	{
		double d0 = this.living.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        boolean flag1 = this.seeTime > 0;

        if (canSee != flag1)
        {
            this.seeTime = 0;
        }

        if (canSee)
        {
            ++this.seeTime;
        }
        else
        {
            --this.seeTime;
        }

        if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20)
        {
            this.living.getNavigator().clearPath();
            ++this.strafingTime;
        }
        else
        {
            this.living.getNavigator().tryMoveToEntityLiving(target, this.speedToTarget);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20)
        {
            if ((double)this.living.getRNG().nextFloat() < 0.3D)
            {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double)this.living.getRNG().nextFloat() < 0.3D)
            {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1)
        {
            if (d0 > (double)(this.maxAttackDistance * 0.75F))
            {
                this.strafingBackwards = false;
            }
            else if (d0 < (double)(this.maxAttackDistance * 0.25F))
            {
                this.strafingBackwards = true;
            }

            this.living.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.living.faceEntity(target, 30.0F, 30.0F);
        }
        else
        {
            this.living.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }
	}
	
	private int setAttackCooldown(ItemStack stack)
	{
		int cooldown = 25;
		if(stack.getItem() == Item.getItemFromBlock(Blocks.TNT))
		{
			cooldown=65;
		}
		else if(stack.getItem() instanceof ItemSplashPotion)
		{
			cooldown=85;
		}
		else if(stack.getItem() == Items.LAVA_BUCKET)
		{
			cooldown=80;
		}
		else if(stack.getItem() == Items.ENCHANTED_BOOK)
		{
			cooldown=90;
		}
		return cooldown;
	}
	
	/**
	 * Specific mobs here. like for skeletons with bows
	 */
	private boolean shouldNotExecute()
	{
		return this.hasBowAI && this.living.getHeldItemMainhand().getItem() instanceof ItemBow;
	}
}
