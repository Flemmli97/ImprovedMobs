package io.github.flemmli97.improvedmobs.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DifficultyFeatures {

    ALL,
    REVERSE,
    ATTRIBUTES,
    ARMOR,
    HELDITEMS,
    BLOCKBREAK,
    USEITEM,
    LADDER,
    STEAL,
    GUARDIAN,
    FLYING,
    TARGETVILLAGER,
    NEUTRALAGGRO;

    public static List<DifficultyFeatures> toggable() {
        List<DifficultyFeatures> all = new ArrayList<>(Arrays.asList(DifficultyFeatures.values()));
        all.remove(DifficultyFeatures.ALL);
        all.remove(DifficultyFeatures.REVERSE);
        return all;
    }
}
