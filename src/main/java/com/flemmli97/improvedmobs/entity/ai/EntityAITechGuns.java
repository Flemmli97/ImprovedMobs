package com.flemmli97.improvedmobs.entity.ai;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import com.flemmli97.improvedmobs.handler.config.ConfigHandler;
import com.flemmli97.tenshilib.common.javahelper.ReflectionUtils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import techguns.items.guns.GenericGun;

/**
 * Credit goes to pWn3d1337:
 * https://github.com/pWn3d1337/Techguns2/blob/master/src/main/java/techguns/entities/ai/EntityAIRangedAttack.java
 */
public class EntityAITechGuns extends EntityAIBase {

	/** The entity the AI instance has been applied to */
	private final EntityLiving entityHost;
	private EntityLivingBase attackTarget;
	/**
	 * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
	 * maxRangedAttackTime.
	 */
	private int rangedAttackTime;
	private int ticksTargetSeen;
	private int attackTimeVariance;
	/** The maximum time the AI has to wait before peforming another ranged attack. */
	private int maxRangedAttackTime;
	private float attackRange;
	private float attackRange_2;

	//GUN HANDLING:
	private int maxBurstCount; //Total number of shots in burst.
	private int burstCount; //shots left in current burst.
	private int shotDelay; //delay between shots in burst.

	public EntityAITechGuns(EntityLiving shooter, int attackTimeVariance, int attackTime, float attackRange, int maxBurstCount, int shotDelay) {
		this.rangedAttackTime = -1;
		this.entityHost = (EntityLiving) shooter;
		this.attackTimeVariance = attackTimeVariance;
		this.maxRangedAttackTime = attackTime;
		this.attackRange = attackRange;
		this.attackRange_2 = attackRange * attackRange;
		this.setMutexBits(3);

		this.maxBurstCount = maxBurstCount;
		this.burstCount = maxBurstCount;
		this.shotDelay = shotDelay;
	}

	@Override
	public boolean shouldExecute() {
		if(!(this.entityHost.getHeldItemMainhand().getItem() instanceof GenericGun) || this.entityHost.getAttackTarget() == null)
			return false;
		EntityLivingBase entitylivingbase = this.entityHost.getAttackTarget();

		if(entitylivingbase == null){
			return false;
		}else{
			this.attackTarget = entitylivingbase;
			return true;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.attackTarget.isEntityAlive() && (this.shouldExecute() || !this.entityHost.getNavigator().noPath());
	}

	@Override
	public void resetTask() {
		this.attackTarget = null;
		this.ticksTargetSeen = 0;
		this.rangedAttackTime = -1;
	}

	@Override
	public void updateTask() {
		double d0 = this.entityHost.getDistanceSq(this.attackTarget.posX, this.attackTarget.posY/*this.attackTarget.boundingBox.minY TODO??*/,
				this.attackTarget.posZ);
		boolean targetInSight = this.entityHost.getEntitySenses().canSee(this.attackTarget);

		if(targetInSight){
			++this.ticksTargetSeen;
		}else{
			this.ticksTargetSeen = 0;
		}

		if(d0 <= (double) this.attackRange_2 && this.ticksTargetSeen >= 20){
			this.entityHost.getNavigator().clearPath();
		}else{
			this.entityHost.getNavigator().tryMoveToEntityLiving(this.attackTarget, 1);
		}

		this.entityHost.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 55.0F);
		float f;

		if(--this.rangedAttackTime == 0){
			if(d0 > (double) this.attackRange_2 || !targetInSight){
				return;
			}

			f = MathHelper.sqrt(d0) / this.attackRange;

			float f1 = f;

			if(f < 0.1F){
				f1 = 0.1F;
			}

			if(f1 > 1.0F){
				f1 = 1.0F;
			}

			this.attackEntityWithRangedAttack(this.attackTarget, f1);

			if(maxBurstCount > 0)
				burstCount--;
			if(burstCount > 0){
				this.rangedAttackTime = shotDelay;
			}else{
				burstCount = maxBurstCount;
				this.rangedAttackTime = MathHelper
						.floor(f * (float) (this.maxRangedAttackTime - this.attackTimeVariance) + (float) this.attackTimeVariance);
			}
		}else if(this.rangedAttackTime < 0){
			f = MathHelper.sqrt(d0) / this.attackRange;
			this.rangedAttackTime = MathHelper
					.floor(f * (float) (this.maxRangedAttackTime - this.attackTimeVariance) + (float) this.attackTimeVariance);
		}
	}

	private void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
		Item gun = this.entityHost.getHeldItemMainhand().getItem();
		if(gun instanceof GenericGun){

			EnumDifficulty difficulty = this.entityHost.world.getDifficulty();
			float acc = 1.0f;
			float dmg = 1.0f;
			switch(difficulty){
				case EASY:
					acc = 1.3f;
					dmg = 0.6f;
					break;
				case NORMAL:
					acc = 1.15f;
					dmg = 0.8f;
					break;
				case HARD:
					acc = 1.0f;
					dmg = 1.0f;
					break;
				default:
					break;
			}

			((GenericGun) gun).fireWeaponFromNPC(this.entityHost, dmg, acc);
		}
	}

	private static Field AI_attackTime;
	private static Field AI_burstCount;
	private static Field AI_burstAttackTime;

	@Nullable
	public static void applyAI(EntityLiving e) {
		if(!ConfigHandler.useTGunsMod)
			return;
		if(AI_attackTime == null)
			AI_attackTime = ReflectionUtils.getField(GenericGun.class, "AI_attackTime");
		if(AI_burstCount == null)
			AI_burstCount = ReflectionUtils.getField(GenericGun.class, "AI_burstCount");
		if(AI_burstAttackTime == null)
			AI_burstAttackTime = ReflectionUtils.getField(GenericGun.class, "AI_burstAttackTime");
		Item item = e.getHeldItemMainhand().getItem();
		if(item instanceof GenericGun){
			GenericGun gun = ((GenericGun) item);
			int time = ReflectionUtils.getFieldValue(AI_attackTime, gun);
			e.tasks.addTask(0, new EntityAITechGuns(e, time / 3, time, gun.getAI_attackRange(), ReflectionUtils.getFieldValue(AI_burstCount, gun),
					ReflectionUtils.getFieldValue(AI_burstAttackTime, gun)));
		}
	}
}
