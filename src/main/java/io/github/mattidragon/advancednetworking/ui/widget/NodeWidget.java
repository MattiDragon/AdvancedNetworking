package io.github.mattidragon.advancednetworking.ui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.networking.Connector;
import io.github.mattidragon.advancednetworking.networking.node.Node;
import io.github.mattidragon.advancednetworking.ui.screen.NetworkingScreen;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;

import static io.github.mattidragon.advancednetworking.ui.screen.NetworkingScreen.GRID_OFFSET;
import static net.minecraft.util.math.MathHelper.clamp;

public class NodeWidget extends ClickableWidget {
    private static final Identifier TEXTURE = new Identifier(AdvancedNetworking.MOD_ID,"textures/gui/networking.png");
    public static final int WIDTH = 64;
    public static final int ROW_SIZE = 12;

    public final Node node;
    public final NetworkingScreen parent;
    private int dragX;
    private int dragY;

    public NodeWidget(Node node, NetworkingScreen parent) {
        super((int) (GRID_OFFSET + node.guiX * parent.getBoxWidth()), (int) (GRID_OFFSET + node.guiY * parent.getBoxHeight()), WIDTH, 24 + 8 + (node.getInputs().length + node.getOutputs().length) * ROW_SIZE, node.getName());
        this.node = node;
        this.parent = parent;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        //TODO: implement
    }

