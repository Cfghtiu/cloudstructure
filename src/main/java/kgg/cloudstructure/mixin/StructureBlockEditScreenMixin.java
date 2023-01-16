package kgg.cloudstructure.mixin;

import kgg.cloudstructure.config.CloudStructureConfig;
import kgg.cloudstructure.network.CloudStructureNetwork;
import kgg.cloudstructure.network.RequestTool;
import kgg.cloudstructure.network.packet.DownloadStructurePacket;
import kgg.cloudstructure.network.packet.UploadStructurePacket;
import kgg.cloudstructure.screen.EditCloudStructureSourceScreen;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(StructureBlockEditScreen.class)
public abstract class StructureBlockEditScreenMixin extends Screen {
    @Shadow @Final private StructureBlockEntity structure;
    @Shadow private EditBox nameEdit;

    @Shadow protected abstract int parseCoordinate(String p_99436_);

    @Shadow private EditBox posXEdit;
    @Shadow private EditBox posYEdit;
    @Shadow private EditBox posZEdit;
    @Shadow private EditBox sizeXEdit;
    @Shadow private EditBox sizeYEdit;
    @Shadow private EditBox sizeZEdit;

    @Shadow protected abstract float parseIntegrity(String p_99431_);

    @Shadow private EditBox integrityEdit;

    @Shadow protected abstract long parseSeed(String p_99427_);

    @Shadow private EditBox seedEdit;
    @Shadow private EditBox dataEdit;
    private Button downloadButton;
    private Button uploadButton;

    protected StructureBlockEditScreenMixin(Component p_96550_) {
        super(p_96550_);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        uploadButton = addRenderableWidget(new Button(this.width / 2 - 1 - 40 - 1 - 40 - 15, 150, 50, 20, new TranslatableComponent("structure_block.upload"), (button) -> sendPackage(true)));
        downloadButton = addRenderableWidget(new Button(this.width / 2 - 1 - 40 - 1 - 40 - 15, 150, 50, 20, new TranslatableComponent("structure_block.download"), (button) -> sendPackage(false)));
        addRenderableWidget(new Button(this.width / 2 - 4 - 150, 150, 50, 20, new TranslatableComponent("structure_source.edit_config"), (button) -> minecraft.setScreen(new EditCloudStructureSourceScreen(this))));
    }

    public void sendPackage(boolean isUploadButton) {
        if (CloudStructureConfig.CONFIG.isEmpty()) {
            minecraft.setScreen(new EditCloudStructureSourceScreen(this));
        } else {
            minecraft.setScreen(null);
            Util.backgroundExecutor().execute(() -> { // 还要先getToken，需要上网，所以放后背线程了
                try {
                    String token = RequestTool.getToken(isUploadButton? "upload": "download");
                    ResourceLocation location = ResourceLocation.tryParse(nameEdit.getValue());
                    if (location != null) {
                        nameEdit.setValue(CloudStructureConfig.CONFIG.getUser().toLowerCase()+":"+location.getPath());
                    }
                    ServerboundSetStructureBlockPacket packet = getServerboundSetStructureBlockPacket();
                    minecraft.submitAsync(() -> {
                        if (isUploadButton) {
                            CloudStructureNetwork.INSTANCE.sendToServer(new UploadStructurePacket(token, packet));
                        } else {
                            CloudStructureNetwork.INSTANCE.sendToServer(new DownloadStructurePacket(token, packet));
                        }
                    });
                } catch (IOException e) {
                    minecraft.tell(() -> minecraft.player.displayClientMessage(new TranslatableComponent("structure_source.request_failed", e.getMessage()), false));
                }
            });
        }
    }

    private ServerboundSetStructureBlockPacket getServerboundSetStructureBlockPacket() {
        BlockPos blockpos = new BlockPos(this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue()));
        Vec3i vec3i = new Vec3i(this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue()));
        float f = this.parseIntegrity(this.integrityEdit.getValue());
        long i = this.parseSeed(this.seedEdit.getValue());
        return new ServerboundSetStructureBlockPacket(
                this.structure.getBlockPos(), StructureBlockEntity.UpdateType.UPDATE_DATA, this.structure.getMode(),
                this.nameEdit.getValue(), blockpos, vec3i, this.structure.getMirror(), this.structure.getRotation(),
                this.dataEdit.getValue(), this.structure.isIgnoreEntities(), this.structure.getShowAir(), this.structure.getShowBoundingBox(), f, i);
    }


    @Inject(method = "updateMode", at = @At("RETURN"))
    public void updateMode(StructureMode structureMode, CallbackInfo ci) {
        downloadButton.visible = false;
        uploadButton.visible = false;
        switch (structureMode) {
            case LOAD -> downloadButton.visible = true;
            case SAVE -> uploadButton.visible = true;
        }
    }
}
