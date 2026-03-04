package me.emvoh.ae2bettermagnetcard.items;

import me.emvoh.ae2bettermagnetcard.api.IBMCUpgradeModule;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgradeItemType;
import me.emvoh.ae2bettermagnetcard.utils.enums.BMCUpgrades;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemBMCUpgrade extends Item implements IBMCUpgradeModule {

    public static ItemBMCUpgrade instance;
    private final Map<Integer, BMCUpgradeItemType> dmgToType = new HashMap<>();

    public ItemBMCUpgrade() {
        setHasSubtypes(true);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.MISC);
        setRegistryName("ae2bettermagnetcard", "bmc_upgrade");
        setTranslationKey("ae2bettermagnetcard.bmc_upgrade");

        instance = this;

        for (BMCUpgradeItemType t : BMCUpgradeItemType.values()) {
            if (t != BMCUpgradeItemType.INVALID_TYPE && t.getMeta() >= 0) {
                registerType(t);
            }
        }
    }

    private void registerType(BMCUpgradeItemType type) {
        if (dmgToType.containsKey(type.getMeta())) {
            throw new IllegalStateException("Meta overlap detected for BMCUpgradeItemType: " + type);
        }
        dmgToType.put(type.getMeta(), type);
    }

    public BMCUpgradeItemType getTypeByStack(final ItemStack is) {
        final BMCUpgradeItemType type = dmgToType.get(is.getItemDamage());
        return type != null ? type : BMCUpgradeItemType.INVALID_TYPE;
    }

    @Override
    public BMCUpgrades getType(final ItemStack stack) {
        switch (this.getTypeByStack(stack)) {
            case RANGE:
                return BMCUpgrades.RANGE;
            case ADVANCED_RANGE:
                return BMCUpgrades.ADVANCED_RANGE;
            default:
                return null;
        }
    }

    @Override
    @Nonnull
    public String getTranslationKey(final ItemStack stack) {
        return "item.ae2bettermagnetcard.bmc_upgrade." + this.nameOf(stack).toLowerCase();
    }

    private String nameOf(final ItemStack is) {
        if (is.isEmpty()) return "null";
        final BMCUpgradeItemType t = this.getTypeByStack(is);
        if (t == null || t == BMCUpgradeItemType.INVALID_TYPE) return "null";
        return t.name();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        for (BMCUpgradeItemType t : BMCUpgradeItemType.values()) {
            if (t != BMCUpgradeItemType.INVALID_TYPE && t.getMeta() >= 0) {
                items.add(t.stack(this, 1));
            }
        }
    }

    public void registerOredicts() {
        for (BMCUpgradeItemType t : dmgToType.values()) {
            if (t.getOreName() != null && !t.getOreName().isEmpty()) {
                OreDictionary.registerOre(t.getOreName(), t.stack(this, 1));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World worldIn, final List<String> lines, final ITooltipFlag flagIn) {
        final BMCUpgradeItemType type = getTypeByStack(stack);
        if (type == null || type == BMCUpgradeItemType.INVALID_TYPE) return;

        final String suffix = type.name().toLowerCase();

        // Per-upgrade short line
        lines.add(TextFormatting.DARK_GRAY + I18n.translateToLocal("item.ae2bettermagnetcard.bmc_upgrade." + suffix + ".effect_short"));
        lines.add("");

        final boolean shiftDown = GuiScreen.isShiftKeyDown();
        final boolean showCompat = shiftDown && Keyboard.isKeyDown(Keyboard.KEY_M);

        // Global prompts (same for all upgrades)
        if (!shiftDown) {
            final String leftShift = TextFormatting.BLUE + "Left Shift" + TextFormatting.GRAY;
            final String shiftM = TextFormatting.AQUA + "Shift + M" + TextFormatting.GRAY;

            lines.add(TextFormatting.GRAY + String.format(I18n.translateToLocal("item.ae2bettermagnetcard.hold_shift"), leftShift));
            lines.add(TextFormatting.GRAY + String.format(I18n.translateToLocal("item.ae2bettermagnetcard.hold_shift_m"), shiftM));
            return;
        }

        // Shift+M: supported items/blocks (global header, per-upgrade content)
        if (showCompat) {
            lines.add(TextFormatting.GRAY + I18n.translateToLocal("item.ae2bettermagnetcard.compatible_with"));

            final BMCUpgrades u = type.getUpgrade();
            if (u != null) {
                for (Map.Entry<ItemStack, Integer> e : u.getSupported().entrySet()) {
                    final ItemStack host = e.getKey();
                    final int limit = e.getValue();
                    final String name = host.getDisplayName() + (limit > 1 ? " (" + limit + ")" : "");
                    lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + name);
                }
            }
            return;
        }

        // Shift: details (global header, per-upgrade bullets)
        lines.add(TextFormatting.GRAY + I18n.translateToLocal("item.ae2bettermagnetcard.bmc_upgrade.details") + ":");

        switch (type) {
            case RANGE:
            case ADVANCED_RANGE: {
                final int base = 5;
                final int mult = type.getRangeMultiplier();

                lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + String.format(I18n.translateToLocal("item.ae2bettermagnetcard.bmc_upgrade." + suffix + ".detail_range"), base, base * mult));
                break;
            }

            default:
                // Fallback for when i forget to add details for a new upgrade type
                lines.add(TextFormatting.DARK_GRAY + "• " + TextFormatting.GRAY + I18n.translateToLocal("item.ae2bettermagnetcard.bmc_upgrade.generic_no_details"));
                break;
        }
    }
}