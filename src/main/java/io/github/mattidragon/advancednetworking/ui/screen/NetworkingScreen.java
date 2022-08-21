package io.github.mattidragon.advancednetworking.ui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.networking.Connector;
import io.github.mattidragon.advancednetworking.networking.Graph;
import io.github.mattidragon.advancednetworking.networking.GraphSyncPacket;
import io.github.mattidragon.advancednetworking.networking.node.Node;
import io.github.mattidragon.advancednetworking.networking.node.NodeType;
import io.github.mattidragon.advancednetworking.ui.MessageToast;
import io.github.mattidragon.advancednetworking.ui.screen.handler.NetworkingScreenHandler;
import io.github.mattidragon.advancednetworking.ui.widget.NodeWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import javax.annotation.Nullable;
import java.util.*;

public class NetworkingScreen extends HandledScreen<NetworkingScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(AdvancedNetworking.MOD_ID,"textures/gui/networking.png");
    public static final int TILE_SIZE = 16;
    public static final int BORDER_SIZE = 8;
    public static final int BORDER_OFFSET = 32;
    public static final int GRID_OFFSET = BORDER_OFFSET + BORDER_SIZE;

    public final Graph graph;
    private final List<ButtonWidget> addButtons = new ArrayList<>();
    private List<NodeWidget> nodes = null;
    private boolean isAddingNode = false;
    private boolean isDeletingNode = false;

    private ButtonWidget plusButton;
    private ButtonWidget deleteButton;
    private @Nullable Connector<?> lastHoveredConnector = null;
    private long lastHoveredTimestamp = 0;
    public @Nullable Connector<?> connectingConnector;


    public NetworkingScreen(NetworkingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        graph = handler.graph.copy();
    }

    @Override
    protected void init() {
        if (nodes == null) {
            nodes = new ArrayList<>();
            for (var node : graph.getNodes()) {
                nodes.add(new NodeWidget(node, this));
            }
        }

        addButtons.clear();
        List<NodeType<?>> types = NodeType.REGISTRY.stream().toList();
        for (int i = 0; i < types.size(); i++) {
            NodeType<?> type = types.get(i);
            var button = addDrawableChild(new ButtonWidget(GRID_OFFSET, BORDER_OFFSET + i * 20, 100, 20, type.name(), button1 -> {
                toggleAddingMode();
                var node = type.supplier().get();
                graph.addNode(node);
                nodes.add(addDrawableChild(new NodeWidget(node, this)));
                syncGraph();
            }));
            button.visible = false;
            button.active = false;
            addButtons.add(button);
        }

        for (NodeWidget node : nodes) {
            addDrawableChild(node);
            node.fixPos();
        }

        plusButton = addDrawableChild(new ButtonWidget(GRID_OFFSET, BORDER_OFFSET - 20, 100, 20, Text.translatable(isAddingNode ? "advanced_networking.node.cancel" : "advanced_networking.node.add"), button -> toggleAddingMode()));
        deleteButton = addDrawableChild(new ButtonWidget(GRID_OFFSET + 110, BORDER_OFFSET - 20, 100, 20, Text.translatable(isDeletingNode ? "advanced_networking.node.cancel" : "advanced_networking.node.delete"), button -> toggleDeletingMode()));
    }

    public void syncGraph() {
        GraphSyncPacket.send(this.handler.syncId, graph);
        //showToast(Text.literal("Syncing").formatted(Formatting.GREEN));
    }

    private void toggleDeletingMode() {
        isDeletingNode = !isDeletingNode;
        plusButton.active = !isDeletingNode;
        plusButton.visible = !isDeletingNode;

        deleteButton.setMessage(Text.translatable(isDeletingNode ? "advanced_networking.node.cancel" : "advanced_networking.node.delete"));
    }

    private void toggleAddingMode() {
        isAddingNode = !isAddingNode;
        nodes.forEach(node -> {
            node.active = !isAddingNode;
            node.visible = !isAddingNode;
        });
        addButtons.forEach(button -> {
            button.active = isAddingNode;
            button.visible = isAddingNode;
        });
        deleteButton.active = !isAddingNode;
        deleteButton.visible = !isAddingNode;
        plusButton.setMessage(Text.translatable(isAddingNode ? "advanced_networking.node.cancel" : "advanced_networking.node.add"));
    }

    private void tryFindConnection(double mouseX, double mouseY) {
        var row = findConnectorAt(mouseX, mouseY);
        if (row == null) return;
        if (connectingConnector == null) return;
        if (row == connectingConnector) return;

        if (connectingConnector.isOutput() == row.isOutput()) {
            if (row.isOutput())
                showToast(Text.translatable("advanced_networking.node.error.two_outputs").formatted(Formatting.RED));
            else
                showToast(Text.translatable("advanced_networking.node.error.two_inputs").formatted(Formatting.RED));
            return;
        }

        if (connectingConnector.type() != row.type()) {
            showToast(Text.translatable("advanced_networking.node.error.different_type").formatted(Formatting.RED));
            return;
        }

        graph.removeConnections(row);
        graph.addConnection(connectingConnector, row);

        var stack = new ArrayDeque<Connector<?>>();
        var searchTarget = connectingConnector.isOutput() ? connectingConnector : row;
        var searchStarter = connectingConnector.isOutput() ? row : connectingConnector;

        Arrays.stream(searchStarter.parent().getOutputs())
                .map(graph::getConnection)
                .flatMap(Optional::stream)
                .map(connection -> connection.getInputConnector(graph))
                .filter(Objects::nonNull)
                .forEach(stack::push);

        while (!stack.isEmpty()) {
            var element = stack.pop();
            if (element.equals(searchTarget) || element.equals(searchStarter)) {
                showToast(Text.translatable("advanced_networking.node.error.recursion").formatted(Formatting.RED));
                graph.removeConnections(connectingConnector);
                return;
            }

            Arrays.stream(element.parent().getOutputs())
                    .map(graph::getConnection)
                    .flatMap(Optional::stream)
                    .map(connection -> connection.getInputConnector(graph))
                    .filter(Objects::nonNull)
                    .forEach(stack::push);
        }

        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (connectingConnector != null && button == 0) {
            graph.removeConnections(connectingConnector);

            tryFindConnection(mouseX, mouseY);

            connectingConnector = null;
        }
        setFocused(null);
        // Sync node movement and connector changes
        syncGraph();
        return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseReleased(mouseX, mouseY, button)).isPresent();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isDeletingNode) {
            for (Iterator<NodeWidget> iterator = nodes.iterator(); iterator.hasNext(); ) {
                NodeWidget node = iterator.next();
                if (node.clicked(mouseX, mouseY)) {
                    graph.removeNode(node.node.id);
                    remove(node);
                    iterator.remove();
                    syncGraph();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isAddingNode) return;
        var connector = findConnectorAt(mouseX, mouseY);
        if (connector == null) return;

        long time = MinecraftClient.getInstance().world.getTime();
        if (!connector.equals(lastHoveredConnector) || time - lastHoveredTimestamp > 10) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.7f));
        }
        lastHoveredConnector = connector;
        lastHoveredTimestamp = time;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!isAddingNode && this.getFocused() != null && this.isDragging() && button == 0) {
            return this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public NodeWidget findWidget(Node node) {
        for (var widget : nodes) {
            if (widget.node == node)
                return widget;
        }
        return null;
    }

    public NodeWidget.Segment findSegment(Connector<?> connector) {
        for (var segment : findWidget(connector.parent()).calculateSegments()) {
            if (segment.connector.equals(connector))
                return segment;
        }
        return null;
    }

    @Nullable
    private Connector<?> findConnectorAt(double mouseX, double mouseY) {
        for (var node : nodes) {
            for (var segment : node.calculateSegments()) {
                if (segment.hasConnectorAt(mouseX, mouseY)) {
                    return segment.connector;
                }
            }
        }
        return null;
    }

    @Nullable
    private NodeWidget.Segment findSegmentAt(double mouseX, double mouseY) {
        for (var node : nodes) {
            for (var segment : node.calculateSegments()) {
                if (segment.hasConnectorAt(mouseX, mouseY)) {
                    return segment;
                }
            }
        }
        return null;
    }

    public int getBoxWidth() {
        return (this.width - GRID_OFFSET * 2) / TILE_SIZE * TILE_SIZE;
    }

    public int getBoxHeight() {
        return (this.height - GRID_OFFSET * 2) / TILE_SIZE * TILE_SIZE;
    }

    public void showToast(Text message) {
        MinecraftClient.getInstance().getToastManager().add(new MessageToast(message));
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        renderArea(matrices);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        if (!isAddingNode)
            renderConnectors(matrices, mouseX, mouseY);

        for (var node : nodes)
            node.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {

    }

    private void renderConnectors(MatrixStack matrices, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        if (connectingConnector != null) {
            var row = findSegmentAt(mouseX, mouseY);
            var targetX = mouseX;
            var targetY = mouseY;

            if (row != null) {
                targetX = row.getConnectorX();
                targetY = row.getConnectorY();
            }

            var connectingSegment = findSegment(connectingConnector);

            renderConnectorLine(matrices, targetX, targetY, connectingSegment.getConnectorX(), connectingSegment.getConnectorY(), connectingConnector.type().color() | 0xaa000000);
        }

        graph.getConnections().forEach(connection -> {
            var input = findSegment(Objects.requireNonNull(connection.getInputConnector(graph)));
            var output = findSegment(Objects.requireNonNull(connection.getOutputConnector(graph)));

            renderConnectorLine(matrices, input.getConnectorX(), input.getConnectorY(), output.getConnectorX(), output.getConnectorY(), input.connector.type().color() | 0xaa000000);
        });

        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    private void renderConnectorLine(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        var matrix = matrices.peek().getPositionMatrix();
        var xOffset = x1 - x2;
        var yOffset = y1 - y2;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        if (xOffset > 0) {
            bufferBuilder.vertex(matrix, x2, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2, 0).color(color).next();
            bufferBuilder.vertex(matrix, x2, y2, 0).color(color).next();
        } else {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x2 + 4, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x2 + 4, y2, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2, 0).color(color).next();
        }

        if (yOffset < 0) {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1 + 4, 0).color(color).next();
        } else {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y2, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y2, 0).color(color).next();
        }

        if (xOffset > 0) {
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f, y1, 0).color(color).next();
        } else {
            bufferBuilder.vertex(matrix, x1, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1 + 4, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1 - xOffset / 2f + 4, y1, 0).color(color).next();
            bufferBuilder.vertex(matrix, x1, y1, 0).color(color).next();
        }

    }

    private void renderArea(MatrixStack matrices) {
        var rows = (this.height - GRID_OFFSET * 2) / TILE_SIZE;
        var columns = (this.width - GRID_OFFSET * 2) / TILE_SIZE;
        int boxHeight = getBoxHeight();
        int boxWidth = getBoxWidth();

        // DrawableHelper.drawTexture is too slow because it uses one draw call per call. We only need it at the end.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        var matrix = matrices.peek().getPositionMatrix();

        // Draws the main grid
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                addTexturedQuad(matrix, GRID_OFFSET + TILE_SIZE * x, GRID_OFFSET + TILE_SIZE * y, 8, 8, 16, 16);
            }
        }

        // Draws top and bottoms
        for (int x = 0; x < columns; x++) {
            addTexturedQuad(matrix, GRID_OFFSET + TILE_SIZE * x, BORDER_OFFSET, 8, 0, 16, 8);
            addTexturedQuad(matrix, GRID_OFFSET + TILE_SIZE * x, GRID_OFFSET + boxHeight, 8, 24, 16, 8);
        }

        // Draws sides
        for (int y = 0; y < rows; y++) {
            addTexturedQuad(matrix, BORDER_OFFSET, GRID_OFFSET + TILE_SIZE * y, 0, 8, 8, 16);
            addTexturedQuad(matrix, GRID_OFFSET + boxWidth, GRID_OFFSET + TILE_SIZE * y, 24, 8, 8, 16);
        }

        // Draws corners
        addTexturedQuad(matrix, BORDER_OFFSET, BORDER_OFFSET, 0, 0, 8, 8);
        addTexturedQuad(matrix, BORDER_OFFSET, GRID_OFFSET + boxHeight, 0, 24, 8, 8);
        addTexturedQuad(matrix, GRID_OFFSET + boxWidth, BORDER_OFFSET , 24, 0, 8, 8);
        addTexturedQuad(matrix, GRID_OFFSET + boxWidth, GRID_OFFSET + boxHeight, 24, 24, 8, 8);

        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    public static void addTexturedQuad(Matrix4f matrix, int x1, int y1, int u, int v, int width, int height) {
        int x2 = x1 + width;
        int y2 = y1 + height;
        float u1 = u / 256f;
        float u2 = (u + width) / 256f;
        float v1 = v / 256f;
        float v2 = (v + height) / 256f;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        bufferBuilder.vertex(matrix, x1, y2, 0).texture(u1, v2).next();
        bufferBuilder.vertex(matrix, x2, y2, 0).texture(u2, v2).next();
        bufferBuilder.vertex(matrix, x2, y1, 0).texture(u2, v1).next();
        bufferBuilder.vertex(matrix, x1, y1, 0).texture(u1, v1).next();
    }
}
