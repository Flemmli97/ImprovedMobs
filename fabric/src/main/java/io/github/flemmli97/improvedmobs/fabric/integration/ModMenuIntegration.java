package io.github.flemmli97.improvedmobs.fabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.fabric.config.ConfigSpecs;
import io.github.flemmli97.tenshilib.common.config.ClothConfigScreenHelper;

import java.util.List;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ClothConfigScreenHelper.configScreenOf(parent, ImprovedMobs.MODID, List.of(ConfigSpecs.clientConfig, ConfigSpecs.commonConfig));
    }
}
