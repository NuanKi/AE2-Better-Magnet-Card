package me.emvoh.ae2bettermagnetcard.mixin.client;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerMEPortableTerminal;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.WirelessTerminalGuiObject;
import me.emvoh.ae2bettermagnetcard.Main;
import me.emvoh.ae2bettermagnetcard.client.gui.GuiMagnetFilterButton;
import me.emvoh.ae2bettermagnetcard.mixin.accessors.AccessorContainerMEPortableTerminal;
import me.emvoh.ae2bettermagnetcard.network.PacketOpenMagnetFilterGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.List;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = GuiMEMonitorable.class, priority = 1100, remap = false)
public abstract class MixinGuiMEMonitorable_MagnetFilterButton extends AEBaseMEGui {

    @Unique
    private static final int ae2bmc$MAGNET_FILTER_BUTTON = 880321;

    @Unique
    private static final String ae2bmc$WUT_BUTTON_CLASS = "com.circulation.ae2wut.client.TooltipButton";

    @Unique
    private GuiMagnetFilterButton ae2bmc$button;

    @Unique
    private WirelessTerminalGuiObject ae2bmc$obj;

    public MixinGuiMEMonitorable_MagnetFilterButton(final Container container) {
        super(container);
    }

    @Inject(method = {"<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lappeng/api/storage/ITerminalHost;Lappeng/container/implementations/ContainerMEMonitorable;)V"},
            at = {@At("TAIL")})
    public void ae2bmc$onInit(final InventoryPlayer inventoryPlayer, final ITerminalHost te, final ContainerMEMonitorable c, final CallbackInfo ci) {
        this.ae2bmc$obj = te instanceof WirelessTerminalGuiObject ? (WirelessTerminalGuiObject) te : null;
    }

    @Inject(method = {"initGui", "func_73866_w_"}, at = {@At("TAIL")})
    public void ae2bmc$initGui(final CallbackInfo ci) {
        ae2bmc$syncMagnetFilterButton();
    }

    @Inject(method = {"updateScreen", "func_73876_c"}, at = {@At("TAIL")})
    public void ae2bmc$updateScreen(final CallbackInfo ci) {
        ae2bmc$syncMagnetFilterButton();
    }

    @Inject(method = {"getJEIExclusionArea"}, at = {@At("RETURN")}, remap = false)
    public void ae2bmc$getJEIExclusionArea(final CallbackInfoReturnable<List<Rectangle>> cir) {
        if (this.ae2bmc$button != null && this.ae2bmc$button.visible) {
            cir.getReturnValue().add(new Rectangle(this.ae2bmc$button.x, this.ae2bmc$button.y,
                    this.ae2bmc$button.width, this.ae2bmc$button.height));
        }
    }

    @Inject(method = {
            "actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V",
            "func_146284_a(Lnet/minecraft/client/gui/GuiButton;)V"
    }, at = {@At("HEAD")}, cancellable = true)
    public void ae2bmc$actionPerformed(final GuiButton btn, final CallbackInfo ci) {
        if (btn != this.ae2bmc$button) {
            return;
        }

        final IInventorySlotAware terminal;
        if (this.ae2bmc$obj != null) {
            terminal = this.ae2bmc$obj;
        } else if (this.inventorySlots instanceof IInventorySlotAware) {
            terminal = (IInventorySlotAware) this.inventorySlots;
        } else {
            return;
        }

        Main.NETWORK.sendToServer(new PacketOpenMagnetFilterGui(terminal.getInventorySlot(), terminal.isBaubleSlot()));
        ci.cancel();
    }

    @Unique
    private int ae2bmc$getToolbarTop() {
        int top = this.guiTop + 8;
        final int x = this.guiLeft - 18;

        for (final GuiButton button : this.buttonList) {
            if (button != this.ae2bmc$button && !ae2bmc$isWutButton(button) && button.x == x && top < button.y) {
                top = button.y;
            }
        }

        return top;
    }

    @Unique
    private void ae2bmc$syncMagnetFilterButton() {
        if (!ae2bmc$hasMagnetCard()) {
            if (this.ae2bmc$button != null) {
                this.buttonList.remove(this.ae2bmc$button);
                this.ae2bmc$button = null;
            }
            return;
        }

        if (this.ae2bmc$button == null || !this.buttonList.contains(this.ae2bmc$button)) {
            this.ae2bmc$button = new GuiMagnetFilterButton(ae2bmc$MAGNET_FILTER_BUTTON, this.guiLeft - 18,
                    ae2bmc$getToolbarTop() + 20);
            this.buttonList.add(this.ae2bmc$button);
        } else {
            this.ae2bmc$button.x = this.guiLeft - 18;
            this.ae2bmc$button.y = ae2bmc$getToolbarTop() + 20;
        }

        ae2bmc$moveWutButtonsBelowMagnetFilter();
    }

    @Unique
    private void ae2bmc$moveWutButtonsBelowMagnetFilter() {
        if (this.ae2bmc$button == null) {
            return;
        }

        final GuiButton wutButton = ae2bmc$getWutToolbarButton();
        if (wutButton == null) {
            return;
        }

        final int expectedY = this.ae2bmc$button.y + 20;
        final int delta = expectedY - wutButton.y;
        if (delta == 0) {
            return;
        }

        for (final GuiButton button : this.buttonList) {
            if (ae2bmc$isWutButton(button)) {
                button.y += delta;
            }
        }
    }

    @Unique
    private GuiButton ae2bmc$getWutToolbarButton() {
        final int x = this.guiLeft - 18;
        for (final GuiButton button : this.buttonList) {
            if (ae2bmc$isWutButton(button) && button.x == x) {
                return button;
            }
        }

        return null;
    }

    @Unique
    private boolean ae2bmc$isWutButton(final GuiButton button) {
        return button != null && ae2bmc$WUT_BUTTON_CLASS.equals(button.getClass().getName());
    }

    @Unique
    private boolean ae2bmc$hasMagnetCard() {
        if (!(this.inventorySlots instanceof ContainerMEPortableTerminal)) {
            return false;
        }

        final SlotRestrictedInput magnetSlot = ((AccessorContainerMEPortableTerminal) this.inventorySlots).ae2bmc$getMagnetSlot();
        return magnetSlot != null && AEApi.instance().definitions().materials().cardMagnet().isSameAs(magnetSlot.getStack());
    }
}
