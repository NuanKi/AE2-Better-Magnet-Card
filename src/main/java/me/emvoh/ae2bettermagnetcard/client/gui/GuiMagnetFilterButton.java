package me.emvoh.ae2bettermagnetcard.client.gui;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public final class GuiMagnetFilterButton extends GuiButton implements ITooltip {

    private static final ResourceLocation STATES = new ResourceLocation("appliedenergistics2", "textures/guis/states.png");
    private static final ResourceLocation ICONS = new ResourceLocation("ae2bettermagnetcard", "textures/guis/icons.png");
    private static final int ICON_TEXTURE_WIDTH = 128;
    private static final int ICON_TEXTURE_HEIGHT = 128;

    public GuiMagnetFilterButton(final int id, final int x, final int y) {
        super(id, x, y, 18, 20, "");
    }

    @Override
    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks) {
        if (!visible) {
            return;
        }

        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(STATES);
        drawTexturedModalRect(x, y, 240, 240, 16, 16);

        mc.renderEngine.bindTexture(ICONS);
        drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 16, 16, ICON_TEXTURE_WIDTH, ICON_TEXTURE_HEIGHT);
    }

    @Override
    public String getMessage() {
        return I18n.format("gui.ae2bettermagnetcard.magnet_filter.open") + "\n"
                + I18n.format("gui.ae2bettermagnetcard.magnet_filter.open_tooltip");
    }

    @Override
    public int xPos() {
        return x;
    }

    @Override
    public int yPos() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }
}
