package me.emvoh.ae2bettermagnetcard.gui;

import me.emvoh.ae2bettermagnetcard.utils.MagnetCardFilters;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class MagnetFilterInventory extends ItemStackHandler {

    private final MagnetFilterHost host;
    private final MagnetCardFilters.FilterType type;
    private boolean loading;

    public MagnetFilterInventory(final MagnetFilterHost host, final MagnetCardFilters.FilterType type) {
        super(MagnetCardFilters.FILTER_SIZE);
        this.host = host;
        this.type = type;
        reload();
    }

    public void reload() {
        loading = true;
        final ItemStackHandler source = host.readFilter(type);
        for (int i = 0; i < getSlots(); i++) {
            setStackInSlot(i, source.getStackInSlot(i));
        }
        loading = false;
    }

    public void replaceFrom(final ItemStackHandler source) {
        loading = true;
        for (int i = 0; i < getSlots(); i++) {
            setStackInSlot(i, source.getStackInSlot(i).copy());
        }
        loading = false;
        host.saveFilter(type, this);
    }

    @Override
    public void setStackInSlot(final int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(1);
        }
        super.setStackInSlot(slot, stack);
    }

    @Override
    protected void onContentsChanged(final int slot) {
        if (!loading) {
            host.saveFilter(type, this);
        }
    }
}
