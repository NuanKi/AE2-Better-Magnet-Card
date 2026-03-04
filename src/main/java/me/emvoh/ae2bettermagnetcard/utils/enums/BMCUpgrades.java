package me.emvoh.ae2bettermagnetcard.utils.enums;

import appeng.api.definitions.IItemDefinition;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public enum BMCUpgrades {
    /**
     * Diamond Tier Upgrades.
     */
    RANGE(1),
    ADVANCED_RANGE(1);

    private final int tier;
    private final Map<ItemStack, Integer> supportedMax = new HashMap<>();

    BMCUpgrades(final int tier) {
        this.tier = tier;
    }

    /**
     * @return list of Items/Blocks that support this upgrade, and how many it supports.
     */
    public Map<ItemStack, Integer> getSupported() {
        return this.supportedMax;
    }

    /**
     * Registers a specific amount of this upgrade into a specific machine
     *
     * @param item         machine in which this upgrade can be installed
     * @param maxSupported amount how many upgrades can be installed
     */
    public void registerItem(final IItemDefinition item, final int maxSupported) {
        item.maybeStack(1).ifPresent(is -> this.registerItem(is, maxSupported));
    }

    /**
     * Registers a specific amount of this upgrade into a specific machine
     *
     * @param stack        machine in which this upgrade can be installed
     * @param maxSupported amount how many upgrades can be installed
     */
    public void registerItem(final ItemStack stack, final int maxSupported) {
        if (stack != null) {
            this.supportedMax.put(stack, maxSupported);
        }
    }

    public int getTier() {
        return this.tier;
    }
}
