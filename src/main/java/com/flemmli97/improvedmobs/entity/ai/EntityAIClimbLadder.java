package com.flemmli97.improvedmobs.entity.ai;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;

public class EntityAIClimbLadder extends EntityAIBase{

	private EntityLiving living;	
	private Path path;
	
	public EntityAIClimbLadder(EntityLiving living)
	{
		this.living=living;
		this.setMutexBits(4);
	}
	
	@Override
	public boolean shouldExecute() {
		if(!this.living.getNavigator().noPath())
		{
			this.path= this.living.getNavigator().getPath();
			return this.path!=null && this.living.isOnLadder();
		}
		return false;
	}
	
	@Override
	public void updateTask()
	{
		int i = this.path.getCurrentPathIndex();
		if(i+1<this.path.getCurrentPathLength())
		{
			int y = this.path.getPathPointFromIndex(i).y;//this.living.getPosition().getY();
			PathPoint pointNext = this.path.getPathPointFromIndex(i+1);
			IBlockState down = this.living.world.getBlockState(this.living.getPosition().down());
			if(pointNext.y<y||(pointNext.y==y &&!down.getBlock().isLadder(down, this.living.world, this.living.getPosition().down(), this.living)))
				this.living.motionY=-0.15;
			else
				this.living.motionY=0.15;
		}
	}
}
