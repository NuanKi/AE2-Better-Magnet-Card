package me.emvoh.ae2bettermagnetcard.network;

import io.netty.buffer.ByteBuf;
import me.emvoh.ae2bettermagnetcard.gui.ContainerMagnetFilter;
import me.emvoh.ae2bettermagnetcard.gui.MagnetFilterHost;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketMagnetFilterAction implements IMessage {

    public enum Action {
        TOGGLE_PICKUP,
        TOGGLE_INSERT,
        COPY_PICKUP_TO_INSERT,
        COPY_INSERT_TO_PICKUP,
        SWAP
    }

    private int terminalSlot;
    private boolean baubleSlot;
    private Action action;

    public PacketMagnetFilterAction() {
    }

    public PacketMagnetFilterAction(final int terminalSlot, final boolean baubleSlot, final Action action) {
        this.terminalSlot = terminalSlot;
        this.baubleSlot = baubleSlot;
        this.action = action;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        terminalSlot = buf.readInt();
        baubleSlot = buf.readBoolean();
        final int actionId = buf.readInt();
        final Action[] actions = Action.values();
        action = actionId >= 0 && actionId < actions.length ? actions[actionId] : null;
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(terminalSlot);
        buf.writeBoolean(baubleSlot);
        buf.writeInt(action.ordinal());
    }

    public static final class Handler implements IMessageHandler<PacketMagnetFilterAction, IMessage> {

        @Override
        public IMessage onMessage(final PacketMagnetFilterAction message, final MessageContext ctx) {
            if (message.action == null) {
                return null;
            }

            final EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerMagnetFilter) {
                    final ContainerMagnetFilter container = (ContainerMagnetFilter) player.openContainer;
                    if (container.getTerminalSlot() == message.terminalSlot
                            && container.isBaubleSlot() == message.baubleSlot) {
                        container.applyAction(message.action);
                        container.detectAndSendChanges();
                        return;
                    }
                }

                final MagnetFilterHost host = new MagnetFilterHost(player, message.terminalSlot, message.baubleSlot);
                if (!host.isValid()) {
                    return;
                }

                final ContainerMagnetFilter transientContainer = new ContainerMagnetFilter(player.inventory, host);
                transientContainer.applyAction(message.action);
            });
            return null;
        }
    }
}
