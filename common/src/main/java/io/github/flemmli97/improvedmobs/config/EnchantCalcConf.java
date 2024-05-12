package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.tenshilib.api.config.IConfigListValue;
import io.github.flemmli97.tenshilib.common.utils.SearchUtils;

import java.util.ArrayList;
import java.util.List;

public class EnchantCalcConf implements IConfigListValue<EnchantCalcConf> {

    private static final Value defaultVal = new Value(0, 0, 0);
    private final List<Value> vals = new ArrayList<>();

    public EnchantCalcConf(Value... vals) {
        this.vals.addAll(List.of(vals));
    }

    @Override
    public EnchantCalcConf readFromString(List<String> ss) {
        this.vals.clear();
        List<Value> list = new ArrayList<>();
        for (String s : ss) {
            String[] parts = s.split("-");
            if (parts.length != 3)
                continue;
            list.add(new Value(Float.parseFloat(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        }
        list.sort(null);
        this.vals.addAll(list);
        return this;
    }

    @Override
    public List<String> writeToString() {
        List<String> list = new ArrayList<>();
        this.vals.forEach(v -> list.add(v.diff + "-" + v.min + "-" + v.max));
        return list;
    }

    public Value get(float difficulty) {
        return SearchUtils.searchInfFunc(this.vals, v -> Float.compare(v.diff, difficulty), defaultVal);
    }

    public static class Value implements Comparable<Value> {
        public final int min;
        public final int max;
        public final float diff;

        Value(float diff, int min, int max) {
            this.min = min;
            this.max = max;
            this.diff = diff;
        }

        @Override
        public int compareTo(Value o) {
            return Float.compare(this.diff, o.diff);
        }
    }
}
