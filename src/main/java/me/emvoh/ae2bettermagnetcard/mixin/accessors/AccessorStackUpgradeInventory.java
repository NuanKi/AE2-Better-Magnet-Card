package me.emvoh.ae2bettermagnetcard.mixin.accessors;

import appeng.parts.automation.StackUpgradeInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StackUpgradeInventory.class, remap = false)
public interface AccessorStackUpgradeInventory {
    @Accessor("stack")
    ItemStack ae2bmc$getHostStack();
}