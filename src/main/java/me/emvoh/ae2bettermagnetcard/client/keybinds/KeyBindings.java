package me.emvoh.ae2bettermagnetcard.client.keybinds;

import me.emvoh.ae2bettermagnetcard.Tags;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {

    public static KeyBinding toggleStore;
    public static KeyBinding toggleMagnet;

    public static void init() {
        toggleStore = new KeyBinding("key.ae2bettermagnetcard.toggle_store", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, Keyboard.KEY_BACKSLASH, "key.category." + Tags.MODID);
        toggleMagnet = new KeyBinding("key.ae2bettermagnetcard.toggle_magnet", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, Keyboard.KEY_TAB, "key.category." + Tags.MODID);

        ClientRegistry.registerKeyBinding(toggleStore);
        ClientRegistry.registerKeyBinding(toggleMagnet);
    }
}
