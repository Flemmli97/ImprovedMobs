package io.github.flemmli97.improvedmobs.config;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

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

        //General
        public static boolean enableDifficultyScaling = true;
        public static int difficultyDelay;
        public static boolean ignoreSpawner;
        public static DifficultyConfig increaseHandler = new DifficultyConfig(List.of(Pair.of(0f, DifficultyConfig.Zone.of(0.1f)), Pair.of(250f, DifficultyConfig.Zone.of(0))));
        public static boolean ignorePlayers;
        public static boolean shouldPunishTimeSkip = true;
        public static boolean friendlyFire;
        public static List<String> petArmorBlackList = new ArrayList<>();
        public static boolean petWhiteList;
        public static boolean doIMDifficulty = true;
        public static DifficultyType difficultyType = DifficultyType.GLOBAL;
        public static NoHeightBlockPosConfig centerPos = new NoHeightBlockPosConfig();

        //Black-WhiteList
        public static EntityModifyFlagConfig entityBlacklist = new EntityModifyFlagConfig();
        public static List<String> flagBlacklist = new ArrayList<>();
        public static boolean mobAttributeWhitelist;
        public static boolean armorMobWhitelist;
        public static boolean heldMobWhitelist;
        public static boolean mobListBreakWhitelist;
        public static boolean mobListUseWhitelist;
        public static boolean mobListLadderWhitelist;
        public static boolean mobListStealWhitelist;
        public static boolean mobListBoatWhitelist;
        public static boolean mobListFlyWhitelist;
        public static boolean targetVillagerWhitelist;
        public static boolean neutralAggroWhitelist;

        //Integration
        public static boolean useScalingHealthMod = true;
        public static boolean usePlayerEXMod = true;
        public static boolean useLevelZMod = true;
        public static boolean varySizebyPehkui;
        public static float sizeMax = 0.5f;
        public static float sizeMin = 2;

        //AI
        public static BreakableBlocks breakableBlocks = new BreakableBlocks("#c:glass_blocks", "#c:glass_panes", "#minecraft:fence_gates", "#c:fence_gates", "#minecraft:wooden_doors");
        public static boolean breakingAsBlacklist;
        public static boolean useBlockBreakSound;
        public static float breakerChance = 0.3f;
        public static int breakerInitCooldown = 120;
        public static int breakerCooldown = 20;
        public static int restoreDelay;
        public static boolean idleBreak;
        public static float stealerChance = 0.3f;
        public static List<String> blackListedContainerBlocks = new ArrayList<>();
        public static boolean breakTileEntities = true;
        public static List<WeightedItem> breakingItem = new ArrayList<>();
        public static float neutralAggressiv = 0.05f;
        public static MobClassMapConfig autoTargets = new MobClassMapConfig();
        public static float difficultyBreak;
        public static float difficultySteal;
        public static float guardianAIChance = 0.5f;
        public static float flyAIChance = 0.5f;

        //Equipment
        public static List<String> equipmentModBlacklist = new ArrayList<>();
        public static boolean equipmentModWhitelist;
        public static List<String> itemuseBlacklist = new ArrayList<>(List.of("bigbrain:buckler"));
        public static EntityItemConfig entityItemConfig = new EntityItemConfig()
                .add(new ResourceLocation("skeleton"), "BOW")
                .add(new ResourceLocation("wither_skeleton"), "BOW")
                .add(new ResourceLocation("stray"), "BOW")
                .add(new ResourceLocation("illusioner"), "BOW")
                .add(new ResourceLocation("drowned"), "TRIDENT")
                .add(new ResourceLocation("piglin"), "CROSSBOW")
                .add(new ResourceLocation("pillager"), "CROSSBOW")
                .add(new ResourceLocation("snow_golem"), "minecraft:snowball");
        public static boolean itemuseWhitelist;
        public static float baseEquipChance = 0.1f;
        public static float baseEquipChanceAdd = 0.3f;
        public static float diffEquipAdd = 0.3f;
        public static float baseWeaponChance = 0.5f;
        public static float diffWeaponChance = 0.3f;
        public static float baseEnchantChance = 0.2f;
        public static float diffEnchantAdd = 0.2f;
        public static EnchantCalcConf enchantCalc = new EnchantCalcConf(new EnchantCalcConf.Value(0, 5, 10),
                new EnchantCalcConf.Value(25, 5, 15),
                new EnchantCalcConf.Value(50, 10, 17),
                new EnchantCalcConf.Value(100, 15, 25),
                new EnchantCalcConf.Value(200, 20, 30),
                new EnchantCalcConf.Value(250, 30, 35));
        public static float baseItemChance = 0.5f;
        public static float diffItemChanceAdd = 0.2f;
        public static List<String> enchantBlacklist = new ArrayList<>();
        public static boolean enchantWhitelist;
        public static boolean shouldDropEquip;

        //Attributes
        public static double healthIncrease = 1;
        public static double healthMax = 5;
        public static double roundHP = 0.5;
        public static double damageIncrease = 1;
        public static double damageMax = 3;
        public static double speedIncrease = 1;
        public static double speedMax = 0.1;
        public static double knockbackIncrease = 1;
        public static double knockbackMax = 0.5;
        public static float magicResIncrease = 1;
        public static float magicResMax = 0.4f;
        public static float projectileIncrease = 1;
        public static float projectileMax = 2;
        public static float explosionIncrease = 1;
        public static float explosionMax = 1.75f;

        public static ItemStack getRandomBreakingItem(RandomSource rand) {
            int total = WeightedRandom.getTotalWeight(breakingItem);
            if (breakingItem.isEmpty() || total <= 0)
                return ItemStack.EMPTY;
            return WeightedRandom.getRandomItem(rand, breakingItem, total).map(WeightedItem::getStack).orElse(ItemStack.EMPTY);
        }
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
                this.item = BuiltInRegistries.ITEM.get(new ResourceLocation(this.config));
            return new ItemStack(this.item);
        }
    }

    public enum DifficultyType {
        GLOBAL(true),
        PLAYERMAX(true),
        PLAYERMEAN(true),
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
}
