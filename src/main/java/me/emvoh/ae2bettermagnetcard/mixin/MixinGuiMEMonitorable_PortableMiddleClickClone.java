package me.emvoh.ae2bettermagnetcard.mixin;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.container.implementations.ContainerMEPortableTerminal;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = GuiMEMonitorable.class, remap = false)
public abstract class MixinGuiMEMonitorable_PortableMiddleClickClone {

    @Shadow(remap = true)
    protected Container inventorySlots;

    @Shadow(remap = true)
    protected abstract Slot getSlotUnderMouse();

    @Shadow(remap = true)
    protected abstract void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type);

    @Inject(method = {"mouseClicked(III)V",          // dev
            "func_73864_a(III)V"           // obf client (SRG)
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private void ae2bettermagnetcard$portableTerminalMiddleClickClone(final int xCoord, final int yCoord, final int btn, final CallbackInfo ci) throws IOException {
        if (btn != 2) {
            return;
        }

        if (!(this.inventorySlots instanceof ContainerMEPortableTerminal)) {
            return;
        }

        final Slot slot = this.getSlotUnderMouse();
        if (slot == null) {
            return;
        }

        this.handleMouseClick(slot, slot.slotNumber, btn, ClickType.CLONE);
        ci.cancel();
    }
}