package me.emvoh.ae2bettermagnetcard.utils.enums;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum BMCUpgradeItemType {
    RANGE(0, BMCUpgrades.RANGE, 2, null),
    ADVANCED_RANGE(1, BMCUpgrades.ADVANCED_RANGE, 3, null),

    INVALID_TYPE(-1, null, 1, null);

    private final int meta;
    private final BMCUpgrades upgrade;
    private final int rangeMultiplier;
    private final String oreName; // optional

    BMCUpgradeItemType(int meta, BMCUpgrades upgrade, int rangeMultiplier, String oreName) {
        this.meta = meta;
        this.upgrade = upgrade;
        this.rangeMultiplier = rangeMultiplier;
        this.oreName = oreName;
    }

    public int getMeta() {
        return meta;
    }

    public BMCUpgrades getUpgrade() {
        return upgrade;
    }

    public int getRangeMultiplier() {
        return rangeMultiplier;
    }

    public String getOreName() {
        return oreName;
    }

    public ItemStack stack(Item item, int amount) {
        return new ItemStack(item, amount, this.meta);
    }

    public static BMCUpgradeItemType fromMeta(int meta) {
        for (BMCUpgradeItemType t : values()) {
            if (t.meta == meta) return t;
        }
        return INVALID_TYPE;
    }
}