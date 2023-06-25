package io.github.mattidragon.advancednetworking.test;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.graph.node.redstone.WriteRedstoneNode;
import io.github.mattidragon.advancednetworking.test.util.AdvancedNetworkingGameTest;
import io.github.mattidragon.advancednetworking.test.util.AdvancedNetworkingTestContext;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.test.GameTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RedstoneTests implements AdvancedNetworkingGameTest {
    @GameTest(templateName = AdvancedNetworkingGameTest.EMPTY_4x4x4)
    public void powerLamp(AdvancedNetworkingTestContext context) {
        var controllerPos = new BlockPos(1, 1, 1);
        var controller = context.controller(controllerPos);
        var cablePos = controllerPos.north();
        context.cable(cablePos, CableBlock.UP);
        context.setBlockState(cablePos.up(), Blocks.REDSTONE_LAMP);
        context.setBlockState(controllerPos.up(), Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, WallMountLocation.FLOOR));

        var graph = new Graph(AdvancedNetworking.ENVIRONMENT);

        var outputNode = new WriteRedstoneNode(graph);
        outputNode.interfaceId = context.interfaceId(cablePos, Direction.UP);
        graph.addNode(outputNode);

        var numberNode = createNumberNode(graph, 15);
        graph.addNode(numberNode);

        graph.addConnection(numberNode.getOutputs()[0], outputNode.getInputs()[0]);
        controller.setGraph(graph, null, null);

        context.toggleLever(controllerPos.up());
        context.waitAndRun(10, () -> {
            context.checkBlockState(cablePos.up(), state -> state.get(RedstoneLampBlock.LIT), () -> "Expected lamp to be lit");
            context.complete();
        });
    }
}
