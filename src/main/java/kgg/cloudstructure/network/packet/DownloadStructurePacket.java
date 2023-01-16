package kgg.cloudstructure.network.packet;

import kgg.cloudstructure.network.CloudStructureNetwork;
import kgg.cloudstructure.network.RequestTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;

public class DownloadStructurePacket extends StructurePacket {

    public DownloadStructurePacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public DownloadStructurePacket(String token, ServerboundSetStructureBlockPacket packet) {
        super(token, packet);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        /*
        下载步骤
        1：下载结构
        2：创建结构
        3：导入nbt
        4：设置结构方块(因为在下载完结构后原数据可能已被更改)
        5：导入结构
        5：成功提示
         */
        StructureBlockEntity structure;
        if ((structure = getUsableStructureBlock(ctx)) != null) {
            ServerLevel level = (ServerLevel) ctx.getSender().level;
            new Thread(() -> {
                try {
                    CompoundTag templateTag = RequestTool.downloadStructure(url, token, userName, structureName.getPath());  // 下载结构
                    ctx.enqueueWork(() -> {
                        try {
                            StructureTemplate template = level.getStructureManager().getOrCreate(structureName);  // 创建结构
                            template.load(templateTag);  // 导入nbt
                            packet.handle(ctx.getSender().connection);  // 设置结构方块
                            structure.loadStructure(level);  // 导入结构
                            // 成功提示
                            CloudStructureNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), new ReplyPacket(new TranslatableComponent("structure_block.download_success"), new TranslatableComponent("structure_block.hover.location", structureName)));
                        } catch (Exception e) {
                            ctx.getSender().displayClientMessage(new TranslatableComponent("structure_block.download_failure", e.getMessage()), false);
                        }
                    });
                } catch (IOException e) {
                    CloudStructureNetwork.LOGGER.error("download structure failure", e);
                    ctx.enqueueWork(() -> ctx.getSender().displayClientMessage(new TranslatableComponent("structure_block.download_failure", e.getMessage()), false));
                }
            }, "download-"+structureName).start();
        }
    }
}
