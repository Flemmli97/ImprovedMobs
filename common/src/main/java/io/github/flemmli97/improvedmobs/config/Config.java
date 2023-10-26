package io.github.flemmli97.improvedmobs.config;

import io.github.flemmli97.tenshilib.api.config.ItemWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static class ClientConfig {

        public static int guiX;
        public static int guiY;
        public static ChatFormatting color = ChatFormatting.DARK_PURPLE;
        public static float scale = 1;
        public static boolean showDifficulty;
        public static DifficultyBarLocation location = DifficultyBarLocation.TOPLEFT;

        public static boolean showDifficultyServerSync;

    }

    public static class CommonConfig {

        //General
        public static boolean enableDifficultyScaling;
        public static int difficultyDelay;
        public static DifficultyConfig increaseHandler = new DifficultyConfig();
        public static boolean ignorePlayers;
        public static boolean shouldPunishTimeSkip;
        public static boolean friendlyFire;
        public static List<String> petArmorBlackList;
        public static boolean petWhiteList;
        public static boolean doIMDifficulty;
        public static DifficultyType difficultyType = DifficultyType.GLOBAL;
        public static NoHeightBlockPosConfig centerPos = new NoHeightBlockPosConfig();

        //Black-WhiteList
        public static EntityModifyFlagConfig entityBlacklist = new EntityModifyFlagConfig();
        public static List<String> flagBlacklist;
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
        public static float sizeMax;
        public static float sizeMin;

        //AI
        public static BreakableBlocks breakableBlocks = new BreakableBlocks();
        public static boolean breakingAsBlacklist;
        public static boolean useBlockBreakSound;
        public static float breakerChance;
        public static int breakerInitCooldown;
        public static int breakerCooldown;
        public static boolean idleBreak;
        public static float stealerChance;
        public static List<String> blackListedContainerBlocks = new ArrayList<>();
        public static boolean breakTileEntities;
        public static List<WeightedItem> breakingItem = new ArrayList<>();
        public static float neutralAggressiv;
        public static MobClassMapConfig autoTargets = new MobClassMapConfig();
        public static float difficultyBreak;
        public static float difficultySteal;
        public static float guardianAIChance;
        public static float flyAIChance;

        //Equipment
        public static List<String> equipmentModBlacklist;
        public static boolean equipmentModWhitelist;
        public static List<String> itemuseBlacklist;
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
        public static float baseEquipChance;
        public static float baseEquipChanceAdd;
        public static float diffEquipAdd;
        public static float baseWeaponChance;
        public static float diffWeaponChance;
        public static float baseEnchantChance;
        public static float diffEnchantAdd;
        public static EnchantCalcConf enchantCalc = new EnchantCalcConf();
        public static float baseItemChance;
        public static float diffItemChanceAdd;
        public static List<String> enchantBlacklist;
        public static boolean enchantWhitelist;
        public static boolean shouldDropEquip;

        //Attributes
        public static double healthIncrease;
        public static double healthMax;
        public static double roundHP;
        public static double damageIncrease;
        public static double damageMax;
        public static double speedIncrease;
        public static double speedMax;
        public static double knockbackIncrease;
        public static double knockbackMax;
        public static float magicResIncrease;
        public static float magicResMax;
        public static float projectileIncrease;
        public static float projectileMax;
        public static float explosionIncrease;
        public static float explosionMax;

        public static ItemStack getRandomBreakingItem(RandomSource rand) {
            int total = WeightedRandom.getTotalWeight(breakingItem);
            if (breakingItem.size() == 0 || total <= 0)
                return ItemStack.EMPTY;
            return WeightedRandom.getRandomItem(rand, breakingItem, total).map(WeightedItem::getItem).map(ItemWrapper::getStack).orElse(ItemStack.EMPTY);
        }
    }

    public static class WeightedItem implements WeightedEntry {

        private final ItemWrapper item;
        private final int weight;

        public WeightedItem(ItemWrapper item, int weight) {
            this.weight = weight;
            this.item = item;
        }

        public ItemWrapper getItem() {
            return this.item;
        }

        @Override
        public Weight getWeight() {
            return Weight.of(this.weight);
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
