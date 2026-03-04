package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.items.contents.CellUpgrades;
import appeng.items.materials.ItemMaterial;
import appeng.items.materials.MaterialType;
import appeng.util.Platform;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemMaterial.class, remap = false)
public abstract class MixinItemMaterialMagnetCardCommon {

    @Inject(method = "getUpgradesInventory", at = @At("HEAD"), cancellable = true, require = 1)
    private void ae2bettermagnetcard$expandMagnetUpgradeSlots(ItemStack is, CallbackInfoReturnable<IItemHandler> cir) {
        if (is == null || is.isEmpty()) return;

        final ItemMaterial self = (ItemMaterial) (Object) this;
        if (self.getTypeByStack(is) != MaterialType.CARD_MAGNET) return;

        final NBTTagCompound root = Platform.openNbtData(is);
        if (root.hasKey("upgrades", Constants.NBT.TAG_COMPOUND)) {
            final NBTTagCompound upgradesTag = root.getCompoundTag("upgrades");
            if (upgradesTag.hasKey("Size", Constants.NBT.TAG_INT) && upgradesTag.getInteger("Size") < 3) {
                upgradesTag.setInteger("Size", 3);
                root.setTag("upgrades", upgradesTag);
            }
        }

        cir.setReturnValue(new CellUpgrades(is, 3));
    }
}