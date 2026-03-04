package me.emvoh.ae2bettermagnetcard.mixin.client;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IItemGroup;
import appeng.items.materials.ItemMaterial;
import appeng.items.materials.MaterialType;
import appeng.util.Platform;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgradeItemType;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgrades;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
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

    /**
     * NOTE: add this lang key:
     * item.ae2bettermagnetcard.magnet.available_upgrades=Available upgrades
     */
    @Inject(
            method = "addCheckedInformation",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/items/AEBaseItem;addCheckedInformation(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/util/ITooltipFlag;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true,
            require = 1
    )
    private void ae2bettermagnetcard$replaceMagnetTooltip(final ItemStack stack,
                                                          final World world,
                                                          final List<String> lines,
                                                          final ITooltipFlag advancedTooltips,
                                                          final CallbackInfo ci) {

        final ItemMaterial self = (ItemMaterial) (Object) this;
        final MaterialType mt = self.getTypeByStack(stack);
        if (mt != MaterialType.CARD_MAGNET) {
            return;
        }

        final NBTTagCompound c = Platform.openNbtData(stack);

        final boolean magnetEnabled = !c.hasKey("enabled") || c.getBoolean("enabled");
        final boolean storeToME = c.hasKey("storeToME") && c.getBoolean("storeToME");

        lines.add(
                I18n.translateToLocal("gui.tooltips.appliedenergistics2.Magnet") + ": "
                        + (magnetEnabled ? TextFormatting.GREEN : TextFormatting.RED)
                        + I18n.translateToLocal(magnetEnabled
                        ? "gui.tooltips.appliedenergistics2.MagnetEnabled"
                        : "gui.tooltips.appliedenergistics2.MagnetDisabled")
                        + TextFormatting.RESET
        );

        lines.add(
                I18n.translateToLocal("gui.tooltips.appliedenergistics2.StoreToNetwork") + ": "
                        + (storeToME ? TextFormatting.GREEN : TextFormatting.RED)
                        + I18n.translateToLocal(storeToME
                        ? "gui.tooltips.appliedenergistics2.StoreToNetworkEnabled"
                        : "gui.tooltips.appliedenergistics2.StoreToNetworkDisabled")
                        + TextFormatting.RESET
        );

        lines.add("");

        final boolean shiftDown = GuiScreen.isShiftKeyDown();
        final boolean showCompat = shiftDown && Keyboard.isKeyDown(Keyboard.KEY_M);

        if (!shiftDown) {
            final String leftShift = TextFormatting.BLUE + "Left Shift" + TextFormatting.GRAY;
            final String shiftM = TextFormatting.AQUA + "Shift + M" + TextFormatting.GRAY;

            lines.add(TextFormatting.GRAY + String.format(
                    I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.hold_shift"),
                    leftShift
            ));

            lines.add(TextFormatting.GRAY + String.format(
                    I18n.translateToLocal("item.appliedenergistics2.material.card_magnet.hold_shift_m"),
                    shiftM
            ));

            ci.cancel();
            return;
        }

        if (showCompat) {
            // This is the original “compatible terminals” view (Upgrades.MAGNET supported hosts)
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

                Collections.sort(textList, ae2bettermagnetcard$numberAwareComparator());

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

        lines.add("");
        lines.add(TextFormatting.GRAY + I18n.translateToLocal("item.ae2bettermagnetcard.magnet.available_upgrades"));

        final List<String> available = ae2bettermagnetcard$getAvailableUpgradesForMagnet(stack);
        if (available.isEmpty()) {
            lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + I18n.translateToLocal("gui.tooltips.appliedenergistics2.Disabled"));
        } else {
            for (final String s : available) {
                lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + s);
            }
        }

        ci.cancel();
    }

    /**
     * Builds the "Available upgrades" list automatically from:
     *  - AE2 Upgrades registrations (Upgrades.X.registerItem(cardMagnet, n))
     *  - BMC Upgrades registrations (BMCUpgrades.X.registerItem(cardMagnet, n))
     *
     * Output format:
     *  - "Inverter Card (1)"
     *  - "Advanced Range Upgrade (1)"
     */
    @Unique
    private static List<String> ae2bettermagnetcard$getAvailableUpgradesForMagnet(final ItemStack magnetCardStack) {
        final List<String> out = new ArrayList<>();

        for (final Upgrades u : Upgrades.values()) {
            if (u == Upgrades.MAGNET) {
                continue;
            }

            final int limit = ae2bettermagnetcard$getMaxSupported(u.getSupported(), magnetCardStack);
            if (limit <= 0) {
                continue;
            }

            final String cardName = ae2bettermagnetcard$getAe2UpgradeCardDisplayName(u);
            if (cardName != null && !cardName.isEmpty()) {
                out.add(cardName + " (" + limit + ")");
            } else {
                // fallback
                out.add(u.name() + " (" + limit + ")");
            }
        }

        final Item bmcUpgradeItem = Item.getByNameOrId("ae2bettermagnetcard:bmc_upgrade");

        for (final BMCUpgradeItemType t : BMCUpgradeItemType.values()) {
            if (t == BMCUpgradeItemType.INVALID_TYPE || t.getMeta() < 0) {
                continue;
            }

            final BMCUpgrades u = t.getUpgrade();
            if (u == null) {
                continue;
            }

            final int limit = ae2bettermagnetcard$getMaxSupported(u.getSupported(), magnetCardStack);
            if (limit <= 0) {
                continue;
            }

            String name = null;
            if (bmcUpgradeItem != null) {
                name = new ItemStack(bmcUpgradeItem, 1, t.getMeta()).getDisplayName();
            }
            if (name == null || name.isEmpty()) {
                name = t.name();
            }

            out.add(name + " (" + limit + ")");
        }

        Collections.sort(out, ae2bettermagnetcard$numberAwareComparator());
        return out;
    }

    @Unique
    private static int ae2bettermagnetcard$getMaxSupported(final Map<ItemStack, Integer> supported, final ItemStack host) {
        for (final Map.Entry<ItemStack, Integer> e : supported.entrySet()) {
            if (ItemStack.areItemsEqual(e.getKey(), host)) {
                return e.getValue();
            }
        }
        return 0;
    }

    @Unique
    private static String ae2bettermagnetcard$getAe2UpgradeCardDisplayName(final Upgrades u) {
        final IAppEngApi api = AEApi.instance();
        ItemStack card = ItemStack.EMPTY;

        switch (u) {
            case CAPACITY:
                card = api.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case FUZZY:
                card = api.definitions().materials().cardFuzzy().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case REDSTONE:
                card = api.definitions().materials().cardRedstone().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case SPEED:
                card = api.definitions().materials().cardSpeed().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case INVERTER:
                card = api.definitions().materials().cardInverter().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case CRAFTING:
                card = api.definitions().materials().cardCrafting().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case PATTERN_EXPANSION:
                card = api.definitions().materials().cardPatternExpansion().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case QUANTUM_LINK:
                card = api.definitions().materials().cardQuantumLink().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            case STICKY:
                card = api.definitions().materials().cardSticky().maybeStack(1).orElse(ItemStack.EMPTY);
                break;
            default:
                break;
        }

        return card.isEmpty() ? null : card.getDisplayName();
    }

    @Unique
    private static java.util.Comparator<String> ae2bettermagnetcard$numberAwareComparator() {
        return (o1, o2) -> {
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
        };
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