package io.github.flemmli97.improvedmobs.neoforge.data;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.datapack.DifficultyAttributeProperty;
import io.github.flemmli97.improvedmobs.common.datapack.DifficultyAttributeConfig;
import io.github.flemmli97.tenshilib.common.data.provider.CodecBasedProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class DefaultAttributeProvider extends CodecBasedProvider<DifficultyAttributeProperty> {

    public DefaultAttributeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, PackOutput.Target.DATA_PACK, ImprovedMobs.MODID, "config", DifficultyAttributeProperty.CODEC, provider);
    }

    @Override
    protected void add(HolderLookup.Provider provider) {
        this.contents.put(DifficultyAttributeConfig.create(provider).id, DifficultyAttributeConfig.DEFAULT);
    }
}
