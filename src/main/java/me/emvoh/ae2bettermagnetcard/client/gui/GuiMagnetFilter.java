package me.emvoh.ae2bettermagnetcard.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.IJEITargetSlot;
import appeng.container.slot.SlotFake;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import me.emvoh.ae2bettermagnetcard.Main;
import me.emvoh.ae2bettermagnetcard.Tags;
import me.emvoh.ae2bettermagnetcard.client.gui.style.BackgroundGenerator;
import me.emvoh.ae2bettermagnetcard.client.gui.widgets.AE2Button;
import me.emvoh.ae2bettermagnetcard.config.BMCConfig;
import me.emvoh.ae2bettermagnetcard.gui.ContainerMagnetFilter;
import me.emvoh.ae2bettermagnetcard.gui.MagnetUpgradeInventory;
import me.emvoh.ae2bettermagnetcard.network.PacketMagnetFilterAction;
import me.emvoh.ae2bettermagnetcard.utils.MagnetCardFilters;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public final class GuiMagnetFilter extends AEBaseGui implements IJEIGhostIngredients {

    private static final int PICKUP_MODE = 0;
    private static final int INSERT_MODE = 1;
    private static final int COPY_PICKUP_TO_INSERT = 2;
    private static final int COPY_INSERT_TO_PICKUP = 3;
    private static final int SWAP = 4;

    private static final int MAIN_WIDTH = 210;
    private static final int WIDTH = 245;
    private static final int HEIGHT = 304;
    private static final int FILTER_LEFT = 24;
    private static final int PICKUP_FILTER_TOP = 28;
    private static final int INSERT_FILTER_TOP = 140;
    private static final int PLAYER_INVENTORY_LEFT = 16;
    private static final int PLAYER_INVENTORY_TOP = 220;
    private static final int PLAYER_INVENTORY_SLOT_X_OFFSET = 8;
    private static final int UPGRADE_PANEL_X = MAIN_WIDTH;
    private static final int UPGRADE_PANEL_WIDTH = 35;
    private static final int UPGRADE_PANEL_HEIGHT = 50;
    private static final int UPGRADE_SLOT_X = MAIN_WIDTH + 10;
    private static final int FUZZY_UPGRADE_Y = 8;
    private static final int RANGE_UPGRADE_Y = 26;
    private static final int PICKUP_TITLE_Y = 10;
    private static final int INSERT_TITLE_Y = 122;
    private static final int INVENTORY_TITLE_Y = 206;
    private static final int MODE_BUTTON_X = 146;
    private static final int MODE_BUTTON_WIDTH = 56;
    private static final int MODE_BUTTON_HEIGHT = 18;
    private static final int PICKUP_MODE_Y = 5;
    private static final int INSERT_MODE_Y = 117;
    private static final int CONTROL_Y = 90;
    private static final int COPY_LEFT_X = 34;
    private static final int SWAP_X = 82;
    private static final int COPY_RIGHT_X = 142;
    private static final int COPY_BUTTON_WIDTH = 42;
    private static final int SWAP_BUTTON_WIDTH = 52;
    private static final int CONTROL_BUTTON_HEIGHT = 20;
    private static final int SLOT_GRID_WIDTH = 162;
    private static final int FILTER_GRID_HEIGHT = 54;
    private static final int PLAYER_INVENTORY_HEIGHT = 76;
    private static final int SLOT_TEXTURE_SIZE = 256;
    private static final int CLASSIC_FILTER_TEXTURE_X = 7;
    private static final int CLASSIC_FILTER_TEXTURE_Y = 17;
    private static final int CLASSIC_PLAYER_TEXTURE_Y = 139;
    private static final ResourceLocation CLASSIC_SLOT_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private static final ResourceLocation MODERN_SLOT_TEXTURE = new ResourceLocation(Tags.MODID, "textures/guis/filter_inventory.png");

    private final ContainerMagnetFilter container;
    private final Map<IGhostIngredientHandler.Target<?>, Object> mapTargetSlot = new HashMap<>();

    public GuiMagnetFilter(final ContainerMagnetFilter container) {
        super(container);
        this.container = container;
        this.xSize = WIDTH;
        this.ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        buttonList.add(new AE2Button(PICKUP_MODE, guiLeft + MODE_BUTTON_X, guiTop + PICKUP_MODE_Y,
                MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT, pickupModeText()));
        buttonList.add(new AE2Button(INSERT_MODE, guiLeft + MODE_BUTTON_X, guiTop + INSERT_MODE_Y,
                MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT, insertModeText()));
        buttonList.add(new AE2Button(COPY_PICKUP_TO_INSERT, guiLeft + COPY_LEFT_X, guiTop + CONTROL_Y,
                COPY_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT, "P>I"));
        buttonList.add(new AE2Button(SWAP, guiLeft + SWAP_X, guiTop + CONTROL_Y,
                SWAP_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT, I18n.format("gui.ae2bettermagnetcard.magnet_filter.switch")));
        buttonList.add(new AE2Button(COPY_INSERT_TO_PICKUP, guiLeft + COPY_RIGHT_X, guiTop + CONTROL_Y,
                COPY_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT, "I>P"));
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException {
        final PacketMagnetFilterAction.Action action;
        switch (button.id) {
            case PICKUP_MODE:
                action = PacketMagnetFilterAction.Action.TOGGLE_PICKUP;
                break;
            case INSERT_MODE:
                action = PacketMagnetFilterAction.Action.TOGGLE_INSERT;
                break;
            case COPY_PICKUP_TO_INSERT:
                action = PacketMagnetFilterAction.Action.COPY_PICKUP_TO_INSERT;
                break;
            case COPY_INSERT_TO_PICKUP:
                action = PacketMagnetFilterAction.Action.COPY_INSERT_TO_PICKUP;
                break;
            case SWAP:
                action = PacketMagnetFilterAction.Action.SWAP;
                break;
            default:
                return;
        }

        container.applyAction(action);
        Main.NETWORK.sendToServer(new PacketMagnetFilterAction(container.getTerminalSlot(), container.isBaubleSlot(), action));
        updateButtonLabels();
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        fontRenderer.drawString(I18n.format("gui.ae2bettermagnetcard.magnet_filter.pickup"), FILTER_LEFT, PICKUP_TITLE_Y, 0x404040);
        fontRenderer.drawString(I18n.format("gui.ae2bettermagnetcard.magnet_filter.insert"), FILTER_LEFT, INSERT_TITLE_Y, 0x404040);
        fontRenderer.drawString(I18n.format("container.inventory"), FILTER_LEFT, INVENTORY_TITLE_Y, 0x404040);

        final int localX = mouseX - offsetX;
        final int localY = mouseY - offsetY;
        if (inside(localX, localY, COPY_LEFT_X, CONTROL_Y, COPY_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT)) {
            drawHoveringText(I18n.format("gui.ae2bettermagnetcard.magnet_filter.copy_insert"), localX, localY);
        } else if (inside(localX, localY, SWAP_X, CONTROL_Y, SWAP_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT)) {
            drawHoveringText(I18n.format("gui.ae2bettermagnetcard.magnet_filter.switch_tooltip"), localX, localY);
        } else if (inside(localX, localY, COPY_RIGHT_X, CONTROL_Y, COPY_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT)) {
            drawHoveringText(I18n.format("gui.ae2bettermagnetcard.magnet_filter.copy_pickup"), localX, localY);
        } else if (inside(localX, localY, UPGRADE_SLOT_X, FUZZY_UPGRADE_Y, 16, 16)
                && container.getUpgradeStack(MagnetUpgradeInventory.FUZZY_SLOT).isEmpty()) {
            drawHoveringText(I18n.format("gui.ae2bettermagnetcard.magnet_filter.fuzzy_upgrade"), localX, localY);
        } else if (inside(localX, localY, UPGRADE_SLOT_X, RANGE_UPGRADE_Y, 16, 16)
                && container.getUpgradeStack(MagnetUpgradeInventory.RANGE_SLOT).isEmpty()) {
            drawHoveringText(I18n.format("gui.ae2bettermagnetcard.magnet_filter.range_upgrade"), localX, localY);
        }
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        drawDefaultBackground();
        BackgroundGenerator.draw(MAIN_WIDTH, ySize, offsetX, offsetY);
        drawUpgradePanel(offsetX, offsetY);
        drawSlotBlocks(offsetX, offsetY);
    }

    @Override
    public List<Rectangle> getJEIExclusionArea() {
        final List<Rectangle> exclusions = new ArrayList<>();
        exclusions.add(new Rectangle(guiLeft + UPGRADE_PANEL_X, guiTop, UPGRADE_PANEL_WIDTH, UPGRADE_PANEL_HEIGHT));
        return exclusions;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(final Object ingredient) {
        mapTargetSlot.clear();

        final ItemStack itemStack = getGhostItemStack(ingredient);
        if (itemStack.isEmpty()) {
            return Collections.emptyList();
        }
        itemStack.setCount(1);

        final List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
        for (final Slot slot : inventorySlots.inventorySlots) {
            if (!(slot instanceof SlotFake)) {
                continue;
            }

            final IGhostIngredientHandler.Target<Object> target = new IGhostIngredientHandler.Target<Object>() {
                @Override
                public Rectangle getArea() {
                    return new Rectangle(guiLeft + slot.xPos, guiTop + slot.yPos, 16, 16);
                }

                @Override
                public void accept(final Object ignored) {
                    try {
                        NetworkHandler.instance().sendToServer(new PacketInventoryAction(
                                InventoryAction.PLACE_JEI_GHOST_ITEM,
                                (IJEITargetSlot) slot,
                                AEItemStack.fromItemStack(itemStack.copy())));
                    } catch (final IOException e) {
                        Main.LOGGER.warn("Failed to send magnet filter ghost item.", e);
                    }
                }
            };

            targets.add(target);
            mapTargetSlot.put(target, slot);
        }

        return targets;
    }

    @Override
    public Map<IGhostIngredientHandler.Target<?>, Object> getFakeSlotTargetMap() {
        return mapTargetSlot;
    }

    private ItemStack getGhostItemStack(final Object ingredient) {
        Object value = ingredient;
        if (value != null && "mezz.jei.bookmarks.BookmarkItem".equals(value.getClass().getName())) {
            try {
                value = value.getClass().getField("ingredient").get(value);
            } catch (final ReflectiveOperationException ignored) {
                return ItemStack.EMPTY;
            }
        }

        return value instanceof ItemStack ? ((ItemStack) value).copy() : ItemStack.EMPTY;
    }

    private void drawSlotBlocks(final int offsetX, final int offsetY) {
        if (BMCConfig.isModernGuiStyle()) {
            drawModernSlotBlocks(offsetX, offsetY);
        } else {
            drawClassicSlotBlocks(offsetX, offsetY);
        }
    }

    private void drawClassicSlotBlocks(final int offsetX, final int offsetY) {
        this.mc.getTextureManager().bindTexture(CLASSIC_SLOT_TEXTURE);
        drawSlotBlock(offsetX + FILTER_LEFT - 1, offsetY + PICKUP_FILTER_TOP - 1,
                CLASSIC_FILTER_TEXTURE_X, CLASSIC_FILTER_TEXTURE_Y, SLOT_GRID_WIDTH, FILTER_GRID_HEIGHT);
        drawSlotBlock(offsetX + FILTER_LEFT - 1, offsetY + INSERT_FILTER_TOP - 1,
                CLASSIC_FILTER_TEXTURE_X, CLASSIC_FILTER_TEXTURE_Y, SLOT_GRID_WIDTH, FILTER_GRID_HEIGHT);
        drawSlotBlock(offsetX + playerInventorySlotLeft() - 1, offsetY + PLAYER_INVENTORY_TOP - 1,
                CLASSIC_FILTER_TEXTURE_X, CLASSIC_PLAYER_TEXTURE_Y, SLOT_GRID_WIDTH, PLAYER_INVENTORY_HEIGHT);
    }

    private void drawModernSlotBlocks(final int offsetX, final int offsetY) {
        this.mc.getTextureManager().bindTexture(MODERN_SLOT_TEXTURE);
        drawSlotBlock(offsetX + FILTER_LEFT - 1, offsetY + PICKUP_FILTER_TOP - 1,
                0, 0, SLOT_GRID_WIDTH, FILTER_GRID_HEIGHT);
        drawSlotBlock(offsetX + FILTER_LEFT - 1, offsetY + INSERT_FILTER_TOP - 1,
                0, 0, SLOT_GRID_WIDTH, FILTER_GRID_HEIGHT);
        drawSlotBlock(offsetX + playerInventorySlotLeft() - 1, offsetY + PLAYER_INVENTORY_TOP - 1,
                0, 0, SLOT_GRID_WIDTH, PLAYER_INVENTORY_HEIGHT);
    }

    private void drawSlotBlock(final int x, final int y, final int textureX, final int textureY, final int width,
            final int height) {
        drawModalRectWithCustomSizedTexture(x, y, textureX, textureY, width, height, SLOT_TEXTURE_SIZE, SLOT_TEXTURE_SIZE);
    }

    private static int playerInventorySlotLeft() {
        return PLAYER_INVENTORY_LEFT + PLAYER_INVENTORY_SLOT_X_OFFSET;
    }

    private void drawUpgradePanel(final int offsetX, final int offsetY) {
        bindTexture("guis/cellworkbench.png");
        drawTexturedModalRect(offsetX + UPGRADE_PANEL_X, offsetY, 177, 0, UPGRADE_PANEL_WIDTH, UPGRADE_PANEL_HEIGHT - 7);
        drawTexturedModalRect(offsetX + UPGRADE_PANEL_X, offsetY + UPGRADE_PANEL_HEIGHT - 7,
                177, 151, UPGRADE_PANEL_WIDTH, 7);
    }

    private boolean inside(final int mouseX, final int mouseY, final int x, final int y, final int w, final int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void updateButtonLabels() {
        for (final GuiButton button : buttonList) {
            if (button.id == PICKUP_MODE) {
                button.displayString = pickupModeText();
            } else if (button.id == INSERT_MODE) {
                button.displayString = insertModeText();
            }
        }
    }

    private String pickupModeText() {
        return modeText(container.getPickupMode());
    }

    private String insertModeText() {
        return modeText(container.getInsertMode());
    }

    private String modeText(final MagnetCardFilters.FilterMode mode) {
        return mode == MagnetCardFilters.FilterMode.WHITELIST
                ? /*TextFormatting.GREEN + */ I18n.format("gui.ae2bettermagnetcard.magnet_filter.allow")
                : /*TextFormatting.RED + */ I18n.format("gui.ae2bettermagnetcard.magnet_filter.deny");
    }
}
