package io.github.flemmli97.improvedmobs.common.config.values;

import io.github.flemmli97.tenshilib.common.utils.SearchUtils;

import java.util.ArrayList;
import java.util.List;

public class EnchantCalcConf {

    private static final Value DEFAULT_VALUE = new Value(0, new ExpressionConfig("0"));

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
            String[] parts = s.split(";");
            if (parts.length != 2)
                continue;
            list.add(new Value(Float.parseFloat(parts[0]), new ExpressionConfig(parts[1])));
        }
        list.sort(null);
        this.values.addAll(list);
    }

    public List<String> write() {
        List<String> list = new ArrayList<>();
        this.values.forEach(v -> list.add(v.write()));
        return list;
    }

    public record Value(float requiredDifficulty, ExpressionConfig expression) implements Comparable<Value> {

        public Value(float requiredDifficulty, String expression) {
            this(requiredDifficulty, new ExpressionConfig(expression));
        }

        public String write() {
            return String.format("%s;%s", this.requiredDifficulty, this.expression.write());
        }

        @Override
        public int compareTo(Value o) {
            return Float.compare(this.requiredDifficulty, o.requiredDifficulty);
        }
    }
}
