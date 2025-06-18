package io.github.mattidragon.advancednetworking.test.util;

import io.github.mattidragon.nodeflow.NodeFlow;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.node.builtin.NumberNode;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.storage.NbtReadView;
import net.minecraft.test.TestContext;
import net.minecraft.util.ErrorReporter;

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
        try (var logging = new ErrorReporter.Logging(() -> "Test number node construction", NodeFlow.LOGGER)) {
            var readView = NbtReadView.create(logging, DynamicRegistryManager.of(Registries.REGISTRIES), numberNbt);
            node.readData(readView);
        }
        return node;
    }
}
