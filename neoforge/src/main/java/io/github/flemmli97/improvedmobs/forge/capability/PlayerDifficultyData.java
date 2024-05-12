package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.improvedmobs.difficulty.IPlayerDifficulty;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class PlayerDifficultyData implements IPlayerDifficulty {

    public static final IAttachmentSerializer<CompoundTag, PlayerDifficultyData> SERIALIZER = new IAttachmentSerializer<CompoundTag, PlayerDifficultyData>() {

        @Override
        public PlayerDifficultyData read(IAttachmentHolder holder, CompoundTag arg, HolderLookup.Provider provider) {
            PlayerDifficultyData cap = new PlayerDifficultyData();
            cap.load(arg);
            return cap;
        }

        @Override
        public CompoundTag write(PlayerDifficultyData object, HolderLookup.Provider provider) {
            CompoundTag compound = new CompoundTag();
            object.save(compound);
            return compound;
        }
    };

    private float difficultyLevel;

    @Override
    public void setDifficultyLevel(float level) {
        this.difficultyLevel = level;
    }

    @Override
    public float getDifficultyLevel() {
        return this.difficultyLevel;
    }

    @Override
    public void load(CompoundTag nbt) {
        this.difficultyLevel = nbt.getFloat("IMDifficulty");
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putFloat("IMDifficulty", this.difficultyLevel);
        return compound;
    }
}
