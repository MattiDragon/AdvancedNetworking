package io.github.mattidragon.advancednetworking.ui.screen;

import io.github.mattidragon.advancednetworking.networking.node.Node;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NodeConfigScreen extends Screen {
    private final Node owner;
    private final NetworkingScreen parent;

    protected NodeConfigScreen(Node owner, NetworkingScreen parent) {
        super(Text.translatable("advanced_networking.node.config"));
        this.owner = owner;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        var x = width - 200;
        var y = 29;
        var isGray = new MutableBoolean(false);

        var validation = owner.validate();
        var texts = validation.stream().map(Text::literal)
                .map(text -> {
                    var formatting = isGray.booleanValue() ? Formatting.GRAY : Formatting.WHITE;
                    isGray.setValue(!isGray.booleanValue());

                    return text.formatted(formatting);
                })
                .flatMap(text -> textRenderer.wrapLines(text, 180).stream())
                .toList();

        if (texts.size() > 0)
            textRenderer.draw(matrices, Text.translatable("advanced_networking.node.config.errors"), x, 20, 0xff5555);
        else
            textRenderer.draw(matrices, Text.translatable("advanced_networking.node.config.no_errors"), x, 20, 0x55ff55);

        for (var text : texts) {
            textRenderer.draw(matrices, text, x, y, 0xffffff);
            y += 9;
        }

    }

    @Override
    public void close() {
        parent.graph.cleanConnections();
        parent.syncGraph();
        client.setScreen(parent);
    }
}
