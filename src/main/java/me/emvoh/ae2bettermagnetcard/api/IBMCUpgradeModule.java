package me.emvoh.ae2bettermagnetcard.api;

import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgrades;
import net.minecraft.item.ItemStack;

public interface IBMCUpgradeModule {
    BMCUpgrades getType(ItemStack var1);
}
