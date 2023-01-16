package kgg.cloudstructure.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class Packet {
    public abstract void write(FriendlyByteBuf buf);
    public abstract void handle(NetworkEvent.Context ctx);
    public void handle(Supplier<NetworkEvent.Context> supplier) {handle(supplier.get());}
}
