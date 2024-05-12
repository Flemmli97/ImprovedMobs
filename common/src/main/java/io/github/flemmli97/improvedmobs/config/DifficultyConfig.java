package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.tenshilib.common.utils.SearchUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class DifficultyConfig {

    private static final Pair<Float, Zone> DEFAULT_VAL = Pair.of(0f, new Zone(1, 0.01f));
    private final List<Pair<Float, Zone>> vals = new ArrayList<>();

    public DifficultyConfig(List<Pair<Float, Zone>> defaultVal) {
        this.vals.addAll(defaultVal);
    }

    public DifficultyConfig readFromString(List<String> ss) {
        this.vals.clear();
        List<Pair<Float, Zone>> list = new ArrayList<>();
        for (String s : ss) {
            String[] parts = s.split("-");
            if (parts.length == 3)
                list.add(Pair.of(Float.parseFloat(parts[0]), new Zone(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]))));
            else if (parts.length == 2)
                list.add(Pair.of(Float.parseFloat(parts[0]), new Zone(Float.parseFloat(parts[1]), 0)));
        }
        list.sort((o1, o2) -> Float.compare(o1.getLeft(), o2.getLeft()));
        this.vals.addAll(list);
        return this;
    }

    public List<String> writeToString() {
        List<String> list = new ArrayList<>();
        this.vals.forEach(v -> list.add(v.getLeft() + "-" + v.getRight().write()));
        return list;
    }

    public Pair<Float, Zone> get(float difficulty) {
        return SearchUtils.searchInfFunc(this.vals, v -> Float.compare(v.getLeft(), difficulty), DEFAULT_VAL);
    }

    public record Zone(float start, float increasePerBlock) {

        public static Zone of(float start) {
            return new Zone(start, 0);
        }

        String write() {
            if (this.increasePerBlock == 0)
                return this.start + "";
            return this.start + "-" + this.increasePerBlock;
        }
    }
}