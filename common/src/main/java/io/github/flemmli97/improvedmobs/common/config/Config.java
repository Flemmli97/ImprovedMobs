package io.github.flemmli97.improvedmobs.common.config;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.improvedmobs.api.DifficultyFeatures;
import io.github.flemmli97.improvedmobs.common.config.values.BreakableBlocks;
import io.github.flemmli97.improvedmobs.common.config.values.DifficultyExpressionConfig;
import io.github.flemmli97.improvedmobs.common.config.values.EntityFeatureConfig;
import io.github.flemmli97.improvedmobs.common.config.values.EntityItemConfig;
import io.github.flemmli97.improvedmobs.common.config.values.ExpressionConfig;
import io.github.flemmli97.improvedmobs.common.config.values.Pos2iConfig;
import io.github.flemmli97.improvedmobs.common.config.values.StepExpressionConfig;
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
import net.minecraft.world.phys.Vec3;

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
        public static DifficultyExpressionConfig difficultyIncrease = new DifficultyExpressionConfig(
                new DifficultyExpressionConfig.UnresolvedValue(0, "difficulty + 0.1"),
                new DifficultyExpressionConfig.UnresolvedValue(250, "difficulty"));
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
        public static IntegrationType usePowerScaleMod = IntegrationType.ON;
        public static IntegrationType usePlayerEXMod = IntegrationType.ON;
        public static float playerEXScale = 1;
        public static IntegrationType useLevelZMod = IntegrationType.ON;
        public static float levelZScale = 1;
        public static IntegrationType useRunecraftoryMod = IntegrationType.ON;
        public static float runecraftoryScale = 1;

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
        public static EntityItemConfig entityItemConfig = new EntityItemConfig(
                Pair.of("minecraft:skeleton", "BOW"),
                Pair.of("minecraft:wither_skeleton", "BOW"),
                Pair.of("minecraft:stray", "BOW"),
                Pair.of("minecraft:illusioner", "BOW"),
                Pair.of("minecraft:drowned", "TRIDENT"),
                Pair.of("minecraft:piglin", "CROSSBOW"),
                Pair.of("minecraft:pillager", "CROSSBOW"),
                Pair.of("minecraft:snow_golem", "minecraft:snowball")
        );

        public static ExpressionConfig equipmentChance = new ExpressionConfig("0.1 + min(difficulty * 0.8 / 250, 0.8)");
        public static ExpressionConfig additionalEquipmentChance = new ExpressionConfig("0.3 + min(difficulty * 0.6 / 250, 0.6)");
        public static ExpressionConfig randomTrimChance = new ExpressionConfig("0.05 + min(difficulty * 0.2 / 250, 0.2)");
        public static ExpressionConfig mainHandChance = new ExpressionConfig("0.1 + min(difficulty * 0.3 / 250, 0.3)");
        public static ExpressionConfig offHandChance = new ExpressionConfig("0.1 + min(difficulty * 0.3 / 250, 0.3)");
        public static ExpressionConfig dropChance = new ExpressionConfig("0");
        public static ExpressionConfig enchantChance = new ExpressionConfig("0.2 + min(difficulty * 0.6 / 250, 0.6)");
        public static StepExpressionConfig enchantCalc = new StepExpressionConfig(StepExpressionConfig.ENCHANT_DEFAULT,
                new StepExpressionConfig.Value(0, "random_integer(1, 10)"),
                new StepExpressionConfig.Value(25, "random_integer(5, 15)"),
                new StepExpressionConfig.Value(50, "random_integer(10, 17)"),
                new StepExpressionConfig.Value(100, "random_integer(15, 25)"),
                new StepExpressionConfig.Value(200, "random_integer(20, 30)"),
                new StepExpressionConfig.Value(250, "random_integer(30, 35)"));
        public static List<String> enchantBlacklist = new ArrayList<>();
        public static boolean enchantWhitelist;

        public static ItemStack getRandomBreakingItem(RandomSource rand) {
            int total = WeightedRandom.getTotalWeight(breakingItem);
            if (breakingItem.isEmpty() || total <= 0)
                return ItemStack.EMPTY;
            return WeightedRandom.getRandomItem(rand, breakingItem, total).map(WeightedItem::getStack).orElse(ItemStack.EMPTY);
        }
    }

    public static VariableMap create(LivingEntity entity, double difficulty) {
        return apply(new VariableMap(), entity, difficulty);
    }

    public static VariableMap apply(VariableMap variables, LivingEntity entity, double difficulty) {
        return apply(variables, entity.getRandom(), entity.level().getSharedSpawnPos(), entity.position(), difficulty);
    }

    public static VariableMap apply(VariableMap variables, RandomSource random, BlockPos spawn, Vec3 pos, double difficulty) {
        double distSpawn = Math.sqrt(pos.distanceToSqr(spawn.getX() + 0.5, pos.y(), spawn.getZ() + 0.5));
        double distOrigin = Math.sqrt(pos.distanceToSqr(0.5, pos.y(), 0.5));
        double distCenter = Math.sqrt(pos.distanceToSqr(Config.CommonConfig.centerPos.getPos().x() + 0.5, pos.y(), Config.CommonConfig.centerPos.getPos().z() + 0.5));
        return variables.withRandom(random)
                .setVariable("difficulty", difficulty)
                .setVariable("distance_spawn", distSpawn)
                .setVariable("distance_origin", distOrigin)
                .setVariable("distance_center", distCenter);
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
