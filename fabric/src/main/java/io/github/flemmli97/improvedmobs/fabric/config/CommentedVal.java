package io.github.flemmli97.improvedmobs.fabric.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommentedVal<T> {

    protected List<String> comments;
    protected T input;

    public CommentedVal(List<String> comments, T input) {
        this.comments = comments;
        this.input = input;
    }

    public T get() {
        return this.input;
    }

    public static class Builder {

        private List<String> comments;

        public Builder comment(String... comments) {
            List<String> list = new ArrayList<>();
            ;
            for (String s : comments) {
                String[] split = s.split("\n");
                list.addAll(Arrays.asList(split));
            }
            this.comments = list;
            return this;
        }

        public <T> CommentedVal<T> define(String name, T value) {
            CommentedVal<T> commentedVal = new CommentedVal<>(this.comments, value);
            this.comments = null;
            return commentedVal;
        }

        public static Builder create() {
            return new Builder();
        }
    }
}
