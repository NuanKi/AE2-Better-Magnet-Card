package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.api.AEApi;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import me.emvoh.ae2bettermagnetcard.mixin.accessors.AccessorStackUpgradeInventory;
import me.emvoh.ae2bettermagnetcard.api.IBMCUpgradeModule;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgrades;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(targets = "appeng.parts.automation.UpgradeInventory$UpgradeInvFilter", remap = false)
public abstract class MixinAE2UpgradeFilter_BMCUpgrades {

    @Shadow
    @Final
    private UpgradeInventory this$0; // outer instance

    @Inject(method = "allowInsert", at = @At("HEAD"), cancellable = true, require = 1)
    private void ae2bmc$allowInsertBmcUpgradeOnlyForMagnet(IItemHandler inv, int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) return;
        if (!(stack.getItem() instanceof IBMCUpgradeModule)) return;

        final BMCUpgrades inserting = ((IBMCUpgradeModule) stack.getItem()).getType(stack);
        if (inserting == null) {
            cir.setReturnValue(false);
            return;
        }

        if (!((Object) this$0 instanceof StackUpgradeInventory)) {
            cir.setReturnValue(false);
            return;
        }

        final ItemStack host = ((AccessorStackUpgradeInventory) (Object) this$0).ae2bmc$getHostStack();
        if (host == null || host.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        if (!AEApi.instance().definitions().materials().cardMagnet().isSameAs(host)) {
            cir.setReturnValue(false);
            return;
        }

        final int max = getMaxSupported(inserting.getSupported(), host);
        if (max <= 0) {
            cir.setReturnValue(false);
            return;
        }

        int installedSameType = 0;
        boolean hasRange = false;
        boolean hasAdvanced = false;

        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack ex = inv.getStackInSlot(i);
            if (ex.isEmpty()) continue;

            if (ex.getItem() instanceof IBMCUpgradeModule) {
                BMCUpgrades t = ((IBMCUpgradeModule) ex.getItem()).getType(ex);

                if (t == inserting) installedSameType++;

                if (t == BMCUpgrades.RANGE) hasRange = true;
                if (t == BMCUpgrades.ADVANCED_RANGE) hasAdvanced = true;
            }
        }

        if (inserting == BMCUpgrades.RANGE && hasAdvanced) {
            cir.setReturnValue(false);
            return;
        }
        if (inserting == BMCUpgrades.ADVANCED_RANGE && hasRange) {
            cir.setReturnValue(false);
            return;
        }

        cir.setReturnValue(installedSameType < max);
    }

    private static int getMaxSupported(final Map<ItemStack, Integer> supported, final ItemStack host) {
        for (final Map.Entry<ItemStack, Integer> e : supported.entrySet()) {
            if (ItemStack.areItemsEqual(e.getKey(), host)) {
                return e.getValue();
            }
        }
        return 0;
    }
}