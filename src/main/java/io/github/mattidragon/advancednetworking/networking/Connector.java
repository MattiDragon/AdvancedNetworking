package io.github.mattidragon.advancednetworking.networking;

import io.github.mattidragon.advancednetworking.networking.data.DataType;
import io.github.mattidragon.advancednetworking.networking.node.Node;

public record Connector<T>(DataType<T> type, String id, boolean isOutput, Node parent) {
}
