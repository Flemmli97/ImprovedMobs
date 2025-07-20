package io.github.flemmli97.improvedmobs.neoforge;

import io.github.flemmli97.improvedmobs.common.difficulty.PlayerDifficulty;
import io.github.flemmli97.improvedmobs.common.utils.ContainerOpened;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AttachmentsRegister {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TenshiLib.MODID);

    public static final IAttachmentSerializer<CompoundTag, PlayerDifficulty> DIFFICULTY_SERIALIZER = new IAttachmentSerializer<>() {

        @Override
        public PlayerDifficulty read(IAttachmentHolder holder, CompoundTag arg, HolderLookup.Provider provider) {
            PlayerDifficulty cap = new PlayerDifficulty();
            cap.load(arg);
            return cap;
        }

        @Override
        public CompoundTag write(PlayerDifficulty object, HolderLookup.Provider provider) {
            CompoundTag compound = new CompoundTag();
            object.save(compound);
            return compound;
        }
    };

    public static final IAttachmentSerializer<CompoundTag, ContainerOpened> CONTAINER_OPEN_SERIALIZER = new IAttachmentSerializer<>() {

        @Override
        public ContainerOpened read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            ContainerOpened cap = new ContainerOpened();
            cap.readFromNBT(tag);
            return cap;
        }

        @Override
        public CompoundTag write(ContainerOpened object, HolderLookup.Provider provider) {
            CompoundTag compound = new CompoundTag();
            object.writeToNBT(compound);
            return compound;
        }
    };

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ContainerOpened>> HAS_BEEN_OPENED = ATTACHMENT_TYPES.register("has_been_opened", () -> AttachmentType.builder(ContainerOpened::new).serialize(CONTAINER_OPEN_SERIALIZER).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerDifficulty>> PLAYER_DIFFICULTY = ATTACHMENT_TYPES.register("player_difficulty", () -> AttachmentType.builder(PlayerDifficulty::new).serialize(DIFFICULTY_SERIALIZER).build());
}
