package me.emvoh.ae2bettermagnetcard.client.keybinds;

import me.emvoh.ae2bettermagnetcard.Main;
import me.emvoh.ae2bettermagnetcard.Tags;
import me.emvoh.ae2bettermagnetcard.network.PacketToggleMagnet;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public class KeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().player == null) {
            return;
        }

        if (KeyBindings.toggleStore.isPressed()) {
            Main.NETWORK.sendToServer(new PacketToggleMagnet(PacketToggleMagnet.ToggleType.STORE_TO_NETWORK));
        } else if (KeyBindings.toggleMagnet.isPressed()) {
            Main.NETWORK.sendToServer(new PacketToggleMagnet(PacketToggleMagnet.ToggleType.MAGNET));
        }
    }
}
