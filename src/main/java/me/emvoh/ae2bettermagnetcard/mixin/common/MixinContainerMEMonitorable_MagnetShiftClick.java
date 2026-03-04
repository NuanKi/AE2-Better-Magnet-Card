package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.api.AEApi;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = ContainerMEMonitorable.class, remap = false)
public abstract class MixinContainerMEMonitorable_MagnetShiftClick {

    @Inject(method = {"transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;", // dev
            "func_82846_b(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;"        // obf client (SRG)
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private void ae2bettermagnetcard$magnetShiftClick(final EntityPlayer p, final int idx, final CallbackInfoReturnable<ItemStack> cir) {
        if (Platform.isClient()) {
            return;
        }

        final Container self = (Container) (Object) this;

        if (idx < 0 || idx >= self.inventorySlots.size()) {
            return;
        }

        final Slot clickSlot0 = self.inventorySlots.get(idx);
        ItemStack itemStack0 = clickSlot0.getStack();

        final boolean isPlayerInventorySlot0 = clickSlot0 instanceof SlotPlayerInv || clickSlot0 instanceof SlotPlayerHotBar;
        if (isPlayerInventorySlot0 && !itemStack0.isEmpty() && AEApi.instance().definitions().materials().cardMagnet().isSameAs(itemStack0)) {

            for (final Slot dst : self.inventorySlots) {
                if (!(dst instanceof SlotRestrictedInput)) {
                    continue;
                }
                if (dst instanceof SlotPlayerInv || dst instanceof SlotPlayerHotBar) {
                    continue;
                }

                final SlotRestrictedInput sri = (SlotRestrictedInput) dst;
                if (sri.getPlaceableItemType() != SlotRestrictedInput.PlacableItemType.UPGRADES) {
                    continue;
                }

                if (!sri.getStack().isEmpty()) {
                    continue;
                }

                if (!sri.isItemValid(itemStack0)) {
                    continue;
                }

                final ItemStack toMove = itemStack0.copy();
                final int maxSize = Math.min(toMove.getMaxStackSize(), sri.getSlotStackLimit());
                if (toMove.getCount() > maxSize) {
                    toMove.setCount(maxSize);
                }

                sri.putStack(toMove);
                sri.onSlotChanged();

                itemStack0.shrink(toMove.getCount());
                clickSlot0.putStack(itemStack0.isEmpty() ? ItemStack.EMPTY : itemStack0);

                self.detectAndSendChanges();
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        }
    }
}