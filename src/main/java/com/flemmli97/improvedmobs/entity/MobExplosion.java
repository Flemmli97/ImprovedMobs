package com.flemmli97.improvedmobs.entity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MobExplosion extends Explosion {

	private float explosionSize;
	private World worldObj;
	private double explosionX;
	private double explosionY;
	private double explosionZ;
	private Entity exploder;
	private final Map<EntityPlayer, Vec3d> playerKnockbackMap;

	@SideOnly(Side.CLIENT)
	public MobExplosion(World worldIn, Entity entityIn, double x, double y, double z, float size, List<BlockPos> affectedPositions) {
		this(worldIn, entityIn, x, y, z, size, false, true, affectedPositions);
	}

	@SideOnly(Side.CLIENT)
	public MobExplosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean smoking, List<BlockPos> affectedPositions) {
		this(worldIn, entityIn, x, y, z, size, flaming, smoking);
	}

	public MobExplosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean smoking) {
		super(worldIn, entityIn, x, y, z, size, flaming, smoking);
		this.playerKnockbackMap = Maps.newHashMap();
		this.worldObj = worldIn;
		this.exploder = entityIn;
		this.explosionSize = size;
		this.explosionX = x;
		this.explosionY = y;
		this.explosionZ = z;
	}

	@Override
	public void doExplosionA() {
		Set<BlockPos> set = Sets.newHashSet();
		for(int j = 0; j < 16; ++j){
			for(int k = 0; k < 16; ++k){
				for(int l = 0; l < 16; ++l){
					if(j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15){
						double d0 = j / 15.0F * 2.0F - 1.0F;
						double d1 = k / 15.0F * 2.0F - 1.0F;
						double d2 = l / 15.0F * 2.0F - 1.0F;
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						d0 = d0 / d3;
						d1 = d1 / d3;
						d2 = d2 / d3;
						float f = this.explosionSize * (0.7F + this.worldObj.rand.nextFloat() * 0.6F);
						double d4 = this.explosionX;
						double d6 = this.explosionY;
						double d8 = this.explosionZ;

						while(f > 0.0F){
							BlockPos blockpos = new BlockPos(d4, d6, d8);

							if(f > 0.0F){
								set.add(blockpos);
							}

							d4 += d0 * 0.30000001192092896D;
							d6 += d1 * 0.30000001192092896D;
							d8 += d2 * 0.30000001192092896D;
							f -= 0.22500001F;
						}
					}
				}
			}
		}

		this.getAffectedBlockPositions().addAll(set);
		float f3 = this.explosionSize * 2.0F;
		int k1 = MathHelper.floor(this.explosionX - f3 - 1.0D);
		int l1 = MathHelper.floor(this.explosionX + f3 + 1.0D);
		int i2 = MathHelper.floor(this.explosionY - f3 - 1.0D);
		int i1 = MathHelper.floor(this.explosionY + f3 + 1.0D);
		int j2 = MathHelper.floor(this.explosionZ - f3 - 1.0D);
		int j1 = MathHelper.floor(this.explosionZ + f3 + 1.0D);
		List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB(k1, i2, j2, l1, i1, j1));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.worldObj, this, list, f3);
		Vec3d vec3d = new Vec3d(this.explosionX, this.explosionY, this.explosionZ);

		for (Entity entity : list) {
			if (!entity.isImmuneToExplosions() && this.getExplosivePlacedBy() instanceof EntityLiving && (entity == ((EntityLiving) this.getExplosivePlacedBy()).getAttackTarget())) {
				double d12 = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / f3;

				if (d12 <= 1.0D) {
					double d5 = entity.posX - this.explosionX;
					double d7 = entity.posY + entity.getEyeHeight() - this.explosionY;
					double d9 = entity.posZ - this.explosionZ;
					double d13 = MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

					if (d13 != 0.0D) {
						d5 = d5 / d13;
						d7 = d7 / d13;
						d9 = d9 / d13;
						double d14 = this.worldObj.getBlockDensity(vec3d, entity.getEntityBoundingBox());
						double d10 = (1.0D - d12) * d14;
						entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * f3 + 1.0D)));
						double d11 = 1.0D;

						if (entity instanceof EntityLivingBase) {
							d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, d10);
						}

						entity.motionX += d5 * d11;
						entity.motionY += d7 * d11;
						entity.motionZ += d9 * d11;

						if (entity instanceof EntityPlayer) {
							EntityPlayer entityplayer = (EntityPlayer) entity;

							if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying)) {
								this.playerKnockbackMap.put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void doExplosionB(boolean spawnParticles) {
		this.worldObj.playSound(null, this.explosionX, this.explosionY, this.explosionZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
		this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);

		for(BlockPos blockpos : this.getAffectedBlockPositions()){
			if(spawnParticles){
				double d0 = blockpos.getX() + this.worldObj.rand.nextFloat();
				double d1 = blockpos.getY() + this.worldObj.rand.nextFloat();
				double d2 = blockpos.getZ() + this.worldObj.rand.nextFloat();
				double d3 = d0 - this.explosionX;
				double d4 = d1 - this.explosionY;
				double d5 = d2 - this.explosionZ;
				double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
				d3 = d3 / d6;
				d4 = d4 / d6;
				d5 = d5 / d6;
				double d7 = 0.5D / (d6 / this.explosionSize + 0.1D);
				d7 = d7 * (this.worldObj.rand.nextFloat() * this.worldObj.rand.nextFloat() + 0.3F);
				d3 = d3 * d7;
				d4 = d4 * d7;
				d5 = d5 * d7;
				this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.explosionX) / 2.0D, (d1 + this.explosionY) / 2.0D, (d2 + this.explosionZ) / 2.0D, d3, d4, d5);
				this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
			}
		}
	}
}
