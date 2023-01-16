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
import java.util.Optional;

public class UploadStructurePacket extends StructurePacket{
    public UploadStructurePacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public UploadStructurePacket(String token, ServerboundSetStructureBlockPacket packet) {
        super(token, packet);
    }


    @Override
    public void handle(NetworkEvent.Context ctx) {
        /*
        上传步骤
        1：保存结构
        2：获取结构
        3：获取nbt
        4：交给请求工具上传
        5：成功提示
         */
        StructureBlockEntity structure;
        if ((structure = getUsableStructureBlock(ctx)) != null) {
            if (structure.saveStructure(false)) {  // 保存结构
                Optional<StructureTemplate> t = ((ServerLevel) structure.getLevel()).getStructureManager().get(structureName);  // 获取结构
                t.ifPresent(template -> {
                    CompoundTag templateTag = template.save(new CompoundTag());  // 获取nbt
                    new Thread(() -> {
                        try {
                            RequestTool.uploadStructure(url, token, templateTag, userName, structureName.getPath());  // 交给请求工具上传
                            // 成功提示
                            ctx.enqueueWork(() -> CloudStructureNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(ctx::getSender), new ReplyPacket(new TranslatableComponent("structure_block.upload_success"), new TranslatableComponent("structure_block.hover.location", structureName))));
                        } catch (IOException e) {
                            CloudStructureNetwork.LOGGER.error("upload structure failure", e);
                            ctx.enqueueWork(() -> ctx.getSender().displayClientMessage(new TranslatableComponent("structure_block.upload_failure", e.getMessage()), false));
                        }
                    }, "upload-"+structureName).start();
                });
            } else {
                ctx.getSender().displayClientMessage(new TranslatableComponent("structure_block.save_failure", structureName), false);
            }
        }
    }
}
