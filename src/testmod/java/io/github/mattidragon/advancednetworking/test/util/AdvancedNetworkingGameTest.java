package io.github.mattidragon.advancednetworking.test.util;

import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.node.builtin.NumberNode;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.TestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface AdvancedNetworkingGameTest extends CustomTestMethodInvoker {
    String EMPTY_4x4x4 = "advanced_networking_test:empty4x4x4";

    default void invokeTestMethod(TestContext context, Method method) {
        try {
            method.invoke(this, new AdvancedNetworkingTestContext(context.test));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to invoke gametest");
        }
    }

    default NumberNode createNumberNode(Graph graph, double value) {
        var node = new NumberNode(graph);
        var numberNbt = new NbtCompound();
        numberNbt.putString("value", value + "");
        node.readNbt(numberNbt);
        return node;
    }
}
