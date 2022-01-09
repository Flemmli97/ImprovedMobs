package io.github.flemmli97.improvedmobs.utils;

import io.github.flemmli97.improvedmobs.mixinhelper.IEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class EntityFlags {

    public boolean ladderClimber;

    public static EntityFlags get(Entity entity) {
        return ((IEntityData) entity).getFlags();
    }

    public boolean modifyArmor, modifyHeldItems, modifyAttributes, enchantGear;

    public boolean isThrownEntity, rideSummon;

    public FlagType canBreakBlocks = FlagType.UNDEF;

    private int shieldCooldown;

    public float magicReg, projMult;

    public void disableShield() {
        this.shieldCooldown = 120;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CanBreakBlocks", this.canBreakBlocks.ordinal());
        tag.putBoolean("ModifiedArmor", this.modifyArmor);
        tag.putBoolean("ModifiedHeld", this.modifyHeldItems);
        tag.putBoolean("ModifiedAttributes", this.modifyAttributes);
        tag.putBoolean("GearEnchanted", this.enchantGear);
        tag.putBoolean("IsThrown", this.isThrownEntity);
        tag.putBoolean("IsRideSummoned", this.rideSummon);
        tag.putFloat("MagicRes", this.magicReg);
        tag.putFloat("ProjBoost", this.projMult);
        return tag;
    }

    public void load(CompoundTag nbt) {
        this.canBreakBlocks = FlagType.values()[nbt.getInt("CanBreakBlocks")];
        this.modifyArmor = nbt.getBoolean("ModifiedArmor");
        this.modifyHeldItems = nbt.getBoolean("ModifiedHeld");
        this.modifyAttributes = nbt.getBoolean("ModifiedAttributes");
        this.enchantGear = nbt.getBoolean("GearEnchanted");
        this.isThrownEntity = nbt.getBoolean("IsThrown");
        this.rideSummon = nbt.getBoolean("IsRideSummoned");
        this.magicReg = nbt.getFloat("MagicRes");
        this.projMult = nbt.getFloat("ProjBoost");
    }

    public boolean isShieldDisabled() {
        return --this.shieldCooldown > 0;
    }

    public enum FlagType {
        UNDEF,
        TRUE,
        FALSE,
    }
}
