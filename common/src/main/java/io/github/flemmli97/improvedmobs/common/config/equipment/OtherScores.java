package io.github.flemmli97.improvedmobs.common.config.equipment;

import com.mojang.datafixers.util.Pair;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import io.github.flemmli97.improvedmobs.api.item.impl.integration.TACZGuns;
import io.github.flemmli97.tenshilib.loader.TenshiLibCrossPlat;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtherScores {

    public static int score(Item item) {
        if (TenshiLibCrossPlat.INSTANCE.isModLoaded("supplementaries")) {
            if (item == ModRegistry.BOMB_BLUE_ITEM.get()) {
                return 3000;
            } else if (item instanceof BombItem) {
                return 2800;
            }
        }
        return -1;
    }

    public static void addItems(Map<EquipmentSlot, List<Pair<Integer, OptionalItemStack>>> scores, HolderLookup.Provider provider) {
        if (TenshiLibCrossPlat.INSTANCE.isModLoaded("tacz")) {
            TimelessAPI.getAllCommonGunIndex().forEach(entry -> {
                CommonGunIndex index = entry.getValue();
                GunData gunData = index.getGunData();
                GunTabType type = TACZGuns.TYPE_LOOKUP.get(index.getType());
                int score = 1000;
                score += type == null ? 3500 : switch (type) {
                    case PISTOL -> 3500;
                    case RIFLE, SMG -> 4000;
                    case SHOTGUN -> 4500;
                    case RPG -> 4600;
                    case SNIPER -> 4900;
                    case MG -> 5000;
                };
                score += switch (gunData.getFireModeSet().getFirst()) {
                    case AUTO -> 500;
                    case SEMI -> 300;
                    case BURST -> 250;
                    case UNKNOWN -> 0;
                };
                score += gunData.getAmmoAmount() * 50;
                score += gunData.getRoundsPerMinute(gunData.getFireModeSet().getFirst()) * 10;
                ItemStack stack = GunItemBuilder.create()
                        .setId(entry.getKey())
                        .setFireMode(gunData.getFireModeSet().getFirst())
                        .setAmmoCount(gunData.getAmmoAmount())
                        .setHeatData(gunData.hasHeatData())
                        .setAmmoInBarrel(true)
                        .build(provider);
                scores.computeIfAbsent(EquipmentSlot.MAINHAND, k -> new ArrayList<>())
                        .add(Pair.of(score, new OptionalItemStack(stack)));
            });
        }
    }
}
