package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.api.AEApi;
import appeng.items.materials.ItemMaterial;
import appeng.items.materials.MaterialType;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;
import me.emvoh.ae2bettermagnetcard.api.IBMCUpgradeModule;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgrades;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ToolWirelessTerminal.class, remap = false)
public abstract class MixinMagnetLogic_RangeUpgrade {

    @ModifyArgs(method = "magnetLogic(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;<init>(DDDDDD)V"), require = 1)
    private void ae2bmc$expandAabbIfRangeUpgrade(Args args, ItemStack stack, net.minecraft.world.World worldIn, Entity entityIn) {
        final double base = 5.0;

        final int mult = getRangeMultiplierFromMagnetCard(stack);
        final double range = base * mult;

        args.set(0, entityIn.posX - range);
        args.set(1, entityIn.posY - range);
        args.set(2, entityIn.posZ - range);
        args.set(3, entityIn.posX + range);
        args.set(4, entityIn.posY + range);
        args.set(5, entityIn.posZ + range);
    }

    /**
     * Returns:
     * - 3 if ADVANCED_RANGE is installed in the Magnet Card
     * - 2 if RANGE is installed in the Magnet Card
     * - 1 otherwise
     */
    private static int getRangeMultiplierFromMagnetCard(ItemStack wirelessTerminal) {
        if (wirelessTerminal == null || wirelessTerminal.isEmpty()) return 1;

        final net.minecraft.nbt.NBTTagCompound upgradeNBT = Platform.openNbtData(wirelessTerminal).getCompoundTag("upgrades");
        final net.minecraftforge.items.ItemStackHandler termUpgrades = new net.minecraftforge.items.ItemStackHandler(0);
        termUpgrades.deserializeNBT(upgradeNBT);

        for (int s = 0; s < termUpgrades.getSlots(); s++) {
            final ItemStack maybeMagnetCard = termUpgrades.getStackInSlot(s);
            if (!AEApi.instance().definitions().materials().cardMagnet().isSameAs(maybeMagnetCard)) {
                continue;
            }

            final ItemMaterial im = (ItemMaterial) maybeMagnetCard.getItem();
            if (im.getTypeByStack(maybeMagnetCard) != MaterialType.CARD_MAGNET) continue;

            final IItemHandler magnetUpgrades = im.getUpgradesInventory(maybeMagnetCard);

            boolean hasRange = false;
            boolean hasAdvanced = false;

            for (int i = 0; i < magnetUpgrades.getSlots(); i++) {
                ItemStack up = magnetUpgrades.getStackInSlot(i);
                if (up.isEmpty()) continue;

                if (up.getItem() instanceof IBMCUpgradeModule) {
                    BMCUpgrades t = ((IBMCUpgradeModule) up.getItem()).getType(up);
                    if (t == BMCUpgrades.ADVANCED_RANGE) {
                        hasAdvanced = true;
                        break;
                    } else if (t == BMCUpgrades.RANGE) {
                        hasRange = true;
                    }
                }
            }

            if (hasAdvanced) return 3;
            if (hasRange) return 2;
            return 1;
        }

        return 1;
    }
}