package io.github.mattidragon.advancednetworking.test;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.graph.node.fluid.storage.FluidSourceNode;
import io.github.mattidragon.advancednetworking.graph.node.fluid.storage.FluidTargetNode;
import io.github.mattidragon.advancednetworking.graph.node.item.storage.ItemSourceNode;
import io.github.mattidragon.advancednetworking.graph.node.item.storage.ItemTargetNode;
import io.github.mattidragon.advancednetworking.test.util.AdvancedNetworkingGameTest;
import io.github.mattidragon.advancednetworking.test.util.AdvancedNetworkingTestContext;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SimpleMoveTests implements AdvancedNetworkingGameTest {
    @GameTest(templateName = AdvancedNetworkingGameTest.EMPTY_4x4x4)
    public void moveItems(AdvancedNetworkingTestContext context) {
        var controllerPos = new BlockPos(1, 1, 1);
        var controller = context.controller(controllerPos);
        context.cable(controllerPos.east());
        var endCablePos = controllerPos.east(2);
        context.cable(controllerPos.east(2), CableBlock.SOUTH, CableBlock.NORTH);

        context.setBlockState(controllerPos.up(), Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, BlockFace.FLOOR));

        var inputPos = endCablePos.south();
        var outputPos = endCablePos.north();
        context.setBlockState(inputPos, Blocks.CHEST);
        context.setBlockState(outputPos, Blocks.CHEST);

        context.getBlockEntity(inputPos, LootableContainerBlockEntity.class)
            .setStack(0, new ItemStack(Items.DIRT));

        var graph = new Graph(AdvancedNetworking.ENVIRONMENT);

        var sourceNode = new ItemSourceNode(graph);
        sourceNode.interfaceId = context.interfaceId(endCablePos, Direction.SOUTH);
        graph.addNode(sourceNode);
        var targetNode = new ItemTargetNode(graph);
        targetNode.interfaceId = context.interfaceId(endCablePos, Direction.NORTH);
        graph.addNode(targetNode);

        graph.addConnection(targetNode.getInputs()[0], sourceNode.getOutputs()[0]);
        controller.setGraph(graph, null, null);

        context.toggleLever(controllerPos.up());
        context.waitAndRun(10, () -> {
            context.expectContainerWith(outputPos, Items.DIRT);
            context.expectEmptyContainer(inputPos);
            context.complete();
        });
    }

    @GameTest(templateName = AdvancedNetworkingGameTest.EMPTY_4x4x4)
    public void moveFluids(AdvancedNetworkingTestContext context) {
        var controllerPos = new BlockPos(1, 1, 1);
        var controller = context.controller(controllerPos);
        context.cable(controllerPos.east());
        var endCablePos = controllerPos.east(2);
        context.cable(endCablePos, CableBlock.SOUTH, CableBlock.NORTH);

        context.setBlockState(controllerPos.up(), Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, BlockFace.FLOOR));

        var inputPos = endCablePos.south();
        var outputPos = endCablePos.north();
        context.setBlockState(inputPos, Blocks.WATER_CAULDRON);
        context.setBlockState(outputPos, Blocks.CAULDRON);

        var graph = new Graph(AdvancedNetworking.ENVIRONMENT);

        var sourceNode = new FluidSourceNode(graph);
        sourceNode.interfaceId = context.interfaceId(endCablePos, Direction.SOUTH);
        graph.addNode(sourceNode);
        var targetNode = new FluidTargetNode(graph);
        targetNode.interfaceId = context.interfaceId(endCablePos, Direction.NORTH);
        graph.addNode(targetNode);

        graph.addConnection(targetNode.getInputs()[0], sourceNode.getOutputs()[0]);
        controller.setGraph(graph, null, null);

        context.toggleLever(controllerPos.up());
        context.waitAndRun(10, () -> {
            context.expectBlock(Blocks.WATER_CAULDRON, outputPos);
            context.expectBlock(Blocks.CAULDRON, inputPos);
            context.complete();
        });
    }
}
