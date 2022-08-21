package io.github.mattidragon.advancednetworking.networking.data;

import io.github.mattidragon.advancednetworking.networking.Connector;
import io.github.mattidragon.advancednetworking.networking.node.Node;

public record DataType<T>(String id, int color, Class<T> type) {
    public static final DataType<Double> NUMBER = new DataType<>("number", 0x5555ff, Double.class);

    public DataValue<T> makeValue(T value) {
        return new DataValue<>(this, value);
    }

    public Connector<T> makeConnector(String name, boolean isOutput, Node parent) {
        return new Connector<>(this, name, isOutput, parent);
    }
}
