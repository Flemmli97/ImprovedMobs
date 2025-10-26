package io.github.flemmli97.improvedmobs.common.config.values;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.improvedmobs.ImprovedMobs;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DifficultyExpressionConfig {

    public static final Value DIFFICULTY_DEFAULT = new Value(0, "difficulty");

    private final List<Value> values = new ArrayList<>();

    public DifficultyExpressionConfig(UnresolvedValue... values) {
        for (int i = 0; i < values.length; i++) {
            UnresolvedValue value = values[i];
            UnresolvedValue next = i + 1 < values.length ? values[i + 1] : null;
            this.values.add(new Value(new DifficultyRange(value.minDifficulty, next == null ? Double.MAX_VALUE : next.minDifficulty), value.expression()));
        }
    }

    public Pair<Integer, Value> get(double difficulty, int index) {
        if (index >= 0 && index < this.values.size()) {
            Value value = this.values.get(index);
            if (value.range().matches(difficulty)) {
                return Pair.of(index, value);
            }
        }
        for (int i = 0; i < this.values.size(); i++) {
            int idx = Math.floorMod(i + index, this.values.size());
            Value value = this.values.get(idx);
            if (value.range().matches(difficulty)) {
                return Pair.of(idx, value);
            }
        }
        return Pair.of(-1, DIFFICULTY_DEFAULT);
    }

    public void read(List<String> config) {
        try {
            List<Value> list = new ArrayList<>();
            for (int i = 0; i < config.size(); i++) {
                String[] parts = config.get(i).split(";");
                if (parts.length != 2)
                    continue;
                String next = i + 1 < config.size() ? config.get(i + 1) : null;
                list.add(new Value(new DifficultyRange(parts[0], next), new ExpressionConfig(parts[1])));
            }
            this.values.clear();
            this.values.addAll(list);
        } catch (IllegalStateException e) {
            ImprovedMobs.LOGGER.error("Unable to parse expression from config: {}", config, e);
        }
    }

    public List<String> write() {
        List<String> list = new ArrayList<>();
        this.values.forEach(v -> list.add(v.write()));
        return list;
    }

    public record Value(DifficultyRange range, ExpressionConfig expression) {

        public Value(double minDifficulty, String expression) {
            this(new DifficultyRange(minDifficulty + "", null), new ExpressionConfig(expression));
        }

        public String write() {
            return String.format("%s;%s", this.range.source, this.expression.write());
        }

        @Override
        public String toString() {
            return String.format("Value:[%s, %s]", this.range, this.expression.write());
        }
    }

    public record UnresolvedValue(double minDifficulty, ExpressionConfig expression) {

        public UnresolvedValue(double minDifficulty, String expression) {
            this(minDifficulty, new ExpressionConfig(expression));
        }
    }

    public static class DifficultyRange {

        private final String source;
        private final double start;
        private final double end;

        private DifficultyRange(String source, @Nullable String next) {
            this.source = source;
            if (source.contains("-")) {
                String[] vals = source.split("-");
                this.start = Double.parseDouble(vals[0]);
                this.end = Double.parseDouble(vals[1]);
            } else {
                this.start = Double.parseDouble(source);
                if (next == null) {
                    this.end = Double.MAX_VALUE;
                } else {
                    String[] parts = next.split(";");
                    if (parts.length != 2) {
                        this.end = Double.MAX_VALUE;
                    } else {
                        this.end = Double.parseDouble(parts[0].split("-")[0]);
                    }
                }
            }
        }

        private DifficultyRange(double min, double max) {
            this.source = String.valueOf(min);
            this.start = min;
            this.end = max;
        }

        public boolean matches(double difficulty) {
            if (this.start < this.end) {
                return this.start <= difficulty && difficulty < this.end;
            }
            return difficulty <= this.start && difficulty > this.end;
        }

        @Override
        public String toString() {
            return String.format("Range[%s, %s]", this.start, this.end);
        }
    }
}
