package io.github.flemmli97.improvedmobs.common.config.equipment;

import io.github.flemmli97.tenshilib.loader.TenshiLibCrossPlat;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.item.Item;

public class OtherScores {

    public static int score(Item item) {
        if (TenshiLibCrossPlat.INSTANCE.isModLoaded("supplementaries")) {
            if (item == ModRegistry.BOMB_BLUE_ITEM.get()) {
                return 1900;
            } else if (item instanceof BombItem) {
                return 1700;
            }
        }
        return -1;
    }
}