    public Segment[] calculateSegments() {
        var segments = new Segment[node.getInputs().length + node.getOutputs().length];
        var i = 0;

        for (var input : node.getInputs())
            segments[i++] = new Segment(x, y + 12 + i * ROW_SIZE, false, input);
        for (var output : node.getOutputs())
            segments[i++] = new Segment(x, y + 12 + i * ROW_SIZE, true, output);

        return segments;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        dragX = (int) (x - mouseX);
        dragY = (int) (y - mouseY);

        if (node.hasConfig() && mouseX >= x + WIDTH - 20 && mouseX <= x + WIDTH - 4 && mouseY >= y + 4 && mouseY <= y + 20) {
            MinecraftClient.getInstance().setScreen(node.createConfigScreen(parent));
            return;
        }

        for (Segment row : calculateSegments()) {
            if (row.hasConnectorAt(mouseX, mouseY)) {
                parent.connectingConnector = row.connector;
                break;
            }
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (parent.connectingConnector != null) return;

        x = (int) (mouseX + dragX);
        y = (int) (mouseY + dragY);

        fixPos();
        node.guiX = (x - GRID_OFFSET) / (float) parent.getBoxWidth();
        node.guiY = (y - GRID_OFFSET) / (float) parent.getBoxHeight();
    }

    public void fixPos() {
        x = clamp(x, GRID_OFFSET, GRID_OFFSET + parent.getBoxWidth() - width);
        y = clamp(y, GRID_OFFSET, GRID_OFFSET + parent.getBoxHeight() - height);
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        if (mouseX >= x + WIDTH - 20 && mouseX <= x + WIDTH - 4 && mouseY >= y + 4 && mouseY <= y + 20) {
            var tooltip = new ArrayList<Text>();
            var hasError = !node.validate().isEmpty() || !node.isFullyConnected(parent.graph);

            if (node.hasConfig())
                tooltip.add(Text.translatable("advanced_networking.node.config_tooltip").formatted(Formatting.WHITE));
            if (hasError)
                tooltip.add(Text.translatable("advanced_networking.node.errors").formatted(Formatting.RED));

            if (!node.validate().isEmpty())
                tooltip.add(Text.literal("  ").append(Text.translatable("advanced_networking.node.invalid_config").formatted(Formatting.RED)));
            if (!node.isFullyConnected(parent.graph))
                tooltip.add(Text.literal("  ").append(Text.translatable("advanced_networking.node.not_connected").formatted(Formatting.RED)));

            parent.renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        var textRenderer = Screens.getTextRenderer(parent);
        var matrix = matrices.peek().getPositionMatrix();
        var segments = calculateSegments();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        addQuad(matrix, x, y, 32, 0, 64, 12);
        addQuad(matrix, x, y + 12, 32, 8, 64, 12);

        var status = 0xffffffff;
        if (!node.isFullyConnected(parent.graph))
            status = 0xffffaa55;
        if (!node.validate().isEmpty())
            status = 0xffffaa55;
        if (node.hasConfig() && mouseX >= x + WIDTH - 20 && mouseX <= x + WIDTH - 4 && mouseY >= y + 4 && mouseY <= y + 20)
            status = 0xff9999ff;

        if (!node.isFullyConnected(parent.graph) || !node.validate().isEmpty())
            addQuad(matrix, x + WIDTH - 20, y + 4, 112, 4, 16, 16, status);
        else if (node.hasConfig())
            addQuad(matrix, x + WIDTH - 20, y + 4, 96, 4, 16, 16, status);

        for (int i = 0; i < segments.length; i++) {
            addQuad(matrix, x, y + 24 + i * ROW_SIZE, 32, 8, 64, 12);
        }

        addQuad(matrix, x, y + 24 + segments.length * ROW_SIZE, 32, 20, 64, 8);

        BufferRenderer.drawWithShader(bufferBuilder.end());

        for (var segment : segments) {
            segment.render(matrices, mouseX, mouseY);
        }

        var texts = textRenderer.wrapLines(getMessage(), 40);

        for (int i = 0; i < texts.size(); i++) {
            textRenderer.draw(matrices, texts.get(i), x + 5, y + 5 + i * 9, 0x404040);
        }
    }

    private static void addQuad(Matrix4f matrix, int x, int y, int u, int v, int width, int height) {
        addQuad(matrix, x, y, u, v, width, height, 0xffffffff);
    }

    public static void addQuad(Matrix4f matrix, int x1, int y1, int u, int v, int width, int height, int color) {
        int x2 = x1 + width;
        int y2 = y1 + height;
        float u1 = u / 256f;
        float u2 = (u + width) / 256f;
        float v1 = v / 256f;
        float v2 = (v + height) / 256f;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        bufferBuilder.vertex(matrix, x1, y2, 0).texture(u1, v2).color(color).next();
        bufferBuilder.vertex(matrix, x2, y2, 0).texture(u2, v2).color(color).next();
        bufferBuilder.vertex(matrix, x2, y1, 0).texture(u2, v1).color(color).next();
        bufferBuilder.vertex(matrix, x1, y1, 0).texture(u1, v1).color(color).next();
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY);
    }

    public class Segment extends DrawableHelper {
        private static final Identifier TEXTURE = new Identifier(AdvancedNetworking.MOD_ID,"textures/gui/networking.png");
        public static final int HEIGHT = 12;
        public static final int WIDTH = 64;

        public int x;
        public int y;
        public final Connector<?> connector;
        public final boolean isOutput;

        public Segment(int x, int y, boolean isOutput, Connector<?> connector) {
            this.x = x;
            this.y = y;
            this.isOutput = isOutput;
            this.connector = connector;
        }

        public int getConnectorX() {
            return isOutput ? x + WIDTH - 12 : x + 8;
        }

        public int getConnectorY() {
            return y + 4;
        }

        public boolean hasConnectorAt(double mouseX, double mouseY) {
            return mouseX > getConnectorX() && mouseX < getConnectorX() + 4 && mouseY > getConnectorY() && mouseY < getConnectorY() + 4;
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY) {
            var textRenderer = Screens.getTextRenderer(NodeWidget.this.parent);
            var matrix = matrices.peek().getPositionMatrix();
            boolean hovered = hasConnectorAt(mouseX, mouseY);

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(hovered ? 2 : 1, hovered ? 2 : 1, hovered ? 2 : 1, 1);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            addQuad(matrix, getConnectorX(), getConnectorY(), 96, 0, 4, 4, this.connector.type().color() | 0xff000000);

            BufferRenderer.drawWithShader(bufferBuilder.end());

            var orderedText = ((Text) Text.literal(this.connector.id())).asOrderedText();
            textRenderer.draw(matrices, orderedText, (float)(x + WIDTH / 2 - textRenderer.getWidth(orderedText) / 2) + (isOutput ? -4 : 4), (float) (y + 2), 0x404040);
        }

        public static void addQuad(Matrix4f matrix, int x1, int y1, int u, int v, int width, int height, int color) {
            int x2 = x1 + width;
            int y2 = y1 + height;
            float u1 = u / 256f;
            float u2 = (u + width) / 256f;
            float v1 = v / 256f;
            float v2 = (v + height) / 256f;

            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

            bufferBuilder.vertex(matrix, x1, y2, 0).texture(u1, v2).color(color).next();
            bufferBuilder.vertex(matrix, x2, y2, 0).texture(u2, v2).color(color).next();
            bufferBuilder.vertex(matrix, x2, y1, 0).texture(u2, v1).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).texture(u1, v1).color(color).next();
        }
    }
}
