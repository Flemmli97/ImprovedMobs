package io.github.flemmli97.improvedmobs.common.entities.ai;

import io.github.flemmli97.improvedmobs.api.difficulty.DifficultyFetcher;
import io.github.flemmli97.improvedmobs.common.config.Config;
import io.github.flemmli97.improvedmobs.common.utils.BlockRestorationData;
import io.github.flemmli97.improvedmobs.common.utils.Utils;
import io.github.flemmli97.improvedmobs.mixinhelper.PathNavigateData;
import io.github.flemmli97.improvedmobs.platform.CrossPlatformStuff;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class BlockBreakGoal extends Goal {

    protected final Mob living;
    private final VariableMap variables = new VariableMap();

    private BlockPos diggingPosition;
    private Vec3 lastPos;
    private int digTimer;
    private int cooldown = Config.CommonConfig.breakerInitCooldown;

    public BlockBreakGoal(Mob living) {
        this.living = living;
    }

    @Override
    public boolean canUse() {
        if (!Utils.canBreakBlocks(this.living)) {
            return false;
        }
        if (this.lastPos == null) {
            this.lastPos = this.living.position();
            this.cooldown = Config.CommonConfig.breakerCooldown;
        }
        if (--this.cooldown <= 0) {
            if (this.lastPos.distanceToSqr(this.living.position()) > 0.2) {
                this.lastPos = null;
                this.cooldown = Config.CommonConfig.breakerCooldown;
                return false;
            } else {
                BlockPos blockPos = this.getDiggingLocation();
                if (blockPos == null)
                    return false;
                ((PathNavigateData) this.living.getNavigation()).improvedMobs$setMining(true);
                this.cooldown = Config.CommonConfig.breakerCooldown;
                this.diggingPosition = blockPos;
                this.lastPos = this.living.position();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return Utils.canBreakBlocks(this.living) && this.living.isAlive()
                && this.diggingPosition != null && this.lastPos.distanceToSqr(this.living.position()) <= 0.2;
    }

    @Override
    public void stop() {
        ((PathNavigateData) this.living.getNavigation()).improvedMobs$setMining(false);
        if (this.diggingPosition != null)
            this.living.level().destroyBlockProgress(this.living.getId(), this.diggingPosition, -1);
        this.diggingPosition = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.diggingPosition == null) {
            this.digTimer = 0;
            return;
        }
        BlockState state = this.living.level().getBlockState(this.diggingPosition);
        float str = Utils.getBlockStrength(this.living, state, this.living.level(), this.diggingPosition);
        str = str == Float.POSITIVE_INFINITY ? 1 : str / (1 + str * 6) * (this.digTimer * this.breakSpeedMod() + 1);
        if (str >= 1F) {
            this.digTimer = 0;
            this.cooldown *= 0.5;
            ItemStack item = this.living.getMainHandItem();
            ItemStack itemOff = this.living.getOffhandItem();
            boolean canHarvest;
            if (Config.CommonConfig.restoreDelay > 0 && this.living.level() instanceof ServerLevel serverLevel) {
                canHarvest = false;
                BlockRestorationData.get(serverLevel)
                        .restore(serverLevel, serverLevel.getBlockState(this.diggingPosition), this.diggingPosition, this.living);
            } else
                canHarvest = Utils.canHarvest(state, item) || Utils.canHarvest(state, itemOff);
            this.living.level().destroyBlock(this.diggingPosition, canHarvest);
            this.living.level().destroyBlockProgress(this.living.getId(), this.diggingPosition, -1);
            this.diggingPosition = null;
        } else {
            this.digTimer++;
            if (this.digTimer % 5 == 0) {
                SoundType sound = CrossPlatformStuff.INSTANCE.blockSound(state, this.living, this.diggingPosition);
                this.living.level().playSeededSound(null, this.diggingPosition.getX() + 0.5, this.diggingPosition.getY() + 0.5, this.diggingPosition.getZ() + 0.5, Config.CommonConfig.useBlockBreakSound ? BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.getBreakSound()) : SoundEvents.NOTE_BLOCK_BASS, SoundSource.BLOCKS, 2F, 0.5F, this.living.level().getRandom().nextLong());
                this.living.swing(InteractionHand.MAIN_HAND);
                this.living.getLookControl().setLookAt(this.diggingPosition.getX(), this.diggingPosition.getY(), this.diggingPosition.getZ(), 0.0F, 0.0F);
                this.living.level().destroyBlockProgress(this.living.getId(), this.diggingPosition, (int) (str * 10) - 1);
            }
        }
    }

    private float breakSpeedMod() {
        return (float) Config.CommonConfig.breakSpeed.get(Config.apply(this.variables, this.living,
                DifficultyFetcher.getDifficulty((ServerLevel) this.living.level(), this.living.position())));
    }

    public BlockPos getDiggingLocation() {
        Path path = this.living.getNavigation().getPath();
        BlockPos currentPos = this.living.blockPosition();
        BlockPos direction;
        if (path == null || path.isDone()) {
            direction = BlockPos.ZERO;
        } else {
            Node node = path.getNextNode();
            // We dig towards the next node
            // Reason the node is not directly used is that there are cases were the location of the mob and the position of the next node
            // don't match correctly (often with bigger mobs)
            direction = new BlockPos(
                    Math.clamp(node.x - this.living.getBlockX(), -1, 1),
                    Math.clamp(node.y - this.living.getBlockY(), -2, 1),
                    Math.clamp(node.z - this.living.getBlockZ(), -1, 1));
        }
        int digHeight = Mth.floor(this.living.getBbHeight() + 1 + (direction.getY() < 0 ? Math.abs(direction.getY()) : 0));
        int digWidth = Mth.floor(this.living.getBbWidth() + 1);
        if (direction.getX() != 0 && direction.getZ() != 0) {
            // For diagonal nodes we need to break more blocks
            // For this try each individual axis separately
            BlockPos pos = this.getBreakablePosition(digHeight, digWidth,
                    currentPos.getX() + direction.getX(), currentPos.getY() + direction.getY(), currentPos.getZ());
            if (pos != null) {
                return pos;
            }
            pos = this.getBreakablePosition(digHeight, digWidth,
                    currentPos.getX(), currentPos.getY() + direction.getY(), currentPos.getZ() + direction.getZ());
            if (pos != null) {
                return pos;
            }
        }
        return this.getBreakablePosition(digHeight, digWidth,
                currentPos.getX() + direction.getX(), currentPos.getY() + direction.getY(), currentPos.getZ() + direction.getZ());
    }

    private BlockPos getBreakablePosition(int height, int width, int px, int py, int pz) {
        if (width > 1)
            width = (int) Math.ceil(width * 0.5);
        int minZ = width > 1 ? -width : 0;
        int minX = width > 1 ? -width : 0;
        for (int y = 0; y < height; ++y) {
            for (int z = minZ; z < width; ++z) {
                for (int x = minX; x < width; ++x) {
                    BlockPos offset = new BlockPos(x, y, z);
                    BlockPos pos = this.getPosFromOffset(offset, px, py, pz);
                    BlockState state = this.living.level().getBlockState(pos);
                    if (Utils.canBreakState(this.living, state)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private BlockPos getPosFromOffset(BlockPos offset, int x, int y, int z) {
        offset = offset.rotate(this.getDigDirection(x, z)).mutable();
        return new BlockPos(x + offset.getX(),
                y + offset.getY(),
                z + offset.getZ());
    }

    public Rotation getDigDirection(int x, int z) {
        Vec3 dir = new Vec3(x + 0.5, this.living.position().y, z + 0.5).subtract(this.living.position());
        if (Math.abs(dir.x) <= Math.abs(dir.z)) {
            if (dir.z >= 0)
                return Rotation.NONE;
            return Rotation.CLOCKWISE_180;
        }
        if (dir.x >= 0)
            return Rotation.COUNTERCLOCKWISE_90;
        return Rotation.CLOCKWISE_90;
    }
}