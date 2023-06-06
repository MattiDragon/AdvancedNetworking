package io.github.mattidragon.advancednetworking.screen;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import io.github.mattidragon.advancednetworking.network.NetworkRegistry;
import io.github.mattidragon.advancednetworking.network.node.InterfaceNode;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;

public class ControllerScreenHandler extends EditorScreenHandler {
    private final List<Text> errors;
    private final ScreenHandlerContext context;
    public double viewX;
    public double viewY;
    public int zoom;

    public ControllerScreenHandler(int syncId, ControllerBlockEntity provider, ScreenHandlerContext context) {
        super(syncId, provider, context);
        this.context = context;
        errors = new ArrayList<>();
        viewX = provider.viewX;
        viewY = provider.viewY;
        zoom = provider.zoom;
    }

    public ControllerScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        super(syncId, inv, buf);
        context = ScreenHandlerContext.EMPTY;
        zoom = buf.readInt();
        viewX = buf.readDouble();
        viewY = buf.readDouble();
        errors = buf.readList(PacketByteBuf::readText);
    }

    public Optional<Map<String, String>> getInterfaces() {
        return context.get((world, pos) -> {
            if (!(world instanceof ServerWorld serverWorld))
                return Optional.empty();

            var graphWorld = NetworkRegistry.UNIVERSE.getGraphWorld(serverWorld);
            var nodes = graphWorld.getLoadedGraphsAt(pos)
                    .flatMap(BlockGraph::getNodes)
                    .toList();

            var map = new HashMap<String, String>();

            for (var node : nodes) {
                if (!(node.getNode() instanceof InterfaceNode interfaceNode))
                    continue;
                if (!(world.getBlockEntity(node.getPos()) instanceof CableBlockEntity cable))
                    continue;

                var interfaceId = CableBlock.calcInterfaceId(node.getPos(), interfaceNode.getSide());
                var name = cable.getName(interfaceNode.getSide());
                map.put(interfaceId, name);
            }
            return Optional.of(map);
        }, Optional.empty());
    }

    public List<Text> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return AdvancedNetworking.CONTROLLER_SCREEN;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        context.run((world, pos) -> {
            if (world.getBlockEntity(pos) instanceof ControllerBlockEntity controller) {
                controller.viewX = viewX;
                controller.viewY = viewY;
                controller.zoom = zoom;
                controller.markDirty();
            }
        });
    }
}
