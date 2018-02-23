package com.flemmli97.improvedmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.World;

public class NewPathNavigateGround extends PathNavigateGround{

	public NewPathNavigateGround(EntityLiving entitylivingIn, World worldIn) {
		super(entitylivingIn, worldIn);
	}

	@Override
	protected PathFinder getPathFinder() {
		NewWalkNodeProcessor walkNode =  new NewWalkNodeProcessor();
		walkNode.setBreakBlocks(this.theEntity.getTags().contains("Breaker"));
		this.nodeProcessor = walkNode;
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);	
   }
}
