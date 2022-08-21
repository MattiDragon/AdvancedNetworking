package io.github.mattidragon.advancednetworking.block;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.networking.Graph;
import io.github.mattidragon.advancednetworking.networking.data.DataValue;
import io.github.mattidragon.advancednetworking.networking.node.Node;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.advancednetworking.ui.screen.handler.NetworkingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ControllerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    public Graph graph = new Graph();
    public byte ticks = 0;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        var graphNbt = new NbtCompound();
        graph.writeNbt(graphNbt);
        buf.writeNbt(graphNbt);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new NetworkingScreenHandler(syncId, inv, this, ScreenHandlerContext.create(world, pos));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        graph.readNbt(nbt.getCompound("graph"));
        ticks = nbt.getByte("ticks");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        var graphNbt = new NbtCompound();
        graph.writeNbt(graphNbt);
        nbt.put("graph", graphNbt);
        nbt.putByte("ticks", ticks);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ControllerBlockEntity controller) {
        if (world.isClient) return;
        if (controller.ticks-- <= 0) {
            if (state.get(ControllerBlock.POWERED)) {
                var result = controller.evaluate();
                if (result != state.get(ControllerBlock.SUCCESS))
                    world.setBlockState(pos, state.cycle(ControllerBlock.SUCCESS), Block.NOTIFY_ALL);
            }
            controller.ticks = 10;
        }
    }

    public boolean evaluate() {
        if (world.isClient)
            return false;

        if (!graph.getNodes().stream().allMatch(node -> node.isFullyConnected(graph)))
            return false;
        if (!graph.getNodes().stream().map(Node::validate).allMatch(List::isEmpty))
            return false;

        var toEvaluate = new ArrayList<>(graph.getNodes());
        var availableInputs = new HashMap<Node, Map<String, DataValue<?>>>();

        while (!toEvaluate.isEmpty()) {
            var size = toEvaluate.size();

            for (Iterator<Node> iterator = toEvaluate.iterator(); iterator.hasNext(); ) {
                Node node = iterator.next();
                var foundInputs = availableInputs.computeIfAbsent(node, __ -> new HashMap<>());
                var requiredInputs = node.getInputs();
                if (requiredInputs.length != foundInputs.size()) continue;

                var values = Arrays.stream(requiredInputs)
                        .map(connector -> foundInputs.get(connector.id()))
                        .toArray(DataValue[]::new);

                for (int i = 0; i < values.length; i++) {
                    if (requiredInputs[i].type() != values[i].type()) {
                        evaluationError("Mismatched types for connection ('{}' -> '{}')", values[i].type().id(), requiredInputs[i].type().id());
                        return false;
                    }
                }

                var results = node.process(values, (ServerWorld) world, pos);
                var outputs = node.getOutputs();

                if (results.length != outputs.length) {
                    evaluationError("Unexpected output count (found '{}', but expected '{}')", results.length, outputs.length);
                    return false;
                }

                for (int i = 0; i < outputs.length; i++) {
                    if (results[i].type() != outputs[i].type()) {
                        evaluationError("Unexpected output type (found '{}', but expected '{}')", results[i].type().id(), outputs[i].type().id());
                        return false;
                    }

                    var connector = outputs[i];
                    var value = results[i];

                    // We use the 'input' of the connector as it's the targets input
                    var connection = graph.getConnection(connector).orElseThrow(IllegalStateException::new);
                    var target = graph.getNode(connection.inputUuid());

                    availableInputs.computeIfAbsent(target, __ -> new HashMap<>())
                            .put(connection.inputName(), value);
                }
                iterator.remove();
            }

            if (toEvaluate.size() == size) {
                evaluationError("Couldn't find resolvable node. Nodes left: {}", size);
                return false;
            }
        }
        return true;
    }

    private void evaluationError(String message, Object... args) {
        AdvancedNetworking.LOGGER.warn("Error while evaluating graph at {}: " + message, ArrayUtils.addFirst(args, pos.toShortString()));
    }
}
