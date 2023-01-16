package kgg.cloudstructure.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import kgg.cloudstructure.config.CloudStructureConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class EditCloudStructureSourceScreen extends Screen {
    private static final Component URL_LABEL = new TranslatableComponent("structure_source.url");
    private static final Component USER_LABEL = new TranslatableComponent("structure_source.user");
    private static final Component PASSWD_LABEL = new TranslatableComponent("structure_source.passwd");

    Screen lastScreen;
    EditBox userEdit;
    EditBox passwdEdit;
    EditBox urlEdit;

    public EditCloudStructureSourceScreen(Screen screen) {
        super(new TranslatableComponent("structure_source.screen.title"));
        lastScreen = screen;
    }

    @Override
    protected void init() {
        urlEdit = addRenderableWidget(new EditBox(font, this.width / 2 - 152, 50, 300, 20, URL_LABEL));
        urlEdit.setMaxLength(100);
        urlEdit.setValue(CloudStructureConfig.CONFIG.getUrl());

        userEdit = addRenderableWidget(new EditBox(font, this.width / 2 - 152, 85, 300, 20, USER_LABEL){
            public boolean charTyped(char c, int i) {
                return EditCloudStructureSourceScreen.this.isValidCharacterForName(this.getValue(), c, this.getCursorPosition()) && super.charTyped(c, i);
            }
        });
        userEdit.setMaxLength(100);
        userEdit.setValue(CloudStructureConfig.CONFIG.getUser());

        passwdEdit = addRenderableWidget(new EditBox(font, this.width / 2 - 152, 120, 300, 20, PASSWD_LABEL));
        passwdEdit.setMaxLength(100);
        passwdEdit.setValue(CloudStructureConfig.CONFIG.getPasswd());

        this.addRenderableWidget(new Button(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_DONE, (p_97691_) -> {
            CloudStructureConfig.CONFIG.setUrl(urlEdit.getValue());
            CloudStructureConfig.CONFIG.setUser(userEdit.getValue());
            CloudStructureConfig.CONFIG.setPasswd(passwdEdit.getValue());
            minecraft.setScreen(lastScreen);
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_CANCEL, (p_97687_) -> minecraft.setScreen(lastScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 16777215);

        drawString(poseStack, this.font, URL_LABEL, this.width / 2 - 153, 40, 10526880);
        drawString(poseStack, this.font, USER_LABEL, this.width / 2 - 153, 75, 10526880);
        drawString(poseStack, this.font, PASSWD_LABEL, this.width / 2 - 153, 110, 10526880);
        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        urlEdit.tick();
        userEdit.tick();
        passwdEdit.tick();
    }
}
