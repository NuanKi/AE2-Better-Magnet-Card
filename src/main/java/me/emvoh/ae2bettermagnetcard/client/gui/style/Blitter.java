package me.emvoh.ae2bettermagnetcard.client.gui.style;

import me.emvoh.ae2bettermagnetcard.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

final class Blitter {

    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private int srcX;
    private int srcY;
    private int srcWidth;
    private int srcHeight;
    private int destX;
    private int destY;

    private Blitter(final ResourceLocation texture, final int textureWidth, final int textureHeight) {
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    static Blitter texture(final String path, final int textureWidth, final int textureHeight) {
        return new Blitter(new ResourceLocation(Tags.MODID, "textures/" + path), textureWidth, textureHeight);
    }

    Blitter copy() {
        final Blitter copy = new Blitter(texture, textureWidth, textureHeight);
        copy.srcX = srcX;
        copy.srcY = srcY;
        copy.srcWidth = srcWidth;
        copy.srcHeight = srcHeight;
        copy.destX = destX;
        copy.destY = destY;
        return copy;
    }

    Blitter src(final int x, final int y, final int width, final int height) {
        srcX = x;
        srcY = y;
        srcWidth = width;
        srcHeight = height;
        return this;
    }

    Blitter srcWidth(final int width) {
        srcWidth = width;
        return this;
    }

    Blitter srcHeight(final int height) {
        srcHeight = height;
        return this;
    }

    Blitter dest(final int x, final int y) {
        destX = x;
        destY = y;
        return this;
    }

    void blit() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(destX, destY, srcX, srcY, srcWidth, srcHeight, textureWidth, textureHeight);
    }
}
