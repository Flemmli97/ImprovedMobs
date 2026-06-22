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

    public static ItemScore score(Item item, double durability, double damage, double armor, double toughness, double knockbackResistance, int enchantmentValue) {
        if (TenshiLibCrossPlat.INSTANCE.isModLoaded("supplementaries")) {
            if (item == ModRegistry.BOMB_BLUE_ITEM.get()) {
                return new ItemScore(durability, 25, armor, toughness, knockbackResistance, enchantmentValue, 2100, 1);
            } else if (item instanceof BombItem) {
                return new ItemScore(durability, 20, armor, toughness, knockbackResistance, enchantmentValue, 2250, 1);
            }
        }
        return null;
    }

    public static void addItems(Map<EquipmentSlot, List<Pair<ItemScore, OptionalItemStack>>> scores, HolderLookup.Provider provider) {
        if (TenshiLibCrossPlat.INSTANCE.isModLoaded("tacz")) {
            TimelessAPI.getAllCommonGunIndex().forEach(entry -> {
                CommonGunIndex index = entry.getValue();
                GunData gunData = index.getGunData();
                GunTabType type = TACZGuns.TYPE_LOOKUP.get(index.getType());
                int score = 500;
                score += type == null ? 750 : switch (type) {
                    case PISTOL -> 750;
                    case RIFLE, SMG -> 1000;
                    case SHOTGUN -> 1500;
                    case RPG -> 1650;
                    case SNIPER -> 1750;
                    case MG -> 2000;
                };
                double rate = gunData.getRoundsPerMinute(gunData.getFireModeSet().getFirst());
                score += switch (gunData.getFireModeSet().getFirst()) {
                    case AUTO -> 400;
                    case SEMI -> 300;
                    case BURST -> {
                        rate = index.getGunData().getBurstData().getBpm();
                        yield 250;
                    }
                    case UNKNOWN -> 0;
                };
                score += (int) Math.ceil(rate);
                ItemStack stack = GunItemBuilder.create()
                        .setId(entry.getKey())
                        .setFireMode(gunData.getFireModeSet().getFirst())
                        .setAmmoCount(gunData.getAmmoAmount())
                        .setHeatData(gunData.hasHeatData())
                        .setAmmoInBarrel(true)
                        .build(provider);
                scores.computeIfAbsent(EquipmentSlot.MAINHAND, k -> new ArrayList<>())
                        .add(Pair.of(new ItemScore(gunData.getAmmoAmount() * 50,
                                (gunData.getBulletData().getDamageAmount() * gunData.getBulletData().getBulletAmount()),
                                0, 0, 0, 0, score, 1.5), new OptionalItemStack(stack)));
            });
        }
    }
}
