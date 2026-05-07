package me.emvoh.ae2bettermagnetcard.mixin.accessors;

import appeng.container.implementations.ContainerMEPortableTerminal;
import appeng.container.slot.SlotRestrictedInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ContainerMEPortableTerminal.class, remap = false)
public interface AccessorContainerMEPortableTerminal {
    @Accessor("magnetSlot")
    SlotRestrictedInput ae2bmc$getMagnetSlot();
}
