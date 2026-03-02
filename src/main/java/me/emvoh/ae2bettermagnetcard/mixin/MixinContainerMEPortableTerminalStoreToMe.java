package me.emvoh.ae2bettermagnetcard.mixin;

import appeng.api.AEApi;
import appeng.container.implementations.ContainerMEPortableTerminal;
import appeng.container.slot.SlotRestrictedInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = ContainerMEPortableTerminal.class, remap = false)
public abstract class MixinContainerMEPortableTerminalStoreToMe {

    @Shadow
    protected SlotRestrictedInput magnetSlot;

    @Inject(method = {"slotClick(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", // dev
            "func_184996_a(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;" // obf client (SRG)
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private void ae2bettermagnetcard$middleClickToggleStoreToME(final int slotId, final int dragType, final ClickType clickTypeIn, final EntityPlayer player, final CallbackInfoReturnable<ItemStack> cir) {
        if (clickTypeIn != ClickType.CLONE) {
            return;
        }

        if (this.magnetSlot == null || slotId < 0) {
            return;
        }

        final Slot clicked;
        try {
            clicked = ((Container) (Object) this).getSlot(slotId);
        } catch (final IndexOutOfBoundsException ignored) {
            return;
        }

        if (clicked != this.magnetSlot) {
            return;
        }

        final ItemStack itemStack = this.magnetSlot.getStack();
        if (itemStack.isEmpty()) {
            return;
        }

        if (!AEApi.instance().definitions().materials().cardMagnet().isSameAs(itemStack)) {
            return;
        }

        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }

        final boolean store = tag.hasKey("storeToME") && tag.getBoolean("storeToME");
        tag.setBoolean("storeToME", !store);

        itemStack.setTagCompound(tag);
        this.magnetSlot.onSlotChanged();

        cir.setReturnValue(ItemStack.EMPTY);
    }
}