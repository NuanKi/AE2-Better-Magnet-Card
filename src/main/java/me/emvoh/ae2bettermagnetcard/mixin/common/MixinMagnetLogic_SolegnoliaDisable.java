package me.emvoh.ae2bettermagnetcard.mixin.common;

import appeng.items.tools.powered.ToolWirelessTerminal;
import me.emvoh.ae2bettermagnetcard.integration.botania.BotaniaSolegnoliaCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = ToolWirelessTerminal.class, remap = false)
public abstract class MixinMagnetLogic_SolegnoliaDisable {

    @Shadow
    private void teleportItem(EntityItem i, Entity entityIn) {
    }

    // If player is inside Solegnolia range, disable magnet logic entirely
    @Inject(method = "magnetLogic(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lappeng/util/Platform;openNbtData(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/NBTTagCompound;", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true, require = 1)
    private void ae2bmc$disableMagnetInSolegnolia(ItemStack stack, World worldIn, Entity entityIn, CallbackInfo ci) {
        if (BotaniaSolegnoliaCompat.hasSolegnoliaAround(entityIn)) {
            ci.cancel();
        }
    }

    // If the item is inside Solegnolia range, do not teleport it
    @Redirect(method = "magnetLogic(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lappeng/items/tools/powered/ToolWirelessTerminal;teleportItem(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/entity/Entity;)V"), require = 1)
    private void ae2bmc$blockTeleportIfItemInSolegnolia(ToolWirelessTerminal self, EntityItem item, Entity entityIn) {
        if (BotaniaSolegnoliaCompat.hasSolegnoliaAround(item)) {
            return;
        }
        this.teleportItem(item, entityIn);
    }
}