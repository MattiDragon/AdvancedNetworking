package io.github.mattidragon.advancednetworking.client;

import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.graph.BlockGraph;
import com.kneelawk.graphlib.graph.SidedBlockNode;
import com.kneelawk.graphlib.graph.struct.Node;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Set;

public class GraphDebugRenderer{
    public static final GraphDebugRenderer INSTANCE = new GraphDebugRenderer();
    public boolean active = false;
    public boolean classview = false;

    private GraphDebugRenderer() {}

    public void render(WorldRenderContext context) {
        if (!active)
            return;
        var client = MinecraftClient.getInstance();
        if (client.getServer() == null || client.player == null)
            return;
        var world = client.getServer().getWorld(client.player.world.getRegistryKey());
        if (world == null)
            return;

        var controller = GraphLib.getController(world);
        var links = controller.getGraphs()
                .mapToObj(controller::getGraph)
                .filter(Objects::nonNull)
                .flatMap(BlockGraph::getNodes)
                .map(Node::connections)
                .flatMap(Set::stream)
                .distinct()
                .toList();

        var builder = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        builder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        var cam = context.camera().getPos();

        for (var link : links) {
            var first = Vec3d.ofCenter(link.first().data().getPos());
            var second = Vec3d.ofCenter(link.second().data().getPos());
            if (link.first().data().getNode() instanceof SidedBlockNode sided)
                first = first.add(Vec3d.of(sided.getSide().getVector()).multiply(0.25));
            if (link.second().data().getNode() instanceof SidedBlockNode sided)
                second = second.add(Vec3d.of(sided.getSide().getVector()).multiply(0.25));

            builder.vertex(first.getX() - cam.x, first.getY() - cam.y, first.getZ() - cam.z).color(classview ? link.first().data().getNode().getClass().hashCode() | 0xff000000 : 0xffffffff).next();
            builder.vertex(second.getX() - cam.x, second.getY() - cam.y, second.getZ() - cam.z).color(classview ? link.second().data().getNode().getClass().hashCode() | 0xff000000 : 0xffffffff).next();
        }

        BufferRenderer.drawWithShader(builder.end());
    }
}
