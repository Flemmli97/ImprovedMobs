package io.github.flemmli97.improvedmobs.common.config.equipment;

import java.util.Collection;

public record ItemScore(double durability,
                        double damage, double armor, double armorToughness, double knockbackResistance,
                        double enchantmentValue, double utilityScore, double multiplier) {

    public static ScoreRange[] composite(Collection<ItemScore> itemScores) {
        ScoreRange min = null;
        ScoreRange max = null;
        for (ItemScore itemScore : itemScores) {
            if (min == null)
                min = new ScoreRange(itemScore, true);
            else
                min.update(itemScore);
            if (max == null)
                max = new ScoreRange(itemScore, false);
            else
                max.update(itemScore);
        }
        return new ScoreRange[]{min, max};
    }

    public static class ScoreRange {

        private final boolean min;
        private double durability, damage, armor, armorToughness, knockbackResistance, enchantmentValue, utilityScore;

        public ScoreRange(ItemScore score, boolean min) {
            this.min = min;
            this.durability = score.durability();
            this.damage = score.damage();
            this.armor = score.armor();
            this.armorToughness = score.armorToughness();
            this.knockbackResistance = score.knockbackResistance();
            this.enchantmentValue = score.enchantmentValue();
            this.utilityScore = score.utilityScore();
        }

        private void update(ItemScore score) {
            if (this.min) {
                this.durability = Math.min(this.damage, score.durability());
                this.damage = Math.min(this.damage, score.damage());
                this.armor = Math.min(this.armor, score.armor());
                this.armorToughness = Math.min(this.armorToughness, score.armorToughness());
                this.knockbackResistance = Math.min(this.knockbackResistance, score.knockbackResistance());
                this.enchantmentValue = Math.min(this.enchantmentValue, score.enchantmentValue());
                this.utilityScore = Math.min(this.utilityScore, score.utilityScore());
                return;
            }
            this.durability = Math.max(this.durability, score.durability());
            this.damage = Math.max(this.damage, score.damage());
            this.armor = Math.max(this.armor, score.armor());
            this.armorToughness = Math.max(this.armorToughness, score.armorToughness());
            this.knockbackResistance = Math.max(this.knockbackResistance, score.knockbackResistance());
            this.enchantmentValue = Math.max(this.enchantmentValue, score.enchantmentValue());
            this.utilityScore = Math.max(this.utilityScore, score.utilityScore());
        }

        public double durability() {
            return this.durability;
        }

        public double damage() {
            return this.damage;
        }

        public double armor() {
            return this.armor;
        }

        public double armorToughness() {
            return this.armorToughness;
        }

        public double knockbackResistance() {
            return this.knockbackResistance;
        }

        public double enchantmentValue() {
            return this.enchantmentValue;
        }

        public double utilityScore() {
            return this.utilityScore;
        }

        @Override
        public String toString() {
            return "ScoreRange{" +
                    "min=" + this.min +
                    ", durability=" + this.durability +
                    ", damage=" + this.damage +
                    ", armor=" + this.armor +
                    ", armorToughness=" + this.armorToughness +
                    ", knockbackResistance=" + this.knockbackResistance +
                    ", enchantmentValue=" + this.enchantmentValue +
                    ", utilityScore=" + this.utilityScore +
                    '}';
        }
    }
}
