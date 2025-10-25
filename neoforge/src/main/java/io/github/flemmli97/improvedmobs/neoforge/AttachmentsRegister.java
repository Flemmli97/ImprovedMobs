package io.github.flemmli97.improvedmobs.neoforge;

import io.github.flemmli97.improvedmobs.common.difficulty.PlayerDifficulty;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Well... used wrong mod id so this is here for moving it over to correct id
 */
public class AttachmentsRegister {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TenshiLib.MODID);

    public static final IAttachmentSerializer<CompoundTag, PlayerDifficulty> DIFFICULTY_SERIALIZER = new IAttachmentSerializer<>() {

        @Override
        public PlayerDifficulty read(IAttachmentHolder holder, CompoundTag arg, HolderLookup.Provider provider) {
            PlayerDifficulty cap = new PlayerDifficulty();
            cap.read(arg, provider);
            return cap;
        }

        // Don't serialize this wrong attachment
        @Override
        public CompoundTag write(PlayerDifficulty object, HolderLookup.Provider provider) {
            return null;
        }
    };
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerDifficulty>> PLAYER_DIFFICULTY = ATTACHMENT_TYPES.register("player_difficulty", () -> AttachmentType.builder(() -> new PlayerDifficulty()).serialize(DIFFICULTY_SERIALIZER).build());
}
