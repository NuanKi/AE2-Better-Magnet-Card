package me.emvoh.ae2bettermagnetcard.client.gui.widgets;

import me.emvoh.ae2bettermagnetcard.Tags;
import me.emvoh.ae2bettermagnetcard.config.BMCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class AE2Button extends GuiButton {

    private static final int TEXTURE_WIDTH = 200;
    private static final int TEXTURE_HEIGHT = 20;
    private static final int BORDER_WIDTH = 4;

    private static final int DISABLED_TEXT_COLOR = 0x413f54;
    private static final int HOVERED_TEXT_COLOR = 0xFFFFBE;
    private static final int NORMAL_TEXT_COLOR = 0xf2f2f2;

    private static final int TEXT_SHADOW_COLOR = 0x000000;

    private static final int TEXT_SHADOW_OFFSET = 1;

    private final PressAction onPress;

    public AE2Button(final int buttonId, final int x, final int y, final int width, final int height,
            final String buttonText) {
        this(buttonId, x, y, width, height, buttonText, null);
    }

    public AE2Button(final int buttonId, final int x, final int y, final int width, final int height, final String buttonText,
            final PressAction onPress) {
        super(buttonId, x, y, width, height, buttonText);
        this.onPress = onPress;
    }

    public AE2Button(final int x, final int y, final int width, final int height, final String buttonText,
            final PressAction onPress) {
        this(-1, x, y, width, height, buttonText, onPress);
    }

    public AE2Button(final String buttonText, final PressAction onPress) {
        this(0, 0, 0, 0, buttonText, onPress);
    }

    private static ResourceLocation texture(final String name) {
        return new ResourceLocation(Tags.MODID, "textures/" + BMCConfig.getGuiButtonTexture(name));
    }

    @Override
    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks) {
        if (!this.visible) {
            return;
        }

        if (this.width <= 0 || this.height <= 0) {
            return;
        }

        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
                && mouseY < this.y + this.height;

        mc.getTextureManager().bindTexture(this.getTexture());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        this.drawButtonTexture(this.x, this.y, this.width, this.height);
        this.mouseDragged(mc, mouseX, mouseY);
        this.renderButtonText(mc);
    }

    @Override
    public boolean mousePressed(final Minecraft mc, final int mouseX, final int mouseY) {
        final boolean pressed = super.mousePressed(mc, mouseX, mouseY);

        if (pressed && this.onPress != null) {
            this.onPress.onPress(this);
        }

        return pressed;
    }

    public void setMessage(final String message) {
        this.displayString = message;
    }

    public String getMessage() {
        return this.displayString;
    }

    protected void renderButtonText(final Minecraft mc) {
        final int color;

        if (!this.enabled) {
            color = DISABLED_TEXT_COLOR;
        } else if (this.hovered) {
            color = HOVERED_TEXT_COLOR;
        } else {
            color = NORMAL_TEXT_COLOR;
        }

        renderButtonText(mc, this.displayString, this.x + 4, this.y, this.x + this.width - 4, this.y + this.height, color,
                TEXT_SHADOW_COLOR, TEXT_SHADOW_OFFSET);
    }

    public static void renderButtonText(final Minecraft mc, final String text, final int left, final int top, final int right,
            final int bottom, final int color, final int shadowColor, final int shadowOffset) {
        final FontRenderer font = mc.fontRenderer;
        final int availableWidth = Math.max(0, right - left);
        final String visibleText = fitText(font, text == null ? "" : text, availableWidth);
        final int textWidth = font.getStringWidth(visibleText);
        final int centerX = (left + right) / 2;
        final int textX = MathHelper.clamp(centerX - textWidth / 2, left, Math.max(left, right - textWidth));
        final int textY = top + (bottom - top - font.FONT_HEIGHT) / 2;

        if (shadowOffset != 0) {
            font.drawString(visibleText, textX + shadowOffset, textY + shadowOffset, shadowColor);
        }

        font.drawString(visibleText, textX, textY, color);
    }

    private static String fitText(final FontRenderer font, final String text, final int width) {
        if (font.getStringWidth(text) <= width) {
            return text;
        }

        final String ellipsis = "...";
        final int ellipsisWidth = font.getStringWidth(ellipsis);

        if (width <= ellipsisWidth) {
            return font.trimStringToWidth(text, width);
        }

        return font.trimStringToWidth(text, width - ellipsisWidth) + ellipsis;
    }

    private ResourceLocation getTexture() {
        if (!this.enabled) {
            return texture("button_disabled");
        }

        return texture(this.hovered ? "button_highlighted" : "button");
    }

    private void drawButtonTexture(final int x, final int y, final int width, final int height) {
        final int centerWidth = Math.max(0, width - BORDER_WIDTH * 2);

        Gui.drawScaledCustomSizeModalRect(x, y, 0.0F, 0.0F, BORDER_WIDTH, TEXTURE_HEIGHT, BORDER_WIDTH, height,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (centerWidth > 0) {
            Gui.drawScaledCustomSizeModalRect(x + BORDER_WIDTH, y, BORDER_WIDTH, 0.0F,
                    TEXTURE_WIDTH - BORDER_WIDTH * 2, TEXTURE_HEIGHT, centerWidth, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        Gui.drawScaledCustomSizeModalRect(x + width - BORDER_WIDTH, y, TEXTURE_WIDTH - BORDER_WIDTH, 0.0F, BORDER_WIDTH,
                TEXTURE_HEIGHT, BORDER_WIDTH, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public interface PressAction {

        void onPress(AE2Button button);
    }
}
