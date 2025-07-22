package io.github.flemmli97.improvedmobs.common.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.Reader;

public abstract class SingleFileResources extends SimplePreparableReloadListener<JsonElement> {

    public static final String CONFIG_DIRECTORY = "improvedmobs/config";
    public static final Gson GSON = new Gson();

    public final ResourceLocation id;
    public final ResourceLocation file;

    protected final HolderLookup.Provider provider;

    public SingleFileResources(ResourceLocation id, HolderLookup.Provider provider) {
        this.id = id;
        this.file = FileToIdConverter.json(CONFIG_DIRECTORY).idToFile(id);
        this.provider = provider;
    }

    @Override
    protected JsonElement prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        try (Reader reader = resourceManager.openAsReader(this.file)) {
            return GsonHelper.fromJson(GSON, reader, JsonElement.class);
        } catch (IllegalArgumentException | IOException | JsonParseException exception) {
            ImprovedMobs.LOGGER.error("Couldn't parse file {}", this.file, exception);
        }
        return JsonNull.INSTANCE;
    }
}
