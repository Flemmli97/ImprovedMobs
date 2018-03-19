package com.flemmli97.improvedmobs.entity.ai;

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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class NewWalkNodeProcessor extends WalkNodeProcessor{

	private boolean canBreakBlocks;
	 
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
    		if(this.canBreakBlocks && GeneralHelperMethods.isBlockBreakable(block, ConfigHandler.breakList) && GeneralHelperMethods.canHarvest(iblockstate, new ItemStack(Items.DIAMOND_PICKAXE)))
        {
    			if(this.entity != null && this.entity.posY > blockpos.getY() + 0.8)
					return this.defaultNode(acc, iblockstate, blockpos);
    			else if(this.entity != null && this.entity.posY<= blockpos.getY()+1 && this.entity.posY >= blockpos.getY())
    			{
    				double d1 = (double)this.entity.width / 2.0D;
    				AxisAlignedBB aabb = new AxisAlignedBB((double)x - d1 + 0.5D, (double)y + 1.001D, (double)z - d1 + 0.5D, (double)x + d1 + 0.5D, (double)((float)y + 1+this.entity.height), (double)z + d1 + 0.5D);
    				if (this.entity.world.collidesWithAnyBlock(aabb))
    					return PathNodeType.OPEN;
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
