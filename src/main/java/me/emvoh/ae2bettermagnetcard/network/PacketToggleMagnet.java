package me.emvoh.ae2bettermagnetcard.network;

import io.netty.buffer.ByteBuf;
import appeng.util.Platform;
import me.emvoh.ae2bettermagnetcard.events.MagnetStoreToMEHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemStackHandler;

public class PacketToggleMagnet implements IMessage {

    public enum ToggleType {
        MAGNET, STORE_TO_NETWORK
    }

    private ToggleType type;

    public PacketToggleMagnet() {
    }

    public PacketToggleMagnet(ToggleType type) {
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = ToggleType.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(type.ordinal());
    }

    public static class Handler implements IMessageHandler<PacketToggleMagnet, IMessage> {

        @Override
        public IMessage onMessage(PacketToggleMagnet message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                MagnetStoreToMEHandler handler = new MagnetStoreToMEHandler();
                MagnetStoreToMEHandler.TerminalAndMagnet result = handler.findAnyTerminalWithMagnet(player);

                if (result != null) {
                    ItemStack terminal = result.terminalStack;
                    NBTTagCompound terminalTag = Platform.openNbtData(terminal);
                    NBTTagCompound upgradesNbt = terminalTag.getCompoundTag("upgrades");
                    
                    ItemStackHandler upgradesHandler = new ItemStackHandler(0);
                    upgradesHandler.deserializeNBT(upgradesNbt);
                    
                    ItemStack magnetCard = ItemStack.EMPTY;
                    for (int s = 0; s < upgradesHandler.getSlots(); s++) {
                        ItemStack card = upgradesHandler.getStackInSlot(s);
                        if (!card.isEmpty() && appeng.api.AEApi.instance().definitions().materials().cardMagnet().isSameAs(card)) {
                            magnetCard = card;
                            break;
                        }
                    }

                    if (!magnetCard.isEmpty()) {
                        NBTTagCompound tag = magnetCard.getTagCompound();
                        if (tag == null) {
                            tag = new NBTTagCompound();
                            magnetCard.setTagCompound(tag);
                        }
                        if (message.type == ToggleType.MAGNET) {
                            boolean currentState = !tag.hasKey("enabled") || tag.getBoolean("enabled");
                            tag.setBoolean("enabled", !currentState);

                            TextComponentTranslation status = new TextComponentTranslation(!currentState ? "gui.tooltips.appliedenergistics2.MagnetEnabled" : "gui.tooltips.appliedenergistics2.MagnetDisabled");
                            status.getStyle().setColor(!currentState ? TextFormatting.GREEN : TextFormatting.RED);

                            player.sendStatusMessage(new TextComponentTranslation(
                                    "gui.tooltips.appliedenergistics2.Magnet")
                                    .appendText(": ")
                                    .appendSibling(status), true);
                        } else {
                            boolean currentState = tag.hasKey("storeToME") && tag.getBoolean("storeToME");
                            tag.setBoolean("storeToME", !currentState);

                            TextComponentTranslation status = new TextComponentTranslation(!currentState ? "gui.tooltips.appliedenergistics2.StoreToNetworkEnabled" : "gui.tooltips.appliedenergistics2.StoreToNetworkDisabled");
                            status.getStyle().setColor(!currentState ? TextFormatting.GREEN : TextFormatting.RED);

                            player.sendStatusMessage(new TextComponentTranslation(
                                    "gui.tooltips.appliedenergistics2.StoreToNetwork")
                                    .appendText(": ")
                                    .appendSibling(status), true);
                        }
                        
                        terminalTag.setTag("upgrades", upgradesHandler.serializeNBT());
                    }
                }
            });
            return null;
        }
    }
}
