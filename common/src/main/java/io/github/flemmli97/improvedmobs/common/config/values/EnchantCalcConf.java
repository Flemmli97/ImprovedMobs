package io.github.flemmli97.improvedmobs.common.config.values;

import io.github.flemmli97.tenshilib.common.utils.SearchUtils;

import java.util.ArrayList;
import java.util.List;

public class EnchantCalcConf {

    private static final Value DEFAULT_VALUE = new Value(0, 0, 0);

    private final List<Value> values = new ArrayList<>();

    public EnchantCalcConf(Value... values) {
        this.values.addAll(List.of(values));
    }

    public Value get(float difficulty) {
        return SearchUtils.searchInfFunc(this.values, v -> Float.compare(v.requiredDifficulty(), difficulty), DEFAULT_VALUE);
    }

    public void read(List<String> ss) {
        this.values.clear();
        List<Value> list = new ArrayList<>();
        for (String s : ss) {
            String[] parts = s.split("-");
            if (parts.length != 3)
                continue;
            list.add(new Value(Float.parseFloat(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        }
        list.sort(null);
        this.values.addAll(list);
    }

    public List<String> write() {
        List<String> list = new ArrayList<>();
        this.values.forEach(v -> list.add(v.write()));
        return list;
    }

    public record Value(float requiredDifficulty, int min, int max) implements Comparable<Value> {

        public String write() {
            return String.format("%s-%s-%s", this.requiredDifficulty, this.min, this.max);
        }

        @Override
        public int compareTo(Value o) {
            return Float.compare(this.requiredDifficulty, o.requiredDifficulty);
        }
    }
}
