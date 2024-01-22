package io.github.mattidragon.advancednetworking.test;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.graph.node.item.storage.ItemSourceNode;
import io.github.mattidragon.advancednetworking.graph.node.item.storage.ItemTargetNode;
import io.github.mattidragon.advancednetworking.test.util.AdvancedNetworkingGameTest;
import io.github.mattidragon.advancednetworking.test.util.AdvancedNetworkingTestContext;
import io.github.mattidragon.nodeflow.graph.Graph;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class GroupMoveTests implements AdvancedNetworkingGameTest {
    @GameTest(templateName = AdvancedNetworkingGameTest.EMPTY_4x4x4)
    public void moveItemsWithGroup(AdvancedNetworkingTestContext context) {
        var controllerPos = new BlockPos(1, 1, 1);
        var controller = context.controller(controllerPos);
        var cable1 = context.cable(controllerPos.east(), CableBlock.SOUTH, CableBlock.NORTH);
        var cable2 = context.cable(controllerPos.east(2), CableBlock.SOUTH, CableBlock.NORTH);

        cable1.setGroup(Direction.NORTH, "IN");
        cable2.setGroup(Direction.NORTH, "IN");
        cable1.setGroup(Direction.SOUTH, "OUT");
        cable2.setGroup(Direction.SOUTH, "OUT");

        context.setBlockState(controllerPos.up(), Blocks.LEVER.getDefaultState().with(LeverBlock.FACE, BlockFace.FLOOR));

        context.setBlockState(controllerPos.east().south(), Blocks.DECORATED_POT);
        context.setBlockState(controllerPos.east().north(), Blocks.DECORATED_POT);
        context.setBlockState(controllerPos.east(2).south(), Blocks.DECORATED_POT);
        context.setBlockState(controllerPos.east(2).north(), Blocks.DECORATED_POT);

        context.getBlockEntity(controllerPos.east().north(), DecoratedPotBlockEntity.class)
                .setStack(0, new ItemStack(Items.DIRT, 64));
        context.getBlockEntity(controllerPos.east(2).north(), DecoratedPotBlockEntity.class)
                .setStack(0, new ItemStack(Items.DIRT, 64));

        var graph = new Graph(AdvancedNetworking.ENVIRONMENT);

        var sourceNode = new ItemSourceNode(graph);
        sourceNode.interfaceId = "IN";
        sourceNode.isGroup = true;
        graph.addNode(sourceNode);
        var targetNode = new ItemTargetNode(graph);
        targetNode.interfaceId = "OUT";
        targetNode.isGroup = true;
        graph.addNode(targetNode);

        graph.addConnection(targetNode.getInputs()[0], sourceNode.getOutputs()[0]);
        controller.setGraph(graph, null, null);

        context.toggleLever(controllerPos.up());
        context.waitAndRun(20, () -> {
            context.expectPotWith(controllerPos.east().south(), Items.DIRT, 64);
            context.expectPotWith(controllerPos.east(2).south(), Items.DIRT, 64);
            context.expectEmptyPot(controllerPos.east().north());
            context.expectEmptyPot(controllerPos.east(2).north());
            context.complete();
        });
    }
}
