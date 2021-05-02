package com.flemmli97.improvedmobs.mixin;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEntity.class)
public interface MobEntityMixin {

    @Accessor("deathLootTable")
    void setDeathLootTable(ResourceLocation res);

    @Accessor("navigator")
    PathNavigator getTrueNavigator();
}
