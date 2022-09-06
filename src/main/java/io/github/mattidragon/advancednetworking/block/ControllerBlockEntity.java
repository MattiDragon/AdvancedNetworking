package io.github.mattidragon.advancednetworking.block;

import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.misc.GraphProvidingBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ControllerBlockEntity extends GraphProvidingBlockEntity {
    private Graph graph = new Graph();
    public byte ticks = 0;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
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
                controller.ticks = 10;
            }
        }
    }

    public boolean evaluate() {
        return graph.evaluate(Context.builder()
                        .put(ContextType.SERVER_WORLD, (ServerWorld) world)
                        .put(ContextType.BLOCK_POS, pos)
                        .build(graph))
                .isEmpty();
    }

    @Override
    public void setGraph(Graph graph, World world, BlockPos pos) {
        this.graph = graph;
    }

    @Override
    public Graph getGraph(World world, BlockPos pos) {
        return graph;
    }

    @Override
    public List<NodeType<?>> getTypes() {
        return List.of(NodeType.TIME, NodeType.MATH, NodeType.DEBUG);
    }
}
