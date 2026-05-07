package me.emvoh.ae2bettermagnetcard.network;

import io.netty.buffer.ByteBuf;
import me.emvoh.ae2bettermagnetcard.Main;
import me.emvoh.ae2bettermagnetcard.events.MagnetStoreToMEHandler;
import me.emvoh.ae2bettermagnetcard.gui.MagnetFilterHost;
import me.emvoh.ae2bettermagnetcard.gui.ModGuiHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketOpenMagnetFilterGui implements IMessage {

    private int terminalSlot = -1;
    private boolean baubleSlot;

    public PacketOpenMagnetFilterGui() {
    }

    public PacketOpenMagnetFilterGui(final int terminalSlot, final boolean baubleSlot) {
        this.terminalSlot = terminalSlot;
        this.baubleSlot = baubleSlot;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        terminalSlot = buf.readInt();
        baubleSlot = buf.readBoolean();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(terminalSlot);
        buf.writeBoolean(baubleSlot);
    }

    public static final class Handler implements IMessageHandler<PacketOpenMagnetFilterGui, IMessage> {

        @Override
        public IMessage onMessage(final PacketOpenMagnetFilterGui message, final MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                int terminalSlot = message.terminalSlot;
                boolean baubleSlot = message.baubleSlot;

                if (terminalSlot < 0) {
                    final MagnetStoreToMEHandler.TerminalAndMagnet result =
                            new MagnetStoreToMEHandler().findAnyTerminalWithMagnet(player);
                    if (result == null) {
                        return;
                    }

                    terminalSlot = result.slotIdx;
                    baubleSlot = result.isBauble;
                }

                final MagnetFilterHost host = new MagnetFilterHost(player, terminalSlot, baubleSlot);
                if (host.isValid()) {
                    player.openGui(Main.INSTANCE, ModGuiHandler.MAGNET_FILTER, player.world,
                            terminalSlot, baubleSlot ? 1 : 0, 0);
                }
            });
            return null;
        }
    }
}
