package com.flemmli97.improvedmobs.utils;

import com.flemmli97.improvedmobs.config.Config;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;

import javax.annotation.Nullable;

public class CustomBlockCollision extends AbstractIterator<VoxelShape> {

    private final AxisAlignedBB box;
    private final ISelectionContext context;
    private final CubeCoordinateIterator cursor;
    private final BlockPos.Mutable pos;
    private final VoxelShape entityShape;
    private final ICollisionReader collisionGetter;
    private IBlockReader cachedBlockGetter;
    private long cachedBlockGetterPos;

    public CustomBlockCollision(ICollisionReader reader, @Nullable Entity entity, AxisAlignedBB aABB) {
        this.context = entity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(entity);
        this.pos = new BlockPos.Mutable();
        this.entityShape = VoxelShapes.create(aABB);
        this.collisionGetter = reader;
        this.box = aABB;
        int i = MathHelper.floor(aABB.minX - 1.0E-7) - 1;
        int j = MathHelper.floor(aABB.maxX + 1.0E-7) + 1;
        int k = MathHelper.floor(aABB.minY - 1.0E-7) - 1;
        int l = MathHelper.floor(aABB.maxY + 1.0E-7) + 1;
        int m = MathHelper.floor(aABB.minZ - 1.0E-7) - 1;
        int n = MathHelper.floor(aABB.maxZ + 1.0E-7) + 1;
        this.cursor = new CubeCoordinateIterator(i, k, m, j, l, n);
    }

    @Nullable
    private IBlockReader getChunk(int i, int j) {
        IBlockReader blockGetter;
        int k = i >> 4;
        int l = j >> 4;
        long m = ChunkPos.asLong(k, l);
        if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == m) {
            return this.cachedBlockGetter;
        }
        this.cachedBlockGetter = blockGetter = this.collisionGetter.getBlockReader(k, l);
        this.cachedBlockGetterPos = m;
        return blockGetter;
    }

    @Override
    protected VoxelShape computeNext() {
        while (this.cursor.hasNext()) {
            IBlockReader blockGetter;
            int i = this.cursor.getX();
            int j = this.cursor.getY();
            int k = this.cursor.getZ();
            int l = this.cursor.numBoundariesTouched();
            if (l == 3 || (blockGetter = this.getChunk(i, k)) == null) continue;
            this.pos.setPos(i, j, k);
            BlockState blockState = blockGetter.getBlockState(this.pos);
            if (Config.CommonConfig.breakableBlocks.canBreak(blockState, this.pos, blockGetter, this.context) || l == 1 && !blockState.isCollisionShapeLargerThanFullBlock() || l == 2 && !blockState.matchesBlock(Blocks.MOVING_PISTON))
                continue;
            VoxelShape voxelShape = blockState.getCollisionShape(this.collisionGetter, this.pos, this.context);
            if (voxelShape == VoxelShapes.fullCube()) {
                if (!this.box.intersects(i, j, k, (double) i + 1.0, (double) j + 1.0, (double) k + 1.0)) continue;
                return voxelShape.withOffset(i, j, k);
            }
            VoxelShape voxelShape2 = voxelShape.withOffset(i, j, k);
            if (!VoxelShapes.compare(voxelShape2, this.entityShape, IBooleanFunction.AND)) continue;
            return voxelShape2;
        }
        return this.endOfData();
    }
}