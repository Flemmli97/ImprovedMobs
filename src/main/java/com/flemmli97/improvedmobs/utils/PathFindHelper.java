package com.flemmli97.improvedmobs.utils;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class PathFindHelper {

    public static PathNodeType getLandNodeType(IBlockReader worldIn, BlockPos.Mutable pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        PathNodeType pathnodetype = getCommonNodeType(worldIn, pos);
        if (pathnodetype == PathNodeType.OPEN && j >= 1) {
            PathNodeType pathnodetype1 = getCommonNodeType(worldIn, pos.setPos(i, j - 1, k));
            pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
            if (pathnodetype1 == PathNodeType.DAMAGE_FIRE) {
                pathnodetype = PathNodeType.DAMAGE_FIRE;
            }

            if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
                pathnodetype = PathNodeType.DAMAGE_CACTUS;
            }

            if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) {
                pathnodetype = PathNodeType.DAMAGE_OTHER;
            }

            if (pathnodetype1 == PathNodeType.STICKY_HONEY) {
                pathnodetype = PathNodeType.STICKY_HONEY;
            }
        }

        if (pathnodetype == PathNodeType.WALKABLE) {
            pathnodetype = getNodeTypeFromNeighbors(worldIn, pos.setPos(i, j, k), pathnodetype);
        }

        return pathnodetype;
    }

    public static PathNodeType getNodeTypeFromNeighbors(IBlockReader worldIn, BlockPos.Mutable centerPos, PathNodeType nodeType) {
        int i = centerPos.getX();
        int j = centerPos.getY();
        int k = centerPos.getZ();

        for (int l = -1; l <= 1; ++l) {
            for (int i1 = -1; i1 <= 1; ++i1) {
                for (int j1 = -1; j1 <= 1; ++j1) {
                    if (l != 0 || j1 != 0) {
                        centerPos.setPos(i + l, j + i1, k + j1);
                        BlockState blockstate = worldIn.getBlockState(centerPos);
                        if (blockstate.matchesBlock(Blocks.CACTUS)) {
                            return PathNodeType.DANGER_CACTUS;
                        }

                        if (blockstate.matchesBlock(Blocks.SWEET_BERRY_BUSH)) {
                            return PathNodeType.DANGER_OTHER;
                        }

                        if (method_27138(blockstate)) {
                            return PathNodeType.DANGER_FIRE;
                        }

                        if (worldIn.getFluidState(centerPos).isTagged(FluidTags.WATER)) {
                            return PathNodeType.WATER_BORDER;
                        }
                    }
                }
            }
        }

        return nodeType;
    }

    protected static PathNodeType getCommonNodeType(IBlockReader reader, BlockPos pos) {
        BlockState blockstate = reader.getBlockState(pos);
        PathNodeType type = blockstate.getAiPathNodeType(reader, pos);
        if (type != null) return type;
        Block block = blockstate.getBlock();
        Material material = blockstate.getMaterial();
        if (blockstate.getBlock().isAir(blockstate, reader, pos)) {
            return PathNodeType.OPEN;
        } else if (!blockstate.isIn(BlockTags.TRAPDOORS) && !blockstate.matchesBlock(Blocks.LILY_PAD)) {
            if (blockstate.matchesBlock(Blocks.CACTUS)) {
                return PathNodeType.DAMAGE_CACTUS;
            } else if (blockstate.matchesBlock(Blocks.SWEET_BERRY_BUSH)) {
                return PathNodeType.DAMAGE_OTHER;
            } else if (blockstate.matchesBlock(Blocks.HONEY_BLOCK)) {
                return PathNodeType.STICKY_HONEY;
            } else if (blockstate.matchesBlock(Blocks.COCOA)) {
                return PathNodeType.COCOA;
            } else {
                FluidState fluidstate = reader.getFluidState(pos);
                if (fluidstate.isTagged(FluidTags.WATER)) {
                    return PathNodeType.WATER;
                } else if (fluidstate.isTagged(FluidTags.LAVA)) {
                    return PathNodeType.LAVA;
                } else if (method_27138(blockstate)) {
                    return PathNodeType.DAMAGE_FIRE;
                } else if (DoorBlock.isWooden(blockstate) && !blockstate.get(DoorBlock.OPEN)) {
                    return PathNodeType.DOOR_WOOD_CLOSED;
                } else if (block instanceof DoorBlock && material == Material.IRON && !blockstate.get(DoorBlock.OPEN)) {
                    return PathNodeType.DOOR_IRON_CLOSED;
                } else if (block instanceof DoorBlock && blockstate.get(DoorBlock.OPEN)) {
                    return PathNodeType.DOOR_OPEN;
                } else if (block instanceof AbstractRailBlock) {
                    return PathNodeType.RAIL;
                } else if (block instanceof LeavesBlock) {
                    return PathNodeType.LEAVES;
                } else if (!block.isIn(BlockTags.FENCES) && !block.isIn(BlockTags.WALLS) && (!(block instanceof FenceGateBlock) || blockstate.get(FenceGateBlock.OPEN))) {
                    return !blockstate.allowsMovement(reader, pos, PathType.LAND) ? PathNodeType.BLOCKED : PathNodeType.OPEN;
                } else {
                    return PathNodeType.FENCE;
                }
            }
        } else {
            return PathNodeType.TRAPDOOR;
        }
    }

    private static boolean method_27138(BlockState state) {
        return state.isIn(BlockTags.FIRE) || state.matchesBlock(Blocks.LAVA) || state.matchesBlock(Blocks.MAGMA_BLOCK) || CampfireBlock.isLit(state);
    }
}
