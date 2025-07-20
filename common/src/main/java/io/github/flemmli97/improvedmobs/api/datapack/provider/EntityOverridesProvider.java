package io.github.flemmli97.improvedmobs.api.datapack.provider;

import io.github.flemmli97.improvedmobs.api.datapack.EntityConfigProperties;
import io.github.flemmli97.improvedmobs.api.datapack.EntityOverridesManager;
import io.github.flemmli97.tenshilib.common.data.provider.CodecBasedProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public abstract class EntityOverridesProvider extends CodecBasedProvider<EntityConfigProperties> {

    public EntityOverridesProvider(PackOutput output, String modid, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, PackOutput.Target.DATA_PACK, modid, EntityOverridesManager.DIRECTORY, EntityConfigProperties.CODEC, provider);
    }

    public void add(ResourceLocation id, EntityConfigProperties properties) {
        this.contents.put(id, properties);
    }
}
