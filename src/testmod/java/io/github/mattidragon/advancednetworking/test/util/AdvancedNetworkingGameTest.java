package io.github.mattidragon.advancednetworking.test.util;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.node.builtin.NumberNode;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.TestContext;

import java.lang.reflect.Method;

public interface AdvancedNetworkingGameTest extends FabricGameTest {
    String EMPTY_4x4x4 = "advanced_networking_test:empty4x4x4";

    @Override
    default void invokeTestMethod(TestContext context, Method method) {
        FabricGameTest.super.invokeTestMethod(new AdvancedNetworkingTestContext(context.test), method);
    }

    default NumberNode createNumberNode(Graph graph, double value) {
        var node = new NumberNode(graph);
        var numberNbt = new NbtCompound();
        numberNbt.putString("value", "15");
        node.readNbt(numberNbt);
        return node;
    }
}
