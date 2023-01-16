package kgg.cloudstructure.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ReplyPacket extends Packet{
    protected Component title;
    protected Component info;

    public ReplyPacket(Component title, Component info) {
        this.title = title;
        this.info = info;
    }

    public ReplyPacket(FriendlyByteBuf buf) {
        title = buf.readComponent();
        info = buf.readComponent();
    }


    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeComponent(title);
        buf.writeComponent(info);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT, title, info));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1));
        }));
        ctx.setPacketHandled(true);
    }
}
