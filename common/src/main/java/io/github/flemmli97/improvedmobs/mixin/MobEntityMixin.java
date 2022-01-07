package io.github.flemmli97.improvedmobs.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobEntityMixin {

    @Accessor("lootTable")
    void setDeathLootTable(ResourceLocation res);

    @Accessor("navigation")
    PathNavigation getTrueNavigator();
}
