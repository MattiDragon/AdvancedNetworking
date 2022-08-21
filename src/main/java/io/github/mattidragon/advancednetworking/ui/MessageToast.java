package io.github.mattidragon.advancednetworking.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class MessageToast implements Toast {
    private final Text title;

    public MessageToast(Text title) {
        this.title = title;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        var text = manager.getClient().textRenderer.wrapLines(title, 140);
        for (var j = 0; j < text.size(); ++j) {
            manager.getClient().textRenderer.draw(matrices, text.get(j), 7, (float)(7 + j * 12), 0xffffffff);
        }
        return startTime >= 2500L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
