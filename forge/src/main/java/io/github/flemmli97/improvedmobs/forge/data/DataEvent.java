package io.github.flemmli97.improvedmobs.forge.data;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ImprovedMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent event) {
        DataGenerator data = event.getGenerator();
        if (event.includeServer()) {
            data.addProvider(true, new BlockTagGen(data.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
        }
    }
}
