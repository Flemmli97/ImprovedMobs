package com.flemmli97.improvedmobs.entity.ai;

import javax.annotation.Nullable;

import com.flemmli97.improvedmobs.handler.ConfigHandler;
import com.flemmli97.improvedmobs.handler.helper.GeneralHelperMethods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public class NewWalkNodeProcessor extends WalkNodeProcessor{

	private boolean canBreakBlocks;
	 
    private PathNodeType getPathNodeType(EntityLiving entitylivingIn, int x, int y, int z)
    {
        return this.getPathNodeType(this.blockaccess, x, y, z, entitylivingIn, this.entitySizeX, this.entitySizeY, this.entitySizeZ, this.getCanBreakDoors(), this.getCanEnterDoors());
    }
    
	@Override
	public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance)
    {
        int i = 0;
        int j = 0;
        PathNodeType pathnodetype = this.getPathNodeType(this.entity, currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord);

        if (this.entity.getPathPriority(pathnodetype) >= 0.0F)
        {
            j = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
        }

        BlockPos blockpos = (new BlockPos(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord)).down();
        double d0 = currentPoint.yCoord - (1.0D - this.blockaccess.getBlockState(blockpos).getBoundingBox(this.blockaccess, blockpos).maxY);
        PathPoint pathpoint = this.getSafePoint(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord + 1, j, d0, EnumFacing.SOUTH);
        PathPoint pathpoint1 = this.getSafePoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord, j, d0, EnumFacing.WEST);
        PathPoint pathpoint2 = this.getSafePoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord, j, d0, EnumFacing.EAST);
        PathPoint pathpoint3 = this.getSafePoint(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord - 1, j, d0, EnumFacing.NORTH);
        PathPoint ladderUp = this.openPoint(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord);
        PathPoint ladderDown = this.openPoint(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord);
        
        if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint;
        }

        if (pathpoint1 != null && !pathpoint1.visited && pathpoint1.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint1;
        }

        if (pathpoint2 != null && !pathpoint2.visited && pathpoint2.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint2;
        }

        if (pathpoint3 != null && !pathpoint3.visited && pathpoint3.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint3;
        }
        
        IBlockState ladderCheck = this.blockaccess.getBlockState(new BlockPos(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord));
        if (ladderUp != null && !ladderUp.visited && ladderUp.distanceTo(targetPoint) < maxDistance && ladderCheck.getBlock().isLadder(ladderCheck, this.blockaccess, new BlockPos(currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord), entity))
        {
            pathOptions[i++] = ladderUp;
        }
        
        IBlockState ladderCheckDown = this.blockaccess.getBlockState(new BlockPos(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord));
        if (ladderDown != null && !ladderDown.visited && ladderDown.distanceTo(targetPoint) < maxDistance && ladderCheckDown.getBlock().isLadder(ladderCheckDown, this.blockaccess, new BlockPos(currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord), entity))
        {
            pathOptions[i++] = ladderDown;
        }

        boolean flag = pathpoint3 == null || pathpoint3.nodeType == PathNodeType.OPEN || pathpoint3.costMalus != 0.0F;
        boolean flag1 = pathpoint == null || pathpoint.nodeType == PathNodeType.OPEN || pathpoint.costMalus != 0.0F;
        boolean flag2 = pathpoint2 == null || pathpoint2.nodeType == PathNodeType.OPEN || pathpoint2.costMalus != 0.0F;
        boolean flag3 = pathpoint1 == null || pathpoint1.nodeType == PathNodeType.OPEN || pathpoint1.costMalus != 0.0F;

        if (flag && flag3)
        {
            PathPoint pathpoint4 = this.getSafePoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord - 1, j, d0, EnumFacing.NORTH);
			double d1 = this.entity.width / 2.0D;
			AxisAlignedBB aabb1 = new AxisAlignedBB(currentPoint.xCoord-1 - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord - d1 + 0.5D, currentPoint.xCoord-1 + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord + d1 + 0.5D);
			AxisAlignedBB aabb2 = new AxisAlignedBB(currentPoint.xCoord - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord-1 - d1 + 0.5D, currentPoint.xCoord + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord-1 + d1 + 0.5D);
			if (!this.entity.world.collidesWithAnyBlock(aabb1) && !this.entity.world.collidesWithAnyBlock(aabb2) &&pathpoint4 != null && !pathpoint4.visited && pathpoint4.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint4;
            }
        }

        if (flag && flag2)
        {
            PathPoint pathpoint5 = this.getSafePoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord - 1, j, d0, EnumFacing.NORTH);
			double d1 = this.entity.width / 2.0D;
			AxisAlignedBB aabb1 = new AxisAlignedBB(currentPoint.xCoord+1 - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord - d1 + 0.5D, currentPoint.xCoord+1 + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord + d1 + 0.5D);
			AxisAlignedBB aabb2 = new AxisAlignedBB(currentPoint.xCoord - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord-1 - d1 + 0.5D, currentPoint.xCoord + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord-1 + d1 + 0.5D);
			if (!this.entity.world.collidesWithAnyBlock(aabb1) && !this.entity.world.collidesWithAnyBlock(aabb2) &&pathpoint5 != null && !pathpoint5.visited && pathpoint5.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint5;
            }
        }

        if (flag1 && flag3)
        {
            PathPoint pathpoint6 = this.getSafePoint(currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord + 1, j, d0, EnumFacing.SOUTH);

			double d1 = this.entity.width / 2.0D;
			AxisAlignedBB aabb1 = new AxisAlignedBB(currentPoint.xCoord-1 - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord - d1 + 0.5D, currentPoint.xCoord-1 + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord + d1 + 0.5D);
			AxisAlignedBB aabb2 = new AxisAlignedBB(currentPoint.xCoord - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord+1 - d1 + 0.5D, currentPoint.xCoord + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord+1 + d1 + 0.5D);
			if (!this.entity.world.collidesWithAnyBlock(aabb1) && !this.entity.world.collidesWithAnyBlock(aabb2) &&pathpoint6 != null && !pathpoint6.visited && pathpoint6.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint6;
            }
        }

        if (flag1 && flag2)
        {
            PathPoint pathpoint7 = this.getSafePoint(currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord + 1, j, d0, EnumFacing.SOUTH);
			double d1 = this.entity.width / 2.0D;
			AxisAlignedBB aabb1 = new AxisAlignedBB(currentPoint.xCoord+1 - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord - d1 + 0.5D, currentPoint.xCoord+1 + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord + d1 + 0.5D);
			AxisAlignedBB aabb2 = new AxisAlignedBB(currentPoint.xCoord - d1 + 0.5D, currentPoint.yCoord + 1.001D, currentPoint.zCoord+1 - d1 + 0.5D, currentPoint.xCoord + d1 + 0.5D, ((float)currentPoint.yCoord + 1+this.entity.height), currentPoint.zCoord+1 + d1 + 0.5D);
			if (!this.entity.world.collidesWithAnyBlock(aabb1) && !this.entity.world.collidesWithAnyBlock(aabb2) && pathpoint7 != null && !pathpoint7.visited && pathpoint7.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint7;
            }
        }
        return i;
    }
	
	// Really bugs me that this method is private, so much wasted space...
	@Nullable
    private PathPoint getSafePoint(int x, int y, int z, int p_186332_4_, double p_186332_5_, EnumFacing facing)
    {
        PathPoint pathpoint = null;
        BlockPos blockpos = new BlockPos(x, y, z);
        BlockPos blockpos1 = blockpos.down();
        double d0 = y - (1.0D - this.blockaccess.getBlockState(blockpos1).getBoundingBox(this.blockaccess, blockpos1).maxY);
        if (d0 - p_186332_5_ > 1.125D) return null;
        else
        {
            PathNodeType pathnodetype = this.getPathNodeType(this.entity, x, y, z);
            float f = this.entity.getPathPriority(pathnodetype);
            double d1 = this.entity.width / 2.0D;
            if (f >= 0.0F)
            {
                pathpoint = this.openPoint(x, y, z);
                pathpoint.nodeType = pathnodetype;
                pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
            }
            if (pathnodetype == PathNodeType.WALKABLE) return pathpoint;
            else
            {
                if (pathpoint == null && p_186332_4_ > 0 && pathnodetype != PathNodeType.FENCE && pathnodetype != PathNodeType.TRAPDOOR)
                {
                    pathpoint = this.getSafePoint(x, y + 1, z, p_186332_4_ - 1, p_186332_5_, facing);
                    if (pathpoint != null && (pathpoint.nodeType == PathNodeType.OPEN || pathpoint.nodeType == PathNodeType.WALKABLE) && this.entity.width < 1.0F)
                    {
                        double d2 = (x - facing.getFrontOffsetX()) + 0.5D;
                        double d3 = (z - facing.getFrontOffsetZ()) + 0.5D;
                        AxisAlignedBB axisalignedbb = new AxisAlignedBB(d2 - d1, y + 0.001D, d3 - d1, d2 + d1, ((float)y + this.entity.height), d3 + d1);
                        AxisAlignedBB axisalignedbb1 = this.blockaccess.getBlockState(blockpos).getBoundingBox(this.blockaccess, blockpos);
                        AxisAlignedBB axisalignedbb2 = axisalignedbb.addCoord(0.0D, axisalignedbb1.maxY - 0.002D, 0.0D);
                        if (this.entity.world.collidesWithAnyBlock(axisalignedbb2)) pathpoint = null;
                    }
                }
                if (pathnodetype == PathNodeType.OPEN)
                {
                    AxisAlignedBB axisalignedbb3 = new AxisAlignedBB(x - d1 + 0.5D, y + 0.001D, z - d1 + 0.5D, x + d1 + 0.5D, ((float)y + this.entity.height), z + d1 + 0.5D);
                    if (this.entity.world.collidesWithAnyBlock(axisalignedbb3)) return null;
                    if (this.entity.width >= 1.0F)
                    {
                        PathNodeType pathnodetype1 = this.getPathNodeType(this.entity, x, y - 1, z);
                        if (pathnodetype1 == PathNodeType.BLOCKED)
                        {
                            pathpoint = this.openPoint(x, y, z);
                            pathpoint.nodeType = PathNodeType.WALKABLE;
                            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                            return pathpoint;
                        }
                    }
                    int i = 0;
                    while (y > 0 && pathnodetype == PathNodeType.OPEN)
                    {
                        --y;
                        if (i++ >= this.entity.getMaxFallHeight()) return null;
                        pathnodetype = this.getPathNodeType(this.entity, x, y, z);
                        f = this.entity.getPathPriority(pathnodetype);
                        if (pathnodetype != PathNodeType.OPEN && f >= 0.0F)
                        {
                            pathpoint = this.openPoint(x, y, z);
                            pathpoint.nodeType = pathnodetype;
                            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                            break;
                        }
                        if (f < 0.0F)return null;
                    }
                }
                return pathpoint;
            }
        }
    }
	@Override
    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z)
    {
        PathNodeType pathnodetype = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);

        if (pathnodetype == PathNodeType.OPEN && y >= 1)
        {
            Block block = blockaccessIn.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
            PathNodeType pathnodetype1 = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
            pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;

            if (pathnodetype1 == PathNodeType.DAMAGE_FIRE || block == Blocks.MAGMA)
            {
                pathnodetype = PathNodeType.DAMAGE_FIRE;
            }

            if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS)
            {
                pathnodetype = PathNodeType.DAMAGE_CACTUS;
            }
        }

        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        if (pathnodetype == PathNodeType.WALKABLE)
        {
            for (int j = -1; j <= 1; ++j)
            {
                for (int i = -1; i <= 1; ++i)
                {
                    if (j != 0 || i != 0)
                    {
                        Block block1 = blockaccessIn.getBlockState(blockpos$pooledmutableblockpos.setPos(j + x, y, i + z)).getBlock();

                        if (block1 == Blocks.CACTUS)
                        {
                            pathnodetype = PathNodeType.DANGER_CACTUS;
                        }
                        else if (block1 == Blocks.FIRE)
                        {
                            pathnodetype = PathNodeType.DANGER_FIRE;
                        }
                        else if(block1.isBurning(blockaccessIn,blockpos$pooledmutableblockpos.setPos(j +x, y, i + z))) pathnodetype = PathNodeType.DAMAGE_FIRE;
                    }
                }
            }
        }

        blockpos$pooledmutableblockpos.release();
        return pathnodetype;
    }

	private PathNodeType getPathNodeTypeRaw(IBlockAccess acc, int x, int y, int z)
    {
        BlockPos blockpos = new BlockPos(x, y, z);
        IBlockState iblockstate = acc.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        if(block==Blocks.LADDER)
        	return PathNodeType.WALKABLE;
		if(this.canBreakBlocks && GeneralHelperMethods.isBlockBreakable(block, ConfigHandler.breakListNames) && GeneralHelperMethods.canHarvest(iblockstate, new ItemStack(Items.DIAMOND_PICKAXE)) && this.entity != null)
        {
    			if(this.entity.posY > blockpos.getY() + 0.8)
					return this.defaultNode(acc, iblockstate, blockpos);
    			else if(this.entity.posY<= blockpos.getY()+1 && this.entity.posY >= blockpos.getY())
    			{
    				double d1 = (double)this.entity.width / 2.0D;
    				AxisAlignedBB aabb = new AxisAlignedBB((double)x - d1 + 0.5D, (double)y + 1.001D, (double)z - d1 + 0.5D, (double)x + d1 + 0.5D, (double)((float)y + 1+this.entity.height), (double)z + d1 + 0.5D);
    				if (this.entity.world.collidesWithAnyBlock(aabb))
    					return PathNodeType.WALKABLE;
    				else
                		return this.defaultNode(acc, iblockstate, blockpos);
			}
    			return PathNodeType.OPEN;
        }
        else
        {
            return this.defaultNode(acc, iblockstate, blockpos);
        }
    }
	
	private PathNodeType defaultNode(IBlockAccess acc, IBlockState iblockstate, BlockPos blockpos)
	{
		Block block = iblockstate.getBlock();
		Material material = iblockstate.getMaterial();
		PathNodeType type = block.getAiPathNodeType(iblockstate, acc, blockpos);
        if (type != null) return type;

        if (material == Material.AIR)
        {
            return PathNodeType.OPEN;
        }
        else if(iblockstate.getCollisionBoundingBox(acc, blockpos)==Block.NULL_AABB)
        {
        	return PathNodeType.OPEN;
        }
        else if (block != Blocks.TRAPDOOR && block != Blocks.IRON_TRAPDOOR && block != Blocks.WATERLILY)
        {
            if (block == Blocks.FIRE)
            {
                return PathNodeType.DAMAGE_FIRE;
            }
            else if (block == Blocks.CACTUS)
            {
                return PathNodeType.DAMAGE_CACTUS;
            }
            else if (block instanceof BlockDoor && material == Material.WOOD && !((Boolean)iblockstate.getValue(BlockDoor.OPEN)).booleanValue())
            {
                return PathNodeType.DOOR_WOOD_CLOSED;
            }
            else if (block instanceof BlockDoor && material == Material.IRON && !((Boolean)iblockstate.getValue(BlockDoor.OPEN)).booleanValue())
            {
                return PathNodeType.DOOR_IRON_CLOSED;
            }
            else if (block instanceof BlockDoor && ((Boolean)iblockstate.getValue(BlockDoor.OPEN)).booleanValue())
            {
                return PathNodeType.DOOR_OPEN;
            }
            else if (block instanceof BlockRailBase)
            {
                return PathNodeType.RAIL;
            }
            else if (!(block instanceof BlockFence) && !(block instanceof BlockWall) && (!(block instanceof BlockFenceGate) || ((Boolean)iblockstate.getValue(BlockFenceGate.OPEN)).booleanValue()))
            {
                if (material == Material.WATER)
                {
                    return PathNodeType.WATER;
                }
                else if (material == Material.LAVA)
                {
                    return PathNodeType.LAVA;
                }
                else
                {
                    return block.isPassable(acc, blockpos) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
                }
            }
            else
            {
                return PathNodeType.FENCE;
            }
        }
        else
        {
            return PathNodeType.TRAPDOOR;
        }
	}
	
	public boolean canBreakBlocks()
	{
		return this.canBreakBlocks;
	}
	
	public void setBreakBlocks(boolean flag)
	{
		this.canBreakBlocks = flag;
	}
}
