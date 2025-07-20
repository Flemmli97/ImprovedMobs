package io.github.flemmli97.improvedmobs.common.config;

import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.common.config.values.BreakableBlocks;
import io.github.flemmli97.improvedmobs.common.config.values.DifficultyConfig;
import io.github.flemmli97.improvedmobs.common.config.values.EnchantCalcConf;
import io.github.flemmli97.improvedmobs.common.config.values.EntityFeatureConfig;
import io.github.flemmli97.improvedmobs.common.config.values.EntityItemConfig;
import io.github.flemmli97.improvedmobs.common.config.values.ExpressionConfig;
import io.github.flemmli97.improvedmobs.common.config.values.Pos2iConfig;
import io.github.flemmli97.improvedmobs.common.config.values.TargetMapConfig;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Config {

    public static class ClientConfig {

        public static int guiX = 5;
        public static int guiY = 5;
        public static ChatFormatting color = ChatFormatting.DARK_PURPLE;
        public static float scale = 1;
        public static boolean showDifficulty = true;
        public static DifficultyBarLocation location = DifficultyBarLocation.TOPLEFT;

        public static boolean showDifficultyServerSync;
    }

    public static class CommonConfig {

        // General
        public static boolean enableDifficultyScaling = true;
        public static int difficultyDelay;
        public static boolean ignoreSpawner;
        public static DifficultyConfig difficultyIncrease = new DifficultyConfig(DifficultyConfig.Value.of(0, 0.1f), DifficultyConfig.Value.of(250, 0));
        public static boolean ignorePlayers;
        public static boolean considerTimeskip = true;
        public static boolean friendlyFire;
        public static DifficultyType difficultyType = DifficultyType.PLAYERMEAN;
        public static Pos2iConfig centerPos = new Pos2iConfig();
        public static List<String> petArmorBlackList = new ArrayList<>();
        public static boolean petWhiteList;

        // Feature config
        public static Set<DifficultyFeatures> featureBlacklist = EnumSet.noneOf(DifficultyFeatures.class);
        public static EntityFeatureConfig entityBlacklist = new EntityFeatureConfig();
        public static Set<DifficultyFeatures> featureWhitelist = EnumSet.noneOf(DifficultyFeatures.class);

        // Integration
        public static IntegrationType vanillaClamped = IntegrationType.OFF;
        public static float vanillaClampedMax = 250;
        public static IntegrationType useScalingHealthMod = IntegrationType.ON;
        public static IntegrationType usePlayerEXMod = IntegrationType.ON;
        public static float playerEXScale = 1;
        public static IntegrationType useLevelZMod = IntegrationType.ON;
        public static float levelZScale = 1;

        // AI
        public static BreakableBlocks breakableBlocks = new BreakableBlocks("#c:glass_blocks", "#c:glass_panes", "#minecraft:fence_gates", "#c:fence_gates", "#minecraft:wooden_doors");
        public static boolean breakingAsBlacklist;
        public static boolean useBlockBreakSound = true;
        public static ExpressionConfig breakerChance = new ExpressionConfig("0.3");
        public static float difficultyBreak;
        public static int breakerInitCooldown = 120;
        public static int breakerCooldown = 20;
        public static boolean ignoreHarvestLevel;
        public static int restoreDelay;
        public static boolean idleBreak;
        public static float breakerSightIgnore = 1;
        public static ExpressionConfig breakSpeed = new ExpressionConfig("1");
        public static ExpressionConfig stealerChance = new ExpressionConfig("1");
        public static float difficultySteal;
        public static List<String> blackListedContainerBlocks = new ArrayList<>();
        public static boolean breakBlockEntities = true;
        public static List<WeightedItem> breakingItem = new ArrayList<>();
        public static ExpressionConfig neutralAggressiv = new ExpressionConfig("0.05");
        public static ExpressionConfig guardianAIChance = new ExpressionConfig("0.5");
        public static ExpressionConfig flyAIChance = new ExpressionConfig("0.5");
        public static boolean tntBlockDestruction;
        public static ExpressionConfig ignoreSightChance = new ExpressionConfig("0.5");
        public static TargetMapConfig autoTargets = new TargetMapConfig();

        // Equipment
        public static List<String> equipmentModBlacklist = new ArrayList<>();
        public static boolean equipmentModWhitelist;
        public static List<String> itemuseBlacklist = new ArrayList<>(List.of("bigbrain:buckler"));
        public static boolean itemuseWhitelist;
        public static EntityItemConfig entityItemConfig = new EntityItemConfig()
                .add(ResourceLocation.parse("skeleton"), "BOW")
                .add(ResourceLocation.parse("wither_skeleton"), "BOW")
                .add(ResourceLocation.parse("stray"), "BOW")
                .add(ResourceLocation.parse("illusioner"), "BOW")
                .add(ResourceLocation.parse("drowned"), "TRIDENT")
                .add(ResourceLocation.parse("piglin"), "CROSSBOW")
                .add(ResourceLocation.parse("pillager"), "CROSSBOW")
                .add(ResourceLocation.parse("snow_golem"), "minecraft:snowball");

        public static ExpressionConfig equipmentChance = new ExpressionConfig("0.1 + min(difficulty * 0.8 / 250, 0.8)");
        public static ExpressionConfig additionalEquipmentChance = new ExpressionConfig("0.3 + min(difficulty * 0.6 / 250, 0.6)");
        public static ExpressionConfig randomTrimChance = new ExpressionConfig("0.05 + min(difficulty * 0.2 / 250, 0.2)");
        public static ExpressionConfig mainHandChance = new ExpressionConfig("0.1 + min(difficulty * 0.3 / 250, 0.3)");
        public static ExpressionConfig offHandChance = new ExpressionConfig("0.1 + min(difficulty * 0.3 / 250, 0.3)");
        public static ExpressionConfig dropChance = new ExpressionConfig("0");
        public static ExpressionConfig enchantChance = new ExpressionConfig("0.2 + min(difficulty * 0.6 / 250, 0.6)");
        public static EnchantCalcConf enchantCalc = new EnchantCalcConf(new EnchantCalcConf.Value(0, 5, 10),
                new EnchantCalcConf.Value(25, 5, 15),
                new EnchantCalcConf.Value(50, 10, 17),
                new EnchantCalcConf.Value(100, 15, 25),
                new EnchantCalcConf.Value(200, 20, 30),
                new EnchantCalcConf.Value(250, 30, 35));
        public static List<String> enchantBlacklist = new ArrayList<>();
        public static boolean enchantWhitelist;

        public static ItemStack getRandomBreakingItem(RandomSource rand) {
            int total = WeightedRandom.getTotalWeight(breakingItem);
            if (breakingItem.isEmpty() || total <= 0)
                return ItemStack.EMPTY;
            return WeightedRandom.getRandomItem(rand, breakingItem, total).map(WeightedItem::getStack).orElse(ItemStack.EMPTY);
        }
    }

    public static VariableMap create(LivingEntity entity, float difficulty) {
        return apply(new VariableMap(), entity, difficulty);
    }

    public static VariableMap apply(VariableMap variables, LivingEntity entity, float difficulty) {
        double distSpawn = entity.blockPosition().distSqr(entity.level().getSharedSpawnPos());
        double distOrigin = entity.blockPosition().distSqr(BlockPos.ZERO);
        return variables.withRandom(entity.getRandom())
                .setVariable("difficulty", difficulty)
                .setVariable("distance_spawn", distSpawn)
                .setVariable("distance_origin", distOrigin);
    }

    public static class WeightedItem implements WeightedEntry {

        private final LazyItem item;
        private final int weight;

        public WeightedItem(String item, int weight) {
            this.weight = weight;
            this.item = new LazyItem(item);
        }

        public ItemStack getStack() {
            return this.item.getStack();
        }

        @Override
        public Weight getWeight() {
            return Weight.of(this.weight);
        }
    }

    private static class LazyItem {

        private final String config;
        private Item item;

        public LazyItem(String item) {
            this.config = item;
        }

        public ItemStack getStack() {
            if (this.item == null)
                this.item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(this.config));
            return new ItemStack(this.item);
        }
    }

    public enum DifficultyType {
        GLOBAL(true),
        PLAYERMAX(true),
        PLAYERMEAN(true),
        PLAYERSUM(true),
        DISTANCE(false),
        DISTANCESPAWN(false);

        public final boolean increaseDifficulty;

        DifficultyType(boolean increaseDifficulty) {
            this.increaseDifficulty = increaseDifficulty;
        }
    }

    public enum DifficultyBarLocation {
        TOPRIGHT,
        TOPLEFT,
        BOTTOMRIGHT,
        BOTTOMLEFT
    }

    public enum IntegrationType {
        OFF,
        ON,
        ADD;

        public boolean enabled() {
            return this != OFF;
        }
    }
}
