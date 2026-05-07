package me.emvoh.ae2bettermagnetcard.gui;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.util.Platform;
import me.emvoh.ae2bettermagnetcard.utils.MagnetCardFilters;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.ItemStackHandler;

public final class MagnetFilterHost implements IGuiItemObject {

    private final EntityPlayer player;
    private final int terminalSlot;
    private final boolean baubleSlot;

    public MagnetFilterHost(final EntityPlayer player, final int terminalSlot, final boolean baubleSlot) {
        this.player = player;
        this.terminalSlot = terminalSlot;
        this.baubleSlot = baubleSlot;
    }

    public int getTerminalSlot() {
        return terminalSlot;
    }

    public boolean isBaubleSlot() {
        return baubleSlot;
    }

    public boolean isValid() {
        final ItemStack terminal = getTerminalStack();
        if (terminal.isEmpty()) {
            return false;
        }

        return !findMagnetCard(terminal).isEmpty();
    }

    @Override
    public ItemStack getItemStack() {
        return getTerminalStack();
    }

    public void ensureFiltersInitialized() {
        editMagnet(MagnetCardFilters::ensureInitializedFromLegacy);
    }

    public ItemStackHandler readFilter(final MagnetCardFilters.FilterType type) {
        final ItemStack magnetCard = getMagnetCard();
        return MagnetCardFilters.readFilter(magnetCard, type);
    }

    public MagnetCardFilters.FilterMode getMode(final MagnetCardFilters.FilterType type) {
        final ItemStack magnetCard = getMagnetCard();
        return MagnetCardFilters.getMode(magnetCard, type);
    }

    public void saveFilter(final MagnetCardFilters.FilterType type, final ItemStackHandler handler) {
        editMagnet(magnet -> MagnetCardFilters.writeFilter(magnet, type, handler));
    }

    public ItemStack readManagedUpgrade(final MagnetCardFilters.UpgradeSlot slot) {
        return MagnetCardFilters.readManagedUpgrade(getMagnetCard(), slot);
    }

    public boolean canWriteManagedUpgrade(final MagnetCardFilters.UpgradeSlot slot, final ItemStack upgrade) {
        return MagnetCardFilters.canWriteManagedUpgrade(getMagnetCard(), slot, upgrade);
    }

    public void saveManagedUpgrade(final MagnetCardFilters.UpgradeSlot slot, final ItemStack upgrade) {
        editMagnet(magnet -> MagnetCardFilters.writeManagedUpgrade(magnet, slot, upgrade));
    }

    public void toggleMode(final MagnetCardFilters.FilterType type) {
        editMagnet(magnet -> MagnetCardFilters.setMode(magnet, type, MagnetCardFilters.getMode(magnet, type).toggle()));
    }

    public void copyFilter(final MagnetCardFilters.FilterType from, final MagnetCardFilters.FilterType to) {
        editMagnet(magnet -> MagnetCardFilters.copyFilter(magnet, from, to));
    }

    public void swapFilters() {
        editMagnet(MagnetCardFilters::swapFilters);
    }

    private ItemStack getMagnetCard() {
        final ItemStack terminal = getTerminalStack();
        if (terminal.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return findMagnetCard(terminal);
    }

    private void editMagnet(final MagnetEditor editor) {
        final ItemStack terminal = getTerminalStack();
        if (terminal.isEmpty()) {
            return;
        }

        final NBTTagCompound terminalTag = Platform.openNbtData(terminal);
        final NBTTagCompound upgradesNbt = terminalTag.getCompoundTag("upgrades");
        final ItemStackHandler upgradesHandler = new ItemStackHandler(0);
        upgradesHandler.deserializeNBT(upgradesNbt);

        for (int s = 0; s < upgradesHandler.getSlots(); s++) {
            final ItemStack card = upgradesHandler.getStackInSlot(s);
            if (card.isEmpty() || !AEApi.instance().definitions().materials().cardMagnet().isSameAs(card)) {
                continue;
            }

            editor.edit(card);
            upgradesHandler.setStackInSlot(s, card);
            terminalTag.setTag("upgrades", upgradesHandler.serializeNBT());
            markTerminalChanged(terminal);
            return;
        }
    }

    private ItemStack findMagnetCard(final ItemStack terminal) {
        final NBTTagCompound upgradesNbt = Platform.openNbtData(terminal).getCompoundTag("upgrades");
        final ItemStackHandler upgradesHandler = new ItemStackHandler(0);
        upgradesHandler.deserializeNBT(upgradesNbt);

        for (int s = 0; s < upgradesHandler.getSlots(); s++) {
            final ItemStack card = upgradesHandler.getStackInSlot(s);
            if (!card.isEmpty() && AEApi.instance().definitions().materials().cardMagnet().isSameAs(card)) {
                return card;
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack getTerminalStack() {
        if (baubleSlot) {
            if (!Platform.isModLoaded("baubles")) {
                return ItemStack.EMPTY;
            }
            return getBaubleStack();
        }

        final NonNullList<ItemStack> inv = player.inventory.mainInventory;
        if (terminalSlot < 0 || terminalSlot >= inv.size()) {
            return ItemStack.EMPTY;
        }

        return inv.get(terminalSlot);
    }

    @Optional.Method(modid = "baubles")
    private ItemStack getBaubleStack() {
        final baubles.api.cap.IBaublesItemHandler bh = baubles.api.BaublesApi.getBaublesHandler(player);
        if (bh == null || terminalSlot < 0 || terminalSlot >= bh.getSlots()) {
            return ItemStack.EMPTY;
        }
        return bh.getStackInSlot(terminalSlot);
    }

    private void markTerminalChanged(final ItemStack terminal) {
        if (baubleSlot && Platform.isModLoaded("baubles")) {
            setBaubleStack(terminal);
            return;
        }

        player.inventory.markDirty();
    }

    @Optional.Method(modid = "baubles")
    private void setBaubleStack(final ItemStack terminal) {
        final baubles.api.cap.IBaublesItemHandler bh = baubles.api.BaublesApi.getBaublesHandler(player);
        if (bh != null && terminalSlot >= 0 && terminalSlot < bh.getSlots()) {
            bh.setStackInSlot(terminalSlot, terminal);
            bh.setChanged(terminalSlot, true);
        }
    }

    private interface MagnetEditor {
        void edit(ItemStack magnetCard);
    }
}
