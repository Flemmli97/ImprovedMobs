package io.github.flemmli97.improvedmobs.forge.capability;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class Attachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TenshiLib.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OpenContainerData>> HAS_BEEN_OPENED = ATTACHMENT_TYPES.register("has_been_opened", () -> AttachmentType.builder(OpenContainerData::new).serialize(OpenContainerData.SERIALIZER).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerDifficultyData>> PLAYER_DIFFICULTY = ATTACHMENT_TYPES.register("player_difficulty", () -> AttachmentType.builder(PlayerDifficultyData::new).serialize(PlayerDifficultyData.SERIALIZER).build());

}
