package me.emvoh.ae2bettermagnetcard.client;

import me.emvoh.ae2bettermagnetcard.Tags;
import me.emvoh.ae2bettermagnetcard.config.BMCConfig;
import me.emvoh.ae2bettermagnetcard.registry.ModItems;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgradeItemType;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public final class ModelRegistration {

    private static final String RANGE_MODEL = "bmc_upgrade_range";
    private static final String ADVANCED_RANGE_MODEL = "bmc_upgrade_advanced_range";

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelBakery.registerItemVariants(
                ModItems.BMC_UPGRADE,
                new ResourceLocation(Tags.MODID, RANGE_MODEL),
                new ResourceLocation(Tags.MODID, RANGE_MODEL + "_old"),
                new ResourceLocation(Tags.MODID, ADVANCED_RANGE_MODEL),
                new ResourceLocation(Tags.MODID, ADVANCED_RANGE_MODEL + "_old")
        );

        ModelLoader.setCustomMeshDefinition(ModItems.BMC_UPGRADE, ModelRegistration::getUpgradeModel);
    }

    private static ModelResourceLocation getUpgradeModel(final ItemStack stack) {
        final String suffix = BMCConfig.isModernGuiStyle() ? "" : "_old";
        final String model;

        switch (BMCUpgradeItemType.fromMeta(stack.getItemDamage())) {
            case ADVANCED_RANGE:
                model = ADVANCED_RANGE_MODEL;
                break;
            case RANGE:
            default:
                model = RANGE_MODEL;
                break;
        }

        return new ModelResourceLocation(Tags.MODID + ":" + model + suffix, "inventory");
    }
}