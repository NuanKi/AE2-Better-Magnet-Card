package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlacableItemType;
import appeng.util.Platform;
import me.emvoh.ae2bettermagnetcard.api.IBMCUpgradeModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = AEBaseContainer.class, remap = false)
public abstract class MixinAEBaseContainer_ShiftClickBMCUpgrades {

    @Inject(method = {"transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;",
            "func_82846_b(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;"
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private void ae2bmc$shiftClickBmcUpgrades(EntityPlayer player, int idx, CallbackInfoReturnable<ItemStack> cir) {
        if (Platform.isClient()) {
            return;
        }

        final Container container = (Container) (Object) this;
        final List<Slot> slots = container.inventorySlots;

        if (idx < 0 || idx >= slots.size()) {
            return;
        }

        final Slot rawClickSlot = slots.get(idx);
        if (!(rawClickSlot instanceof AppEngSlot)) {
            return;
        }

        final AppEngSlot clickSlot = (AppEngSlot) rawClickSlot;

        if (!clickSlot.isPlayerSide() || !clickSlot.getHasStack()) {
            return;
        }

        final ItemStack clicked = clickSlot.getStack();
        if (clicked.isEmpty() || !(clicked.getItem() instanceof IBMCUpgradeModule)) {
            return;
        }

        final ItemStack toInsert = clicked.copy();
        toInsert.setCount(1);

        for (final Slot s : slots) {
            if (!(s instanceof SlotRestrictedInput)) {
                continue;
            }

            final SlotRestrictedInput dst = (SlotRestrictedInput) s;

            if (dst.isPlayerSide()) {
                continue;
            }

            if (dst.getPlaceableItemType() != PlacableItemType.UPGRADES) {
                continue;
            }

            if (!dst.isSlotEnabled()) {
                continue;
            }

            if (dst.getHasStack()) {
                continue;
            }

            if (!dst.isItemValid(toInsert)) {
                continue;
            }

            dst.putStack(toInsert);
            dst.onSlotChanged();

            clicked.shrink(1);
            clickSlot.putStack(clicked.isEmpty() ? ItemStack.EMPTY : clicked);
            clickSlot.onSlotChanged();

            ((AEBaseContainer) (Object) this).detectAndSendChanges();

            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
            return;
        }
    }
}