package me.emvoh.ae2bettermagnetcard.mixin;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IItemGroup;
import appeng.items.materials.ItemMaterial;
import appeng.items.materials.MaterialType;
import appeng.util.Platform;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(value = ItemMaterial.class, remap = false)
public abstract class MixinItemMaterialMagnetTooltip {

    @Unique
    private static final Pattern AE2BMC_NUMBER_PREFIX = Pattern.compile("(\\d+)[^\\d]");


    @Inject(method = "addCheckedInformation", at = @At(value = "INVOKE", target = "Lappeng/items/AEBaseItem;addCheckedInformation(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/util/ITooltipFlag;)V", shift = At.Shift.AFTER), cancellable = true, require = 1)
    private void ae2bettermagnetcard$replaceMagnetTooltip(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips, final CallbackInfo ci) {

        final ItemMaterial self = (ItemMaterial) (Object) this;
        final MaterialType mt = self.getTypeByStack(stack);
        if (mt != MaterialType.CARD_MAGNET) {
            return;
        }

        final NBTTagCompound c = Platform.openNbtData(stack);

        final boolean magnetEnabled = !c.hasKey("enabled") || c.getBoolean("enabled");
        final boolean storeToME = c.hasKey("storeToME") && c.getBoolean("storeToME");

        lines.add(I18n.translateToLocal("gui.tooltips.appliedenergistics2.Magnet") + ": " + (magnetEnabled ? TextFormatting.GREEN : TextFormatting.RED) + I18n.translateToLocal(magnetEnabled ? "gui.tooltips.appliedenergistics2.MagnetEnabled" : "gui.tooltips.appliedenergistics2.MagnetDisabled") + TextFormatting.RESET);

        lines.add(I18n.translateToLocal("gui.tooltips.appliedenergistics2.StoreToNetwork") + ": " + (storeToME ? TextFormatting.GREEN : TextFormatting.RED) + I18n.translateToLocal(storeToME ? "gui.tooltips.appliedenergistics2.StoreToNetworkEnabled" : "gui.tooltips.appliedenergistics2.StoreToNetworkDisabled") + TextFormatting.RESET);

        lines.add("");

        final boolean shiftDown = GuiScreen.isShiftKeyDown();
        final boolean showCompat = shiftDown && Keyboard.isKeyDown(Keyboard.KEY_M);

        if (!shiftDown) {
            final String leftShift = TextFormatting.BLUE + "Left Shift" + TextFormatting.GRAY;
            final String shiftM = TextFormatting.AQUA + "Shift + M" + TextFormatting.GRAY;

            lines.add(TextFormatting.GRAY + String.format(I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.hold_shift"), leftShift));

            lines.add(TextFormatting.GRAY + String.format(I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.hold_shift_m"), shiftM));

            ci.cancel();
            return;
        }

        if (showCompat) {
            lines.add(TextFormatting.GRAY + I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.compatible"));

            final Upgrades u = self.getType(stack);
            if (u != null) {
                final List<String> textList = new ArrayList<>();
                for (final Map.Entry<ItemStack, Integer> e : u.getSupported().entrySet()) {
                    final String name = ae2bettermagnetcard$formatSupportedName(u, e.getKey(), e.getValue());
                    if (name != null && !textList.contains(name)) {
                        textList.add(name);
                    }
                }

                Collections.sort(textList, (o1, o2) -> {
                    try {
                        final Matcher a = AE2BMC_NUMBER_PREFIX.matcher(o1);
                        final Matcher b = AE2BMC_NUMBER_PREFIX.matcher(o2);
                        if (a.find() && b.find()) {
                            final int ia = Integer.parseInt(a.group(1));
                            final int ib = Integer.parseInt(b.group(1));
                            return Integer.compare(ia, ib);
                        }
                    } catch (final Throwable ignored) {
                    }
                    return o1.compareTo(o2);
                });

                for (final String name : textList) {
                    lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + name);
                }
            }

            ci.cancel();
            return;
        }

        // Shift held, but not Shift+M: show controls + configuration
        lines.add(TextFormatting.GRAY + I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.controls") + ":");
        lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.usage"));
        lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.store"));
        lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.shift"));
        lines.add("");

        lines.add(TextFormatting.GRAY + I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.configuration") + ":");
        lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.partition"));

        ci.cancel();
    }

    @Unique
    private static String ae2bettermagnetcard$formatSupportedName(final Upgrades u, final ItemStack key, final int limit) {
        String name = null;

        if (key.getItem() instanceof IItemGroup) {
            final IItemGroup ig = (IItemGroup) key.getItem();
            final String str = ig.getUnlocalizedGroupName(u.getSupported().keySet(), key);
            if (str != null) {
                name = Platform.gui_localize(str) + (limit > 1 ? " (" + limit + ')' : "");
            }
        }

        if (name == null) {
            name = key.getDisplayName() + (limit > 1 ? " (" + limit + ')' : "");
        }

        return name;
    }
}