package me.emvoh.ae2bettermagnetcard.gui;

import me.emvoh.ae2bettermagnetcard.utils.MagnetCardFilters;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class MagnetUpgradeInventory extends ItemStackHandler {

    public static final int FUZZY_SLOT = 0;
    public static final int RANGE_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    private final MagnetFilterHost host;
    private boolean loading;

    public MagnetUpgradeInventory(final MagnetFilterHost host) {
        super(SLOT_COUNT);
        this.host = host;
        reload();
    }

    public void reload() {
        loading = true;
        setStackInSlot(FUZZY_SLOT, host.readManagedUpgrade(MagnetCardFilters.UpgradeSlot.FUZZY));
        setStackInSlot(RANGE_SLOT, host.readManagedUpgrade(MagnetCardFilters.UpgradeSlot.RANGE));
        loading = false;
    }

    @Override
    public int getSlotLimit(final int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) {
        final MagnetCardFilters.UpgradeSlot type = getUpgradeSlot(slot);
        return type != null && type.accepts(stack) && host.canWriteManagedUpgrade(type, stack);
    }

    @Override
    public void setStackInSlot(final int slot, ItemStack stack) {
        final MagnetCardFilters.UpgradeSlot type = getUpgradeSlot(slot);
        if (type == null) {
            return;
        }

        if (stack == null) {
            stack = ItemStack.EMPTY;
        }
        if (!stack.isEmpty()) {
            if (!type.accepts(stack)) {
                return;
            }
            stack = stack.copy();
            stack.setCount(1);
        }

        super.setStackInSlot(slot, stack);
    }

    @Override
    protected void onContentsChanged(final int slot) {
        if (loading) {
            return;
        }

        final MagnetCardFilters.UpgradeSlot type = getUpgradeSlot(slot);
        if (type != null) {
            host.saveManagedUpgrade(type, getStackInSlot(slot));
        }
    }

    private MagnetCardFilters.UpgradeSlot getUpgradeSlot(final int slot) {
        switch (slot) {
            case FUZZY_SLOT:
                return MagnetCardFilters.UpgradeSlot.FUZZY;
            case RANGE_SLOT:
                return MagnetCardFilters.UpgradeSlot.RANGE;
            default:
                return null;
        }
    }
}
