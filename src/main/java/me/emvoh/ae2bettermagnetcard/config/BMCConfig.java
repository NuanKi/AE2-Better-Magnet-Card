package me.emvoh.ae2bettermagnetcard.config;

import me.emvoh.ae2bettermagnetcard.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MODID, name = Tags.MODID)
public final class BMCConfig {

    @Config.Name("client")
    public static final Client client = new Client();

    private BMCConfig() {
    }

    public static String getGuiBackgroundTexture() {
        return isModernGuiStyle() ? "guis/background.png" : "guis/background_old.png";
    }

    public static String getGuiButtonTexture(final String name) {
        final String suffix = isModernGuiStyle() ? "" : "_old";
        return "guis/sprites/" + name + suffix + ".png";
    }

    public static boolean isModernGuiStyle() {
        return client.guiBackgroundStyle == GuiBackgroundStyle.MODERN;
    }

    public enum GuiBackgroundStyle {
        CLASSIC,
        MODERN
    }

    public static final class Client {

        @Config.Name("gui_background_style")
        @Config.LangKey("ae2bettermagnetcard.config.gui_background_style")
        @Config.Comment({
                "GUI background style for the magnet filter screen.",
                "CLASSIC uses the AE2 1.12.2-style background.",
                "MODERN uses the newer AE2-style background texture."
        })
        public GuiBackgroundStyle guiBackgroundStyle = GuiBackgroundStyle.CLASSIC;
    }
}
