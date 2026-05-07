package me.emvoh.ae2bettermagnetcard.utils;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.materials.ItemMaterial;
import appeng.util.Platform;
import me.emvoh.ae2bettermagnetcard.api.IBMCUpgradeModule;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgrades;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public final class MagnetCardFilters {

    public static final int FILTER_SIZE = 27;

    private static final String KEY_INITIALIZED = "bmcFiltersInitialized";
    private static final String KEY_PICKUP_FILTER = "bmcPickupFilter";
    private static final String KEY_INSERT_FILTER = "bmcInsertFilter";
    private static final String KEY_PICKUP_MODE = "bmcPickupMode";
    private static final String KEY_INSERT_MODE = "bmcInsertMode";

    private MagnetCardFilters() {
    }

    public enum FilterType {
        PICKUP(KEY_PICKUP_FILTER, KEY_PICKUP_MODE),
        INSERT(KEY_INSERT_FILTER, KEY_INSERT_MODE);

        private final String filterKey;
        private final String modeKey;

        FilterType(final String filterKey, final String modeKey) {
            this.filterKey = filterKey;
            this.modeKey = modeKey;
        }

        public String getFilterKey() {
            return filterKey;
        }

        public String getModeKey() {
            return modeKey;
        }
    }

    public enum FilterMode {
        WHITELIST,
        BLACKLIST;

        public FilterMode toggle() {
            return this == WHITELIST ? BLACKLIST : WHITELIST;
        }
    }

    public enum UpgradeSlot {
        FUZZY,
        RANGE;

        public boolean accepts(final ItemStack stack) {
            switch (this) {
                case FUZZY:
                    return isFuzzyUpgrade(stack);
                case RANGE:
                    return isRangeUpgrade(stack);
                default:
                    return false;
            }
        }
    }

    public static boolean hasCustomFilters(final ItemStack magnetCard) {
        if (magnetCard == null || magnetCard.isEmpty()) {
            return false;
        }

        final NBTTagCompound tag = magnetCard.getTagCompound();
        return tag != null && tag.getBoolean(KEY_INITIALIZED);
    }

    public static void ensureInitializedFromLegacy(final ItemStack magnetCard) {
        if (magnetCard == null || magnetCard.isEmpty() || hasCustomFilters(magnetCard)) {
            return;
        }

        final ItemStackHandler pickup = new ItemStackHandler(FILTER_SIZE);
        final ItemStackHandler insert = new ItemStackHandler(FILTER_SIZE);

        final ItemMaterial im = (ItemMaterial) magnetCard.getItem();
        final CellConfig cfg = (CellConfig) im.getConfigInventory(magnetCard);
        final CellUpgrades ups = (CellUpgrades) im.getUpgradesInventory(magnetCard);

        boolean legacyHasEntries = false;
        final int slots = Math.min(FILTER_SIZE, cfg.getSlots());
        for (int i = 0; i < slots; i++) {
            final ItemStack configured = cfg.getStackInSlot(i);
            if (configured.isEmpty()) {
                continue;
            }

            final ItemStack one = configured.copy();
            one.setCount(1);
            pickup.setStackInSlot(i, one.copy());
            insert.setStackInSlot(i, one.copy());
            legacyHasEntries = true;
        }

        final boolean legacyInverted = hasInstalledUpgrade(ups, Upgrades.INVERTER);
        final FilterMode mode = !legacyHasEntries || legacyInverted ? FilterMode.BLACKLIST : FilterMode.WHITELIST;

        writeFilter(magnetCard, FilterType.PICKUP, pickup);
        writeFilter(magnetCard, FilterType.INSERT, insert);
        setMode(magnetCard, FilterType.PICKUP, mode);
        setMode(magnetCard, FilterType.INSERT, mode);
        getOrCreateTag(magnetCard).setBoolean(KEY_INITIALIZED, true);
    }

    public static ItemStackHandler readFilter(final ItemStack magnetCard, final FilterType type) {
        final ItemStackHandler handler = new ItemStackHandler(FILTER_SIZE);
        if (magnetCard == null || magnetCard.isEmpty()) {
            return handler;
        }

        final NBTTagCompound tag = magnetCard.getTagCompound();
        if (tag != null && tag.hasKey(type.getFilterKey())) {
            handler.deserializeNBT(tag.getCompoundTag(type.getFilterKey()));
        }
        return handler;
    }

    public static void writeFilter(final ItemStack magnetCard, final FilterType type, final ItemStackHandler handler) {
        if (magnetCard == null || magnetCard.isEmpty()) {
            return;
        }

        final NBTTagCompound tag = getOrCreateTag(magnetCard);
        tag.setTag(type.getFilterKey(), handler.serializeNBT());
        tag.setBoolean(KEY_INITIALIZED, true);
    }

    public static FilterMode getMode(final ItemStack magnetCard, final FilterType type) {
        if (magnetCard == null || magnetCard.isEmpty()) {
            return FilterMode.BLACKLIST;
        }

        final NBTTagCompound tag = magnetCard.getTagCompound();
        if (tag == null || !tag.hasKey(type.getModeKey())) {
            return FilterMode.BLACKLIST;
        }

        return tag.getBoolean(type.getModeKey()) ? FilterMode.WHITELIST : FilterMode.BLACKLIST;
    }

    public static void setMode(final ItemStack magnetCard, final FilterType type, final FilterMode mode) {
        if (magnetCard == null || magnetCard.isEmpty()) {
            return;
        }

        final NBTTagCompound tag = getOrCreateTag(magnetCard);
        tag.setBoolean(type.getModeKey(), mode == FilterMode.WHITELIST);
        tag.setBoolean(KEY_INITIALIZED, true);
    }

    public static ItemStack readManagedUpgrade(final ItemStack magnetCard, final UpgradeSlot slot) {
        final CellUpgrades upgrades = getMagnetUpgrades(magnetCard);
        if (upgrades == null) {
            return ItemStack.EMPTY;
        }

        final int index = findManagedUpgradeSlot(upgrades, slot);
        return index >= 0 ? copyOne(upgrades.getStackInSlot(index)) : ItemStack.EMPTY;
    }

    public static boolean canWriteManagedUpgrade(final ItemStack magnetCard, final UpgradeSlot slot, final ItemStack upgrade) {
        final CellUpgrades upgrades = getMagnetUpgrades(magnetCard);
        if (upgrades == null) {
            return false;
        }

        if (upgrade == null || upgrade.isEmpty()) {
            return true;
        }

        if (!slot.accepts(upgrade)) {
            return false;
        }

        return findManagedUpgradeSlot(upgrades, slot) >= 0 || findEmptyUpgradeSlot(upgrades) >= 0;
    }

    public static void writeManagedUpgrade(final ItemStack magnetCard, final UpgradeSlot slot, final ItemStack upgrade) {
        final CellUpgrades upgrades = getMagnetUpgrades(magnetCard);
        if (upgrades == null) {
            return;
        }

        final int existing = findManagedUpgradeSlot(upgrades, slot);
        if (upgrade == null || upgrade.isEmpty()) {
            clearManagedUpgrades(upgrades, slot, -1);
            return;
        }

        if (!slot.accepts(upgrade)) {
            return;
        }

        final ItemStack one = copyOne(upgrade);
        if (existing >= 0) {
            upgrades.setStackInSlot(existing, one);
            clearManagedUpgrades(upgrades, slot, existing);
            return;
        }

        final int empty = findEmptyUpgradeSlot(upgrades);
        if (empty >= 0) {
            upgrades.setStackInSlot(empty, one);
        }
    }

    public static boolean passesInsertFilter(final ItemStack magnetCard, final ItemStack candidate) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }
        if (magnetCard == null || magnetCard.isEmpty()) {
            return true;
        }

        if (!hasCustomFilters(magnetCard)) {
            return passesLegacyFilter(magnetCard, candidate);
        }

        return matchesCustomFilter(magnetCard, FilterType.INSERT, candidate);
    }

    public static boolean passesPickupFilter(final ItemStack magnetCard, final ItemStack candidate) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }
        if (magnetCard == null || magnetCard.isEmpty()) {
            return true;
        }

        if (!hasCustomFilters(magnetCard)) {
            return true;
        }

        return matchesCustomFilter(magnetCard, FilterType.PICKUP, candidate);
    }

    public static boolean passesPickupFilterForTerminal(final ItemStack terminalStack, final ItemStack candidate) {
        final ItemStack magnetCard = findMagnetCardInTerminal(terminalStack);
        if (magnetCard.isEmpty()) {
            return true;
        }

        return passesPickupFilter(magnetCard, candidate);
    }

    public static int getPickupFilterSlots(final ItemStack magnetCard, final CellConfig legacyConfig) {
        return hasCustomFilters(magnetCard) ? FILTER_SIZE : legacyConfig.getSlots();
    }

    public static ItemStack getPickupFilterStack(final ItemStack magnetCard, final CellConfig legacyConfig, final int slot) {
        if (!hasCustomFilters(magnetCard)) {
            return legacyConfig.getStackInSlot(slot);
        }

        return readFilter(magnetCard, FilterType.PICKUP).getStackInSlot(slot);
    }

    public static int getPickupInverterValue(final ItemStack magnetCard, final CellUpgrades legacyUpgrades, final Upgrades upgrade) {
        if (upgrade != Upgrades.INVERTER) {
            return legacyUpgrades.getInstalledUpgrades(upgrade);
        }

        if (!hasCustomFilters(magnetCard)) {
            return hasInstalledUpgrade(legacyUpgrades, Upgrades.INVERTER) ? 1 : 0;
        }

        return getMode(magnetCard, FilterType.PICKUP) == FilterMode.BLACKLIST ? 1 : 0;
    }

    public static void copyFilter(final ItemStack magnetCard, final FilterType from, final FilterType to) {
        final ItemStackHandler src = readFilter(magnetCard, from);
        writeFilter(magnetCard, to, src);
        setMode(magnetCard, to, getMode(magnetCard, from));
    }

    public static void swapFilters(final ItemStack magnetCard) {
        final ItemStackHandler pickup = readFilter(magnetCard, FilterType.PICKUP);
        final ItemStackHandler insert = readFilter(magnetCard, FilterType.INSERT);
        final FilterMode pickupMode = getMode(magnetCard, FilterType.PICKUP);
        final FilterMode insertMode = getMode(magnetCard, FilterType.INSERT);

        writeFilter(magnetCard, FilterType.PICKUP, insert);
        writeFilter(magnetCard, FilterType.INSERT, pickup);
        setMode(magnetCard, FilterType.PICKUP, insertMode);
        setMode(magnetCard, FilterType.INSERT, pickupMode);
    }

    private static boolean matchesCustomFilter(final ItemStack magnetCard, final FilterType type, final ItemStack candidate) {
        final ItemMaterial im = (ItemMaterial) magnetCard.getItem();
        final CellUpgrades ups = (CellUpgrades) im.getUpgradesInventory(magnetCard);
        final boolean fuzzy = ups.getInstalledUpgrades(Upgrades.FUZZY) == 1;
        final FuzzyMode fuzzyMode = fuzzy ? im.getFuzzyMode(magnetCard) : null;

        return matches(readFilter(magnetCard, type), getMode(magnetCard, type), candidate, fuzzy, fuzzyMode);
    }

    public static boolean passesLegacyFilter(final ItemStack magnetCard, final ItemStack candidate) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }
        if (magnetCard == null || magnetCard.isEmpty()) {
            return true;
        }

        final ItemMaterial im = (ItemMaterial) magnetCard.getItem();
        final CellConfig cfg = (CellConfig) im.getConfigInventory(magnetCard);
        final CellUpgrades ups = (CellUpgrades) im.getUpgradesInventory(magnetCard);

        final boolean fuzzy = ups.getInstalledUpgrades(Upgrades.FUZZY) == 1;
        final FuzzyMode fuzzyMode = fuzzy ? im.getFuzzyMode(magnetCard) : null;
        final boolean inverted = hasInstalledUpgrade(ups, Upgrades.INVERTER);

        boolean emptyFilter = true;
        boolean matched = false;

        for (int i = 0; i < cfg.getSlots(); i++) {
            final ItemStack filter = cfg.getStackInSlot(i);
            if (filter.isEmpty()) {
                continue;
            }

            emptyFilter = false;
            if (sameItem(filter, candidate, fuzzy, fuzzyMode)) {
                matched = true;
                break;
            }
        }

        if (emptyFilter) {
            return true;
        }

        return inverted ? !matched : matched;
    }

    private static boolean matches(final ItemStackHandler filter, final FilterMode mode, final ItemStack candidate, final boolean fuzzy, final FuzzyMode fuzzyMode) {
        boolean emptyFilter = true;
        boolean matched = false;

        for (int i = 0; i < filter.getSlots(); i++) {
            final ItemStack filterStack = filter.getStackInSlot(i);
            if (filterStack.isEmpty()) {
                continue;
            }

            emptyFilter = false;
            if (sameItem(filterStack, candidate, fuzzy, fuzzyMode)) {
                matched = true;
                break;
            }
        }

        if (emptyFilter) {
            return mode == FilterMode.BLACKLIST;
        }

        return mode == FilterMode.WHITELIST ? matched : !matched;
    }

    private static boolean sameItem(final ItemStack filterStack, final ItemStack candidate, final boolean fuzzy, final FuzzyMode fuzzyMode) {
        if (fuzzy) {
            return Platform.itemComparisons().isFuzzyEqualItem(filterStack, candidate, fuzzyMode);
        }
        return Platform.itemComparisons().isSameItem(filterStack, candidate);
    }

    private static CellUpgrades getMagnetUpgrades(final ItemStack magnetCard) {
        if (magnetCard == null || magnetCard.isEmpty() || !(magnetCard.getItem() instanceof ItemMaterial)) {
            return null;
        }
        if (!AEApi.instance().definitions().materials().cardMagnet().isSameAs(magnetCard)) {
            return null;
        }

        return (CellUpgrades) ((ItemMaterial) magnetCard.getItem()).getUpgradesInventory(magnetCard);
    }

    private static int findManagedUpgradeSlot(final CellUpgrades upgrades, final UpgradeSlot slot) {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (slot.accepts(upgrades.getStackInSlot(i))) {
                return i;
            }
        }
        return -1;
    }

    private static int findEmptyUpgradeSlot(final CellUpgrades upgrades) {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (upgrades.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static void clearManagedUpgrades(final CellUpgrades upgrades, final UpgradeSlot slot, final int exceptSlot) {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            if (i != exceptSlot && slot.accepts(upgrades.getStackInSlot(i))) {
                upgrades.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    private static ItemStack copyOne(final ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        final ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static boolean isFuzzyUpgrade(final ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IUpgradeModule)) {
            return false;
        }

        return ((IUpgradeModule) stack.getItem()).getType(stack) == Upgrades.FUZZY;
    }

    private static boolean hasInstalledUpgrade(final CellUpgrades upgrades, final Upgrades upgrade) {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            final ItemStack stack = upgrades.getStackInSlot(i);
            if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IUpgradeModule)) {
                continue;
            }

            if (((IUpgradeModule) stack.getItem()).getType(stack) == upgrade) {
                return true;
            }
        }

        return false;
    }

    private static boolean isRangeUpgrade(final ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IBMCUpgradeModule)) {
            return false;
        }

        final BMCUpgrades type = ((IBMCUpgradeModule) stack.getItem()).getType(stack);
        return type == BMCUpgrades.RANGE || type == BMCUpgrades.ADVANCED_RANGE;
    }

    private static NBTTagCompound getOrCreateTag(final ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    public static ItemStack findMagnetCardInTerminal(final ItemStack terminalStack) {
        if (terminalStack == null || terminalStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        final NBTTagCompound upgradesNbt = Platform.openNbtData(terminalStack).getCompoundTag("upgrades");
        final ItemStackHandler upgradesHandler = new ItemStackHandler(0);
        upgradesHandler.deserializeNBT(upgradesNbt);

        for (int slot = 0; slot < upgradesHandler.getSlots(); slot++) {
            final ItemStack card = upgradesHandler.getStackInSlot(slot);
            if (!card.isEmpty() && AEApi.instance().definitions().materials().cardMagnet().isSameAs(card)) {
                return card;
            }
        }

        return ItemStack.EMPTY;
    }
}
