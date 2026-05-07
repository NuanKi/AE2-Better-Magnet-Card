package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.api.config.Upgrades;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.tools.powered.ToolWirelessTerminal;
import me.emvoh.ae2bettermagnetcard.mixin.accessors.AccessorCellConfig;
import me.emvoh.ae2bettermagnetcard.mixin.accessors.AccessorStackUpgradeInventory;
import me.emvoh.ae2bettermagnetcard.utils.MagnetCardFilters;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ToolWirelessTerminal.class, remap = false)
public abstract class MixinMagnetLogic_PickupFilter {

    @Redirect(
            method = "magnetLogic(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lappeng/items/contents/CellConfig;getSlots()I"),
            require = 1
    )
    private int ae2bmc$pickupFilterSlots(final CellConfig config) {
        final ItemStack magnetCard = ((AccessorCellConfig) (Object) config).ae2bmc$getHostStack();
        return MagnetCardFilters.getPickupFilterSlots(magnetCard, config);
    }

    @Redirect(
            method = "magnetLogic(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lappeng/items/contents/CellConfig;getStackInSlot(I)Lnet/minecraft/item/ItemStack;"),
            require = 1
    )
    private ItemStack ae2bmc$pickupFilterStack(final CellConfig config, final int slot) {
        final ItemStack magnetCard = ((AccessorCellConfig) (Object) config).ae2bmc$getHostStack();
        return MagnetCardFilters.getPickupFilterStack(magnetCard, config, slot);
    }

    @Redirect(
            method = "magnetLogic(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lappeng/items/contents/CellUpgrades;getInstalledUpgrades(Lappeng/api/config/Upgrades;)I", ordinal = 1),
            require = 1
    )
    private int ae2bmc$pickupFilterMode(final CellUpgrades upgrades, final Upgrades upgrade) {
        final ItemStack magnetCard = ((AccessorStackUpgradeInventory) (Object) upgrades).ae2bmc$getHostStack();
        return MagnetCardFilters.getPickupInverterValue(magnetCard, upgrades, upgrade);
    }
}
