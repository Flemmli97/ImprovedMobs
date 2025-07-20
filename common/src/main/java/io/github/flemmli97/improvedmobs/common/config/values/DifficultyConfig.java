package io.github.flemmli97.improvedmobs.common.config.values;

import io.github.flemmli97.tenshilib.common.utils.SearchUtils;

import java.util.ArrayList;
import java.util.List;

public class DifficultyConfig {

    private static final Value DEFAULT_VALUE = new Value(0, 1, 0.01f);

    private final List<Value> values = new ArrayList<>();

    public DifficultyConfig(Value... values) {
        this.values.addAll(List.of(values));
    }

    public Value get(float difficulty) {
        return SearchUtils.searchInfFunc(this.values, v -> Float.compare(v.requiredDifficulty(), difficulty), DEFAULT_VALUE);
    }

    public void read(List<String> ss) {
        this.values.clear();
        List<Value> list = new ArrayList<>();
        for (String s : ss) {
            String[] parts = s.contains("|") ? s.split("\\|") : s.split("-");
            if (parts.length == 3)
                list.add(new Value(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
            else if (parts.length == 2)
                list.add(new Value(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), 0));
        }
        list.sort(null);
        this.values.addAll(list);
    }

    public List<String> write() {
        List<String> list = new ArrayList<>();
        this.values.forEach(v -> list.add(v.write()));
        return list;
    }

    public record Value(float requiredDifficulty, float start, float increasePerBlock) implements Comparable<Value> {

        public static Value of(float requiredDifficulty, float start) {
            return new Value(requiredDifficulty, start, 0);
        }

        String write() {
            if (this.increasePerBlock == 0)
                return String.format("%s-%s", this.requiredDifficulty, this.start);
            return String.format("%s-%s-%s", this.requiredDifficulty, this.start, this.increasePerBlock);
        }

        @Override
        public int compareTo(Value other) {
            return Float.compare(this.requiredDifficulty, other.requiredDifficulty);
        }
    }
}