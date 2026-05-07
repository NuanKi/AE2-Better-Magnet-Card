package me.emvoh.ae2bettermagnetcard.mixin.accessors;

import appeng.items.contents.CellConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CellConfig.class, remap = false)
public interface AccessorCellConfig {
    @Accessor("is")
    ItemStack ae2bmc$getHostStack();
}
