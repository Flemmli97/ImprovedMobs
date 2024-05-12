package io.github.flemmli97.improvedmobs.forge.data;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = ImprovedMobs.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent event) {
        DataGenerator data = event.getGenerator();
        if (event.includeServer()) {
            data.addProvider(true, new BlockTagGen(data.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        }
    }
}
