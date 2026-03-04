package me.emvoh.ae2bettermagnetcard.client;

import me.emvoh.ae2bettermagnetcard.Main;
import me.emvoh.ae2bettermagnetcard.Tags;
import me.emvoh.ae2bettermagnetcard.registry.ModItems;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public final class ModelRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelBakery.registerItemVariants(
                ModItems.BMC_UPGRADE,
                new ResourceLocation(Tags.MODID, "bmc_upgrade_range"),
                new ResourceLocation(Tags.MODID, "bmc_upgrade_advanced_range")
        );

        // meta 0 -> range model
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BMC_UPGRADE, 0,
                new ModelResourceLocation(Tags.MODID + ":bmc_upgrade_range", "inventory")
        );

        // meta 1 -> advanced range model
        ModelLoader.setCustomModelResourceLocation(
                ModItems.BMC_UPGRADE, 1,
                new ModelResourceLocation(Tags.MODID + ":bmc_upgrade_advanced_range", "inventory")
        );
    }
}