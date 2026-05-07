package me.emvoh.ae2bettermagnetcard.client;

import me.emvoh.ae2bettermagnetcard.Tags;
import me.emvoh.ae2bettermagnetcard.config.BMCConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public class GuiConfigBMC extends GuiConfig {

    public GuiConfigBMC(GuiScreen parent) {
        super(parent, getElements(), Tags.MODID, false, false, Tags.MODNAME + " Settings");
    }

    private static List<IConfigElement> getElements() {
        IConfigElement root = ConfigElement.from(BMCConfig.class);

        for (IConfigElement cat : root.getChildElements()) {
            if ("client".equals(cat.getName())) {
                return cat.getChildElements();
            }
        }

        return root.getChildElements();
    }
}
