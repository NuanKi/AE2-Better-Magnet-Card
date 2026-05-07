package me.emvoh.ae2bettermagnetcard.gui;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import me.emvoh.ae2bettermagnetcard.network.PacketMagnetFilterAction;
import me.emvoh.ae2bettermagnetcard.utils.MagnetCardFilters;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public final class ContainerMagnetFilter extends AEBaseContainer {

    private static final int UPGRADE_SLOT_X = 220;
    private static final int FUZZY_UPGRADE_Y = 8;
    private static final int RANGE_UPGRADE_Y = 26;
    private static final int FILTER_LEFT = 24;
    private static final int PICKUP_FILTER_TOP = 28;
    private static final int INSERT_FILTER_TOP = 140;
    private static final int PLAYER_INVENTORY_LEFT = 16;
    private static final int PLAYER_INVENTORY_TOP = 220;

    private final MagnetFilterHost host;
    private final MagnetUpgradeInventory upgradeInventory;
    private final MagnetFilterInventory pickupFilter;
    private final MagnetFilterInventory insertFilter;
    private final SlotRestrictedInput fuzzyUpgradeSlot;
    private final SlotRestrictedInput rangeUpgradeSlot;
    private int playerInventoryStart;
    private int playerInventoryEnd;

    public ContainerMagnetFilter(final InventoryPlayer playerInv, final MagnetFilterHost host) {
        super(playerInv, host);
        this.host = host;

        host.ensureFiltersInitialized();
        this.upgradeInventory = new MagnetUpgradeInventory(host);
        this.pickupFilter = new MagnetFilterInventory(host, MagnetCardFilters.FilterType.PICKUP);
        this.insertFilter = new MagnetFilterInventory(host, MagnetCardFilters.FilterType.INSERT);

        this.fuzzyUpgradeSlot = addUpgradeSlot(playerInv, MagnetUpgradeInventory.FUZZY_SLOT, UPGRADE_SLOT_X, FUZZY_UPGRADE_Y);
        this.rangeUpgradeSlot = addUpgradeSlot(playerInv, MagnetUpgradeInventory.RANGE_SLOT, UPGRADE_SLOT_X, RANGE_UPGRADE_Y);
        addFilterSlots(pickupFilter, FILTER_LEFT, PICKUP_FILTER_TOP);
        addFilterSlots(insertFilter, FILTER_LEFT, INSERT_FILTER_TOP);
        playerInventoryStart = inventorySlots.size();
        bindPlayerInventory(playerInv, PLAYER_INVENTORY_LEFT, PLAYER_INVENTORY_TOP);
        playerInventoryEnd = inventorySlots.size();
    }

    private SlotRestrictedInput addUpgradeSlot(final InventoryPlayer playerInv, final int index, final int x, final int y) {
        final SlotRestrictedInput slot = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES,
                upgradeInventory, index, x, y, playerInv);
        slot.setStackLimit(1);
        addSlotToContainer(slot);
        return slot;
    }

    private void addFilterSlots(final MagnetFilterInventory inventory, final int left, final int top) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new SlotFakeTypeOnly(inventory, y * 9 + x, left + x * 18, top + y * 18));
            }
        }
    }

    public int getTerminalSlot() {
        return host.getTerminalSlot();
    }

    public boolean isBaubleSlot() {
        return host.isBaubleSlot();
    }

    public MagnetCardFilters.FilterMode getPickupMode() {
        return host.getMode(MagnetCardFilters.FilterType.PICKUP);
    }

    public MagnetCardFilters.FilterMode getInsertMode() {
        return host.getMode(MagnetCardFilters.FilterType.INSERT);
    }

    public ItemStack getUpgradeStack(final int slot) {
        return upgradeInventory.getStackInSlot(slot);
    }

    public void applyAction(final PacketMagnetFilterAction.Action action) {
        switch (action) {
            case TOGGLE_PICKUP:
                host.toggleMode(MagnetCardFilters.FilterType.PICKUP);
                break;
            case TOGGLE_INSERT:
                host.toggleMode(MagnetCardFilters.FilterType.INSERT);
                break;
            case COPY_PICKUP_TO_INSERT:
                insertFilter.replaceFrom(pickupFilter);
                host.copyFilter(MagnetCardFilters.FilterType.PICKUP, MagnetCardFilters.FilterType.INSERT);
                insertFilter.reload();
                break;
            case COPY_INSERT_TO_PICKUP:
                pickupFilter.replaceFrom(insertFilter);
                host.copyFilter(MagnetCardFilters.FilterType.INSERT, MagnetCardFilters.FilterType.PICKUP);
                pickupFilter.reload();
                break;
            case SWAP:
                host.swapFilters();
                pickupFilter.reload();
                insertFilter.reload();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        return host.isValid();
    }

    @Override
    public boolean isValidForSlot(final Slot slot, final ItemStack stack) {
        if (slot == fuzzyUpgradeSlot) {
            return upgradeInventory.isItemValid(MagnetUpgradeInventory.FUZZY_SLOT, stack);
        }
        if (slot == rangeUpgradeSlot) {
            return upgradeInventory.isItemValid(MagnetUpgradeInventory.RANGE_SLOT, stack);
        }

        return super.isValidForSlot(slot, stack);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }

        final Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getStack();
        final ItemStack original = stack.copy();

        if (slot == fuzzyUpgradeSlot || slot == rangeUpgradeSlot) {
            if (!mergeItemStack(stack, playerInventoryStart, playerInventoryEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= playerInventoryStart && index < playerInventoryEnd) {
            if (!moveOneUpgrade(stack, fuzzyUpgradeSlot) && !moveOneUpgrade(stack, rangeUpgradeSlot)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        return original;
    }

    private boolean moveOneUpgrade(final ItemStack stack, final SlotRestrictedInput destination) {
        if (stack.isEmpty() || destination.getHasStack()) {
            return false;
        }

        final ItemStack one = stack.copy();
        one.setCount(1);
        if (!destination.isItemValid(one)) {
            return false;
        }

        destination.putStack(one);
        destination.onSlotChanged();
        stack.shrink(1);
        return true;
    }
}
