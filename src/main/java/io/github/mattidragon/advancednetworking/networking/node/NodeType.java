package io.github.mattidragon.advancednetworking.networking.node;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.function.Supplier;

public record NodeType<T extends Node>(Supplier<T> supplier, Text name) {
    public static final DefaultedRegistry<NodeType<?>> REGISTRY = Registry.create(RegistryKey.ofRegistry(AdvancedNetworking.id("node_type")), "advanced_networking:debug", NodeType::getDefault);
    public static final NodeType<MathNode> MATH = register(new NodeType<>(MathNode::new, Text.translatable("advanced_networking.node.math")), AdvancedNetworking.id("math"));
    public static final NodeType<LogNumberNode> DEBUG = register(new NodeType<>(LogNumberNode::new, Text.translatable("advanced_networking.node.debug")), AdvancedNetworking.id("debug"));
    public static final NodeType<TimeNode> TIME = register(new NodeType<>(TimeNode::new, Text.translatable("advanced_networking.node.time")), AdvancedNetworking.id("time"));

    public static void register() {}

    public static <T extends Node> NodeType<T> register(NodeType<T> type, Identifier id) {
        Registry.register(REGISTRY, id, type);
        return type;
    }

    private static NodeType<?> getDefault(Registry<NodeType<?>> registry) {
        return DEBUG;
    }
}
