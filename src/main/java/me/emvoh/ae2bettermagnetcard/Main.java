package me.emvoh.ae2bettermagnetcard;

import appeng.api.AEApi;
import me.emvoh.ae2bettermagnetcard.events.MagnetStoreToMEHandler;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgrades;
import me.emvoh.ae2bettermagnetcard.network.PacketToggleMagnet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:appliedenergistics2;after:appliedenergistics2")
public class Main {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new MagnetStoreToMEHandler());

        NETWORK.registerMessage(PacketToggleMagnet.Handler.class, PacketToggleMagnet.class, 0, Side.SERVER);

        LOGGER.info("I am " + Tags.MODNAME + " at version " + Tags.VERSION);
    }


    @SubscribeEvent
    // Register recipes here (Remove if not needed)
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {

    }

    @SubscribeEvent
    // Register items here (Remove if not needed)
    public void registerItems(RegistryEvent.Register<Item> event) {

    }

    @SubscribeEvent
    // Register blocks here (Remove if not needed)
    public void registerBlocks(RegistryEvent.Register<Block> event) {

    }

    @EventHandler
    // load "Do your mod setup. Build whatever data structures you care about." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            me.emvoh.ae2bettermagnetcard.client.keybinds.KeyBindings.init();
        }
    }

    @EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        BMCUpgrades.RANGE.registerItem(AEApi.instance().definitions().materials().cardMagnet(), 1);
        BMCUpgrades.ADVANCED_RANGE.registerItem(AEApi.instance().definitions().materials().cardMagnet(), 1);
    }

    @EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
    }
}
