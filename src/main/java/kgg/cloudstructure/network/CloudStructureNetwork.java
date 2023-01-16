package kgg.cloudstructure.network;

import kgg.cloudstructure.CloudStructureMod;
import kgg.cloudstructure.network.packet.DownloadStructurePacket;
import kgg.cloudstructure.network.packet.ReplyPacket;
import kgg.cloudstructure.network.packet.UploadStructurePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CloudStructureNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;
    public static final Logger LOGGER = LogManager.getLogger(CloudStructureNetwork.class);

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(CloudStructureMod.MOD_ID, "main_channel"),
                () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        int id = 0;
        INSTANCE.registerMessage(++id, UploadStructurePacket.class, UploadStructurePacket::write, UploadStructurePacket::new, UploadStructurePacket::handle);
        INSTANCE.registerMessage(++id, DownloadStructurePacket.class, DownloadStructurePacket::write, DownloadStructurePacket::new, DownloadStructurePacket::handle);
        INSTANCE.registerMessage(++id, ReplyPacket.class, ReplyPacket::write, ReplyPacket::new, ReplyPacket::handle);
    }
}
