package io.github.flemmli97.improvedmobs.api.item;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.item.impl.BowHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.CrossbowHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.EnchantedBookHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.EnderpearlHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.FishingRodHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.FlintAndSteelHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.LavaBucketHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.ShieldHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.SimpleProjectileHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.ThrowablePotionHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.TntHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.TridentHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.WindChargeHandler;
import io.github.flemmli97.improvedmobs.api.item.impl.integration.SupplementariesBomb;
import io.github.flemmli97.improvedmobs.api.item.impl.integration.TACZGuns;
import io.github.flemmli97.tenshilib.loader.TenshiLibCrossPlat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ItemUseRegistry {

    private static final Map<ResourceLocation, ItemUseHandler> REGISTRY = new HashMap<>();
    private static final Map<ItemUseHandler, ResourceLocation> LOOKUP_REGISTRY = new HashMap<>();

    /**
     * Registers a use handler.
     * This is callable anytime before datapack reloading
     */
    public static synchronized void register(ResourceLocation id, ItemUseHandler handler) {
        if (REGISTRY.put(id, handler) != null) {
            throw new IllegalStateException("Handler with id " + id + " already registered!");
        }
        LOOKUP_REGISTRY.put(handler, id);
    }

    public static ItemUseHandler get(ResourceLocation id) {
        ItemUseHandler handler = REGISTRY.get(id);
        if (handler == null)
            throw new IllegalStateException("No such handler " + id + " registered!");
        return handler;
    }

    public static ResourceLocation getId(ItemUseHandler handler) {
        return LOOKUP_REGISTRY.get(handler);
    }

    public static Collection<ItemUseHandler> getAll() {
        return REGISTRY.values();
    }

    public static void initBuiltin() {
        register(ImprovedMobs.modRes("bow"), new BowHandler());
        register(ImprovedMobs.modRes("crossbow"), new CrossbowHandler());
        register(ImprovedMobs.modRes("enchanted_book"), new EnchantedBookHandler());
        register(ImprovedMobs.modRes("ender_pearl"), new EnderpearlHandler());
        register(ImprovedMobs.modRes("fishing_rod"), new FishingRodHandler());
        register(ImprovedMobs.modRes("flint_and_steel"), new FlintAndSteelHandler());
        register(ImprovedMobs.modRes("lava_bucket"), new LavaBucketHandler());
        register(ImprovedMobs.modRes("shield"), new ShieldHandler());
        register(ImprovedMobs.modRes("snowball"), new SimpleProjectileHandler(item -> item == Items.SNOWBALL,
                e -> 25, entity -> {
            Snowball ball = new Snowball(entity.level(), entity);
            ball.shootFromRotation(entity, entity.getViewXRot(1), entity.getViewYRot(1), 0, 1.5F, 1.0F);
            return ball;
        }, () -> SoundEvents.SNOWBALL_THROW));
        register(ImprovedMobs.modRes("potion"), new ThrowablePotionHandler());
        register(ImprovedMobs.modRes("tnt"), new TntHandler());
        register(ImprovedMobs.modRes("trident"), new TridentHandler());
        register(ImprovedMobs.modRes("wind_charge"), new WindChargeHandler());
        if (TenshiLibCrossPlat.INSTANCE.isModLoaded("supplementaries")) {
            register(ImprovedMobs.modRes("bomb"), new SupplementariesBomb());
        }
        if (TenshiLibCrossPlat.INSTANCE.isModLoaded("tacz")) {
            register(ImprovedMobs.modRes("tacz"), new TACZGuns());
        }
    }
}
