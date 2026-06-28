package io.github.flemmli97.improvedmobs.common.registry;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.common.difficulty.PlayerDifficulty;
import io.github.flemmli97.improvedmobs.common.utils.ContainerOpened;
import io.github.flemmli97.tenshilib.common.attachment.AttachmentType;
import io.github.flemmli97.tenshilib.loader.registry.AttachmentRegister;

import java.util.function.Supplier;

public class ImprovedMobsAttachments {

    public static final AttachmentRegister.AttachmentRegistry ATTACHMENTS = AttachmentRegister.INSTANCE.of(ImprovedMobs.MODID);

    public static final Supplier<AttachmentType<Object, PlayerDifficulty>> PLAYER_DIFFICULTY = ATTACHMENTS.register("player_difficulty", AttachmentType.builder(() -> new PlayerDifficulty())
            .transferHandler(((from, targetHolder, wasDead) -> new PlayerDifficulty(from))));
    public static final Supplier<AttachmentType<Object, ContainerOpened>> HAS_BEEN_OPENED = ATTACHMENTS.register("has_been_opened", AttachmentType.builder(ContainerOpened::new));
}
