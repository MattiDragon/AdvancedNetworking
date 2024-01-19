package io.github.mattidragon.advancednetworking.screen;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import io.github.mattidragon.advancednetworking.network.NetworkRegistry;
import io.github.mattidragon.advancednetworking.network.node.InterfaceNode;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.*;

public class ControllerScreenHandler extends EditorScreenHandler {
    private final List<Text> errors;
    private final ScreenHandlerContext context;
    public boolean adventureModeAccessAllowed;
    public double viewX;
    public double viewY;
    public int zoom;

    public ControllerScreenHandler(int syncId, ControllerBlockEntity controller, ScreenHandlerContext context) {
        super(syncId, controller, context);
        this.context = context;
        errors = new ArrayList<>();
        viewX = controller.viewX;
        viewY = controller.viewY;
        zoom = controller.zoom;
        adventureModeAccessAllowed = controller.isAdventureModeAccessAllowed();
    }

    public ControllerScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        super(syncId, inv, buf);
        context = ScreenHandlerContext.EMPTY;
        zoom = buf.readInt();
        viewX = buf.readDouble();
        viewY = buf.readDouble();
        errors = buf.readList(PacketByteBuf::readText);
        adventureModeAccessAllowed = buf.readBoolean();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 0 && player.isCreativeLevelTwoOp()) {
            adventureModeAccessAllowed = !adventureModeAccessAllowed;
        }
        return false;
    }

    public Optional<Pair<Map<String, Text>, Map<String, List<String>>>> getInterfaces() {
        return context.get((world, pos) -> {
            if (!(world instanceof ServerWorld serverWorld))
                return Optional.empty();

            var graphWorld = NetworkRegistry.UNIVERSE.getServerGraphWorld(serverWorld);
            var nodes = graphWorld.getLoadedGraphsAt(pos)
                    .flatMap(BlockGraph::getNodes)
                    .toList();

            var interfaces = new HashMap<String, Text>();
            var groups = new HashMap<String, List<String>>();

            for (var node : nodes) {
                if (!(node.getNode() instanceof InterfaceNode interfaceNode))
                    continue;
                if (!(world.getBlockEntity(node.getBlockPos()) instanceof CableBlockEntity cable))
                    continue;

                var interfaceId = CableBlock.calcInterfaceId(node.getBlockPos(), interfaceNode.getSide());
                interfaces.put(interfaceId, cable.getDisplayName(interfaceNode.getSide()));
                var group = cable.getGroup(interfaceNode.getSide());
                if (!group.isBlank()) {
                    groups.computeIfAbsent(group, (group1) -> new ArrayList<>()).add(interfaceId);
                }
            }
            return Optional.of(new Pair<>(interfaces, groups));
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
                controller.setAdventureModeAccessAllowed(adventureModeAccessAllowed);
                controller.markDirty();
            }
        });
    }
}
