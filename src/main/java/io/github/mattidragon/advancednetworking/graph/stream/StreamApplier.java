package io.github.mattidragon.advancednetworking.graph.stream;

@FunctionalInterface
public interface StreamApplier<T, C> {
    C apply(T from, T to, C context);
}
