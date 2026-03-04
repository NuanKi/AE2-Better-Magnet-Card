package me.emvoh.ae2bettermagnetcard.mixin.accessors;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AppEngSlot.class, remap = false)
public interface AccessorAppEngSlot {

    @Accessor("itemHandler")
    IItemHandler ae2bmc$getItemHandler();

    @Accessor("index")
    int ae2bmc$getIndex();

    @Accessor("myContainer")
    AEBaseContainer ae2bmc$getMyContainer();
}