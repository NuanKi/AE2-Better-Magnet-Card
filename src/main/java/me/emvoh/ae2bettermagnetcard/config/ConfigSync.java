package me.emvoh.ae2bettermagnetcard.config;

import me.emvoh.ae2bettermagnetcard.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class ConfigSync {

    private ConfigSync() {
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (Tags.MODID.equals(e.getModID())) {
            ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
        }
    }
}
