package io.github.flemmli97.improvedmobs.api.difficulty;

import io.github.flemmli97.improvedmobs.ImprovedMobs;
import io.github.flemmli97.improvedmobs.api.difficulty.impl.DefaultDifficulty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DifficultyFetcher {

    private static final ResourceLocation DEFAULT = ImprovedMobs.modRes("default");
    private static final List<OrderedResource> ORDERED_RESOURCES = new ArrayList<>();
    private static final Map<ResourceLocation, DifficultyGetter> DIFFICULTIES = new HashMap<>();

    public static void register() {
        add(DEFAULT, -1, new DefaultDifficulty());
    }

    public static synchronized void add(ResourceLocation id, DifficultyGetter impl) {
        add(id, ORDERED_RESOURCES.size(), impl);
    }

    public static synchronized void add(ResourceLocation id, int order, DifficultyGetter impl) {
        ORDERED_RESOURCES.add(new OrderedResource(order, id));
        ORDERED_RESOURCES.sort(Collections.reverseOrder());
        DIFFICULTIES.put(id, impl);
    }

    public static boolean shouldClientShowDifficulty() {
        int base = 0;
        List<Boolean> adding = new ArrayList<>();
        for (OrderedResource r : ORDERED_RESOURCES) {
            DifficultyGetter v = DIFFICULTIES.get(r.res);
            switch (v.getType()) {
                case ON -> {
                    if (base == 0)
                        base = v.hasOwnDisplay() ? 2 : 1;
                }
                case ADD -> adding.add(v.hasOwnDisplay());
            }
        }
        // If base = 2 indicates the integration has its own difficulty display so we dont show ours.
        // Unless we use a modified combined difficulty
        return base < 2 || (adding.size() == 1 && !adding.get(0)) || !adding.isEmpty();
    }

    public static float getDifficulty(ServerLevel level, Vec3 pos) {
        float difficulty = 0;
        float toAdd = 0;
        for (OrderedResource r : ORDERED_RESOURCES) {
            DifficultyGetter v = DIFFICULTIES.get(r.res);
            switch (v.getType()) {
                case ON -> {
                    // First enabled one is used as a base
                    if (difficulty == 0)
                        difficulty = v.getDifficulty(level, pos);
                }
                case ADD -> toAdd += v.getDifficulty(level, pos);
            }
        }
        return difficulty + toAdd;
    }

    record OrderedResource(int order, ResourceLocation res) implements Comparable<OrderedResource> {

        @Override
        public int compareTo(@NotNull DifficultyFetcher.OrderedResource o) {
            return this.order == o.order ? this.res.compareTo(o.res) : Integer.compare(this.order, o.order);
        }
    }
}
