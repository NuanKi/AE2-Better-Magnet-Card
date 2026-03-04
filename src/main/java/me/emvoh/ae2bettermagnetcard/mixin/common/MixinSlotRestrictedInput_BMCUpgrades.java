package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotRestrictedInput;
import me.emvoh.ae2bettermagnetcard.mixin.accessors.AccessorAppEngSlot;
import me.emvoh.ae2bettermagnetcard.api.IBMCUpgradeModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = SlotRestrictedInput.class, remap = false)
public abstract class MixinSlotRestrictedInput_BMCUpgrades {

    @Shadow
    @Final
    private SlotRestrictedInput.PlacableItemType which;

    @Shadow
    private boolean allowEdit;

    @Inject(method = {"isItemValid(Lnet/minecraft/item/ItemStack;)Z",
            "func_75214_a(Lnet/minecraft/item/ItemStack;)Z"
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private void ae2bmc$allowBmcUpgradeInUpgradeSlots(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) return;
        if (!(stack.getItem() instanceof IBMCUpgradeModule)) return;

        if (this.which != SlotRestrictedInput.PlacableItemType.UPGRADES) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        final SlotRestrictedInput self = (SlotRestrictedInput) (Object) this;
        final AppEngSlot as = (AppEngSlot) (Object) this;

        // 1) Container-level slot validation hook
        final AEBaseContainer container = ((AccessorAppEngSlot) (Object) this).ae2bmc$getMyContainer();
        if (container != null && !container.isValidForSlot(self, stack)) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        // 2) Reject AIR
        if (stack.getItem() == Items.AIR) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        // 3) Slot enabled
        if (!as.isSlotEnabled()) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        // 4) Respect allowEdit
        if (!this.allowEdit) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        // 5) Delegate to underlying handler validation
        final IItemHandler handler = ((AccessorAppEngSlot) (Object) this).ae2bmc$getItemHandler();
        final int idx = ((AccessorAppEngSlot) (Object) this).ae2bmc$getIndex();

        final boolean ok = handler != null && handler.isItemValid(idx, stack);

        cir.setReturnValue(ok);
        cir.cancel();
    }
}