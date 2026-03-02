package me.emvoh.ae2bettermagnetcard.events;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.materials.ItemMaterial;
import appeng.me.helpers.PlayerSource;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class MagnetStoreToMEHandler {

    private static final class TerminalAndMagnet {
        final ItemStack terminalStack;
        final int slotIdx;
        final boolean isBauble;
        final ItemStack magnetCard;

        private TerminalAndMagnet(final ItemStack terminalStack, final int slotIdx, final boolean isBauble, final ItemStack magnetCard) {
            this.terminalStack = terminalStack;
            this.slotIdx = slotIdx;
            this.isBauble = isBauble;
            this.magnetCard = magnetCard;
        }
    }

    @SubscribeEvent
    public void onItemPickup(final EntityItemPickupEvent event) {
        final EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world == null || player.world.isRemote) {
            return;
        }

        if (event.isCanceled()) {
            return;
        }

        final EntityItem entityItem = event.getItem();
        if (entityItem == null || entityItem.isDead) {
            return;
        }

        final NBTTagCompound entityTag = entityItem.getEntityData();
        if (entityTag != null && entityTag.hasKey("PreventRemoteMovement")) {
            return;
        }

        final ItemStack picked = entityItem.getItem();
        if (picked.isEmpty() || picked.getCount() <= 0) {
            return;
        }

        final TerminalAndMagnet ctx = findTerminalWithStoreMagnet(player);
        if (ctx == null) {
            return;
        }

        if (!passesMagnetFilter(ctx.magnetCard, picked)) {
            return;
        }

        final IWirelessTermHandler handler = AEApi.instance().registries().wireless().getWirelessTerminalHandler(ctx.terminalStack);
        if (handler == null) {
            return;
        }

        if (!handler.canHandle(ctx.terminalStack)) {
            return;
        }

        final String key = handler.getEncryptionKey(ctx.terminalStack);
        if (key == null || key.isEmpty()) {
            return;
        }

        final WirelessTerminalGuiObject gui = new WirelessTerminalGuiObject(handler, ctx.terminalStack, player, player.world, ctx.slotIdx, ctx.isBauble ? 1 : 0, 0);

        if (!gui.rangeCheck()) {
            return;
        }

        final IItemStorageChannel chan = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        IAEItemStack toInsert = chan.createStack(picked);
        if (toInsert == null) {
            return;
        }
        toInsert.setStackSize(picked.getCount());

        final IAEItemStack leftover = Platform.poweredInsert(gui, gui.getInventory(chan), toInsert, new PlayerSource(player, gui));

        final long inserted = toInsert.getStackSize() - (leftover == null ? 0 : leftover.getStackSize());
        if (inserted <= 0) {
            return;
        }

        event.setCanceled(true);

        // --- Feedback: emulate vanilla pickup sound when storing to ME ---
        final NBTTagCompound playerData = player.getEntityData();
        final long now = player.world.getTotalWorldTime();
        final long last = playerData.getLong("ae2_storeToMe_lastSound");

        if (now - last >= 3) {
            playerData.setLong("ae2_storeToMe_lastSound", now);

            player.world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.init.SoundEvents.ENTITY_ITEM_PICKUP, net.minecraft.util.SoundCategory.PLAYERS, 0.2F, ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        if (leftover == null || leftover.getStackSize() <= 0) {
            entityItem.setDead();
            return;
        }

        final ItemStack givePlayer = leftover.createItemStack();
        final InventoryAdaptor inv = InventoryAdaptor.getAdaptor(player);
        final ItemStack notAdded = inv.addItems(givePlayer);

        if (notAdded.isEmpty()) {
            entityItem.setDead();
        } else {
            entityItem.setItem(notAdded);
        }
    }

    @Nullable
    private TerminalAndMagnet findTerminalWithStoreMagnet(final EntityPlayer player) {
        final NonNullList<ItemStack> inv = player.inventory.mainInventory;

        // 1) Mainhand first
        TerminalAndMagnet ctx = checkTerminalStack(player.getHeldItemMainhand(), player.inventory.currentItem, false);
        if (ctx != null) {
            return ctx;
        }

        // 2) Hotbar next
        for (int i = 0; i < 9 && i < inv.size(); i++) {
            if (i == player.inventory.currentItem) {
                continue;
            }

            ctx = checkTerminalStack(inv.get(i), i, false);
            if (ctx != null) {
                return ctx;
            }
        }

        // 3) Rest of inventory
        for (int i = 9; i < inv.size(); i++) {
            ctx = checkTerminalStack(inv.get(i), i, false);
            if (ctx != null) {
                return ctx;
            }
        }

        // 4) Baubles last
        if (Platform.isModLoaded("baubles")) {
            final TerminalAndMagnet baubleCtx = tryFindInBaubles(player);
            if (baubleCtx != null) {
                return baubleCtx;
            }
        }

        return null;
    }

    private TerminalAndMagnet checkTerminalStack(final ItemStack terminalStack, final int slotIdx, final boolean isBauble) {
        if (terminalStack.isEmpty()) {
            return null;
        }

        if (!AEApi.instance().registries().wireless().isWirelessTerminal(terminalStack)) {
            return null;
        }

        final ItemStack magnetCard = findStoreEnabledMagnetCard(terminalStack);
        if (magnetCard == null || magnetCard.isEmpty()) {
            return null;
        }

        return new TerminalAndMagnet(terminalStack, slotIdx, isBauble, magnetCard);
    }

    @Optional.Method(modid = "baubles")
    private TerminalAndMagnet tryFindInBaubles(final EntityPlayer player) {
        final baubles.api.cap.IBaublesItemHandler bh = baubles.api.BaublesApi.getBaublesHandler(player);

        for (int i = 0; i < bh.getSlots(); i++) {
            final ItemStack stack = bh.getStackInSlot(i);
            final TerminalAndMagnet ctx = checkTerminalStack(stack, i, true);
            if (ctx != null) {
                return ctx;
            }
        }

        return null;
    }

    @Nullable
    private ItemStack findStoreEnabledMagnetCard(final ItemStack terminalStack) {
        final NBTTagCompound upgradesNbt = Platform.openNbtData(terminalStack).getCompoundTag("upgrades");

        final ItemStackHandler handler = new ItemStackHandler(0);
        handler.deserializeNBT(upgradesNbt);

        for (int s = 0; s < handler.getSlots(); s++) {
            final ItemStack card = handler.getStackInSlot(s);
            if (card.isEmpty()) {
                continue;
            }

            if (!AEApi.instance().definitions().materials().cardMagnet().isSameAs(card)) {
                continue;
            }

            final NBTTagCompound tag = card.getTagCompound();

            final boolean storeToME = tag != null && tag.hasKey("storeToME") && tag.getBoolean("storeToME");
            if (!storeToME) {
                continue;
            }

            return card;
        }

        return ItemStack.EMPTY;
    }

    private boolean passesMagnetFilter(final ItemStack magnetCard, final ItemStack candidate) {
        final ItemMaterial im = (ItemMaterial) magnetCard.getItem();
        final CellConfig cfg = (CellConfig) im.getConfigInventory(magnetCard);
        final CellUpgrades ups = (CellUpgrades) im.getUpgradesInventory(magnetCard);

        final boolean isFuzzy = ups.getInstalledUpgrades(Upgrades.FUZZY) == 1;
        final FuzzyMode fz = isFuzzy ? im.getFuzzyMode(magnetCard) : null;
        final boolean inverted = ups.getInstalledUpgrades(Upgrades.INVERTER) == 1;

        boolean emptyFilter = true;
        boolean matched = false;

        for (int i = 0; i < cfg.getSlots(); i++) {
            final ItemStack filter = cfg.getStackInSlot(i);
            if (filter.isEmpty()) {
                continue;
            }

            emptyFilter = false;

            if (isFuzzy) {
                if (Platform.itemComparisons().isFuzzyEqualItem(filter, candidate, fz)) {
                    matched = true;
                    break;
                }
            } else {
                if (Platform.itemComparisons().isSameItem(filter, candidate)) {
                    matched = true;
                    break;
                }
            }
        }

        if (emptyFilter) {
            return true;
        }

        if (matched && !inverted) {
            return true;
        }

        return !matched && inverted;
    }
}