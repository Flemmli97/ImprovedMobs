package com.flemmli97.improvedmobs.config;

import com.flemmli97.tenshilib.api.config.IConfigListValue;
import com.flemmli97.tenshilib.common.utils.SearchUtils;
import com.google.common.collect.Lists;

import java.util.List;

public class EnchantCalcConf implements IConfigListValue<EnchantCalcConf> {

    private static final Value defaultVal = new Value(0, 0, 0);
    private final List<Value> vals = Lists.newArrayList();

    @Override
    public EnchantCalcConf readFromString(List<String> ss) {
        this.vals.clear();
        for (String s : ss) {
            String[] parts = s.split("-");
            if (parts.length != 3)
                continue;
            this.vals.add(new Value(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        }
        this.vals.sort(null);
        return this;
    }

    @Override
    public List<String> writeToString() {
        List<String> list = Lists.newArrayList();
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

        Value(int diff, int min, int max) {
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
