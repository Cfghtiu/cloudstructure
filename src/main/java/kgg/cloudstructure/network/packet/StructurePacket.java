package kgg.cloudstructure.network.packet;

import kgg.cloudstructure.config.CloudStructureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class StructurePacket extends Packet{
    protected ServerboundSetStructureBlockPacket packet;
    protected String userName;
//    protected BlockPos pos;
    protected ResourceLocation structureName;
    protected String url;
    protected String token;

    public StructurePacket(String token, ServerboundSetStructureBlockPacket packet) {
        this.packet = packet;
        this.userName = CloudStructureConfig.CONFIG.getUser();
        this.url = CloudStructureConfig.CONFIG.getUrl();
        this.token = token;
    }

    public StructurePacket(FriendlyByteBuf buf) {
        packet = new ServerboundSetStructureBlockPacket(buf);
        this.userName = buf.readUtf();
        this.url = buf.readUtf();
        this.token = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        packet.write(buf);
        buf.writeUtf(this.userName);
        buf.writeUtf(url);
        buf.writeUtf(token);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ServerPlayer player = supplier.get().getSender();
            if (player != null) {
                packet.handle(player.connection);
                super.handle(supplier);
            }
        });
        supplier.get().setPacketHandled(true);
    }

    @Nullable
    protected StructureBlockEntity getUsableStructureBlock(NetworkEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        BlockPos pos = packet.getPos();
        if (player.level.getBlockEntity(pos) instanceof StructureBlockEntity structure) {
            if (structure.hasStructureName()) {
                structureName = ResourceLocation.tryParse(structure.getStructureName());
                return structure;
            }
        }
        return null;
    }
}
