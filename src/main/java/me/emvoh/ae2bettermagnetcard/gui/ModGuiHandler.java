package me.emvoh.ae2bettermagnetcard.gui;

import me.emvoh.ae2bettermagnetcard.client.gui.GuiMagnetFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public final class ModGuiHandler implements IGuiHandler {

    public static final int MAGNET_FILTER = 0;

    @Nullable
    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        if (id != MAGNET_FILTER) {
            return null;
        }

        final MagnetFilterHost host = new MagnetFilterHost(player, x, y != 0);
        if (!host.isValid()) {
            return null;
        }

        return new ContainerMagnetFilter(player.inventory, host);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        if (id != MAGNET_FILTER) {
            return null;
        }

        final MagnetFilterHost host = new MagnetFilterHost(player, x, y != 0);
        if (!host.isValid()) {
            return null;
        }

        return new GuiMagnetFilter(new ContainerMagnetFilter(player.inventory, host));
    }
}
