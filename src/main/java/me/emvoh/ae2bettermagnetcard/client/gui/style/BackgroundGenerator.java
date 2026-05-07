package me.emvoh.ae2bettermagnetcard.client.gui.style;

import me.emvoh.ae2bettermagnetcard.config.BMCConfig;

public final class BackgroundGenerator {

    private static final int BORDER = 4;
    private static final int SIZE = 256;
    private static final int TILED_SIZE = SIZE - BORDER * 2;
    private static String currentTexture;
    private static BackgroundParts currentParts;

    private BackgroundGenerator() {
    }

    public static void draw(final int width, final int height, final int left, final int top) {
        if (width < BORDER * 2 || height < BORDER * 2) {
            return;
        }

        final BackgroundParts parts = getParts();
        final int right = left + width;
        final int bottom = top + height;

        parts.topLeft.dest(left, top).blit();
        parts.topRight.dest(right - BORDER, top).blit();
        parts.bottomLeft.dest(left, bottom - BORDER).blit();
        parts.bottomRight.dest(right - BORDER, bottom - BORDER).blit();

        final int innerWidth = width - BORDER * 2;
        final int innerHeight = height - BORDER * 2;

        for (int x = 0; x < innerWidth; x += TILED_SIZE) {
            final int tileWidth = Math.min(TILED_SIZE, innerWidth - x);
            parts.topMiddle.copy().srcWidth(tileWidth).dest(left + BORDER + x, top).blit();
            parts.bottomMiddle.copy().srcWidth(tileWidth).dest(left + BORDER + x, bottom - BORDER).blit();

            for (int y = 0; y < innerHeight; y += TILED_SIZE) {
                final int tileHeight = Math.min(TILED_SIZE, innerHeight - y);
                parts.middle.copy().srcWidth(tileWidth).srcHeight(tileHeight)
                        .dest(left + BORDER + x, top + BORDER + y).blit();
            }
        }

        for (int y = 0; y < innerHeight; y += TILED_SIZE) {
            final int tileHeight = Math.min(TILED_SIZE, innerHeight - y);
            parts.left.copy().srcHeight(tileHeight).dest(left, top + BORDER + y).blit();
            parts.right.copy().srcHeight(tileHeight).dest(right - BORDER, top + BORDER + y).blit();
        }
    }

    private static BackgroundParts getParts() {
        final String texture = BMCConfig.getGuiBackgroundTexture();
        if (currentParts == null || !texture.equals(currentTexture)) {
            currentTexture = texture;
            currentParts = new BackgroundParts(texture);
        }

        return currentParts;
    }

    private static final class BackgroundParts {

        private final Blitter topLeft;
        private final Blitter topMiddle;
        private final Blitter topRight;
        private final Blitter left;
        private final Blitter middle;
        private final Blitter right;
        private final Blitter bottomLeft;
        private final Blitter bottomMiddle;
        private final Blitter bottomRight;

        private BackgroundParts(final String texture) {
            final Blitter full = Blitter.texture(texture, SIZE, SIZE);
            this.topLeft = full.copy().src(0, 0, BORDER, BORDER);
            this.topMiddle = full.copy().src(BORDER, 0, TILED_SIZE, BORDER);
            this.topRight = full.copy().src(SIZE - BORDER, 0, BORDER, BORDER);
            this.left = full.copy().src(0, BORDER, BORDER, TILED_SIZE);
            this.middle = full.copy().src(BORDER, BORDER, TILED_SIZE, TILED_SIZE);
            this.right = full.copy().src(SIZE - BORDER, BORDER, BORDER, TILED_SIZE);
            this.bottomLeft = full.copy().src(0, SIZE - BORDER, BORDER, BORDER);
            this.bottomMiddle = full.copy().src(BORDER, SIZE - BORDER, TILED_SIZE, BORDER);
            this.bottomRight = full.copy().src(SIZE - BORDER, SIZE - BORDER, BORDER, BORDER);
        }
    }
}
