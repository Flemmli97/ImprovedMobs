package io.github.flemmli97.improvedmobs.neoforge.data;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.ImprovedMobsTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class MobEffectTagGen extends TagsProvider<MobEffect> {

    public MobEffectTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture, ExistingFileHelper existingFileHelper) {
        super(output, Registries.MOB_EFFECT, completableFuture, ImprovedMobs.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ImprovedMobsTags.ENCHANTED_BOOK_EFFECT)
                .add(MobEffects.REGENERATION.getKey(),
                        MobEffects.MOVEMENT_SPEED.getKey(),
                        MobEffects.DAMAGE_BOOST.getKey(),
                        MobEffects.INVISIBILITY.getKey(),
                        MobEffects.DAMAGE_RESISTANCE.getKey(),
                        MobEffects.FIRE_RESISTANCE.getKey());
        BuiltInRegistries.MOB_EFFECT.holders().forEach(eff -> {
            if (eff.value().getCategory() == MobEffectCategory.HARMFUL) {
                this.tag(ImprovedMobsTags.HARMFUL_EFFECT)
                        .add(eff.key());
            }
        });
    }
}
