package io.github.flemmli97.improvedmobs.common.utils;

import io.github.flemmli97.improvedmobs.mixinhelper.IEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class EntityFlags {

    public static final String TAG_ID = "IMFlags";
    public static final String SERVER_ENTITY_TAG_ID = "ServerSideEntityID";

    public boolean ladderClimber;

    public static EntityFlags get(Entity entity) {
        return ((IEntityData) entity).improvedMobs$getFlags();
    }

    public boolean modifyArmor, modifyHeldItems, modifyAttributes, enchantGear;

    public boolean isThrownEntity;

    public FlagType canBreakBlocks = FlagType.UNDEFINED;
    public FlagType canFly = FlagType.UNDEFINED;

    private int shieldCooldown;

    private final Map<ServerSideAttributes, Double> attributes = new HashMap<>();

    public ResourceLocation serverSideEntityID;

    public void disableShield() {
        this.shieldCooldown = 120;
    }

    public boolean isShieldDisabled() {
        return --this.shieldCooldown > 0;
    }

    public double getAttribute(ServerSideAttributes attribute) {
        return this.attributes.getOrDefault(attribute, attribute.defaultValue);
    }

    public void setAttribute(ServerSideAttributes attribute, double value) {
        this.attributes.put(attribute, value);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CanBreakBlocks", this.canBreakBlocks.ordinal());
        tag.putInt("CanFly", this.canFly.ordinal());
        tag.putBoolean("ModifiedArmor", this.modifyArmor);
        tag.putBoolean("ModifiedHeld", this.modifyHeldItems);
        tag.putBoolean("ModifiedAttributes", this.modifyAttributes);
        tag.putBoolean("GearEnchanted", this.enchantGear);
        tag.putBoolean("IsThrown", this.isThrownEntity);
        CompoundTag attributes = new CompoundTag();
        this.attributes.forEach((att, val) -> attributes.putDouble(att.name(), val));
        tag.put("Attributes", attributes);
        return tag;
    }

    public void load(CompoundTag nbt) {
        this.canBreakBlocks = FlagType.values()[nbt.getInt("CanBreakBlocks")];
        this.canFly = FlagType.values()[nbt.getInt("CanFly")];
        this.modifyArmor = nbt.getBoolean("ModifiedArmor");
        this.modifyHeldItems = nbt.getBoolean("ModifiedHeld");
        this.modifyAttributes = nbt.getBoolean("ModifiedAttributes");
        this.enchantGear = nbt.getBoolean("GearEnchanted");
        this.isThrownEntity = nbt.getBoolean("IsThrown");
        this.attributes.clear();
        CompoundTag attributes = nbt.getCompound("Attributes");
        attributes.getAllKeys().forEach(key -> this.attributes.put(ServerSideAttributes.valueOf(key), attributes.getDouble(key)));
        if (nbt.contains(SERVER_ENTITY_TAG_ID))
            this.serverSideEntityID = ResourceLocation.parse(nbt.getString(SERVER_ENTITY_TAG_ID));
    }

    public enum FlagType {
        UNDEFINED,
        TRUE,
        FALSE,
    }

    public enum ServerSideAttributes {
        MAGIC_RESISTANCE(0),
        PROJECTILE_DAMAGE_MULTIPLIER(1),
        EXPLOSION_DAMAGE_MULTIPLIER(1);

        public final double defaultValue;

        ServerSideAttributes(double defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
