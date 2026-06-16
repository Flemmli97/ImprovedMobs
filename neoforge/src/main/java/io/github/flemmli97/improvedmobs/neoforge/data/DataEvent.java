package io.github.flemmli97.improvedmobs.neoforge.data;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ImprovedMobs.MODID)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent event) {
        DataGenerator data = event.getGenerator();
        data.addProvider(true, new BlockTagGen(data.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        data.addProvider(true, new MobEffectTagGen(data.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        data.addProvider(true, new DefaultAttributeProvider(data.getPackOutput(), event.getLookupProvider()));
//        data.addProvider(true, new EntityOverrideTest(data.getPackOutput(), event.getLookupProvider()));
    }
}
