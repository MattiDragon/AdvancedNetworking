package io.github.mattidragon.advancednetworking.networking;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.networking.node.Node;
import io.github.mattidragon.advancednetworking.networking.node.NodeType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {
    private final Map<UUID, Node> nodes = new HashMap<>();
    private final List<Connection> connections = new ArrayList<>();

    public Graph copy() {
        var nbt = new NbtCompound();
        writeNbt(nbt);
        var graph = new Graph();
        graph.readNbt(nbt);
        return graph;
    }

    public void addNode(Node node) {
        nodes.put(node.id, node);
    }

    public Node getNode(UUID id) {
        return nodes.get(id);
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public void removeNode(UUID id) {
        nodes.remove(id);
        cleanConnections();
        // No need to update; cleanConnections already does
    }

    public void addConnection(Connector<?> input, Connector<?> output) {
        if (input.isOutput() == output.isOutput()) throw new IllegalArgumentException("Adding connection to graph.");

        // swap input and output if necessary
        if (input.isOutput()) {
            var tmp = input;
            input = output;
            output = tmp;
        }

        connections.add(new Connection(input.parent().id, input.id(), output.parent().id, output.id()));
    }

    public void removeConnections(Connector<?> connector) {
        connections.removeIf(connection -> connection.inputUuid().equals(connector.parent().id) && connection.inputName().equals(connector.id()));
        connections.removeIf(connection -> connection.outputUuid().equals(connector.parent().id) && connection.outputName().equals(connector.id()));
    }

    public void cleanConnections() {
        connections.removeIf(connection -> {
            if (!nodes.containsKey(connection.inputUuid()))
                return true;
            if (!nodes.containsKey(connection.outputUuid()))
                return true;

            if (connection.getInputConnector(this) == null)
                return true;
            if (connection.getOutputConnector(this) == null)
                return true;

            return false;
        });
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public Optional<Connection> getConnection(Connector<?> connector) {
        for (var connection : connections) {
            if (connector.equals(connection.getOutputConnector(this))) return Optional.of(connection);
            if (connector.equals(connection.getInputConnector(this))) return Optional.of(connection);
        }
        return Optional.empty();
    }

    public void writeNbt(NbtCompound data) {
        data.put("nodes", nodes.values().stream()
                .map(node -> {
                    var nbt = new NbtCompound();
                    node.writeNbt(nbt);
                    return nbt;
                })
                .collect(Collectors.toCollection(NbtList::new)));

        data.put("connections", connections.stream().map(Connection::toNbt).collect(Collectors.toCollection(NbtList::new)));
    }

    public void readNbt(NbtCompound data) {
        var ignoredIds = new ArrayList<UUID>();

        nodes.clear();
        for (var element : data.getList("nodes", NbtElement.COMPOUND_TYPE)) {
            var nodeNbt = (NbtCompound) element;
            var type = NodeType.REGISTRY.getOrEmpty(new Identifier(nodeNbt.getString("type")));
            if (type.isEmpty()) {
                AdvancedNetworking.LOGGER.warn("Unknown node type: {}. Ignoring node", nodeNbt.getString("type"));
                // uuid getter isn't safe
                if (nodeNbt.containsUuid("id"))
                    ignoredIds.add(nodeNbt.getUuid("id"));
                continue;
            }
            var node = type.get().supplier().get();
            node.readNbt(nodeNbt);
            nodes.put(node.id, node);
        }

        connections.clear();
        for (var element : data.getList("connections", NbtElement.COMPOUND_TYPE)) {
            var connection = Connection.fromNbt((NbtCompound) element);
            if (validateConnection(connection, ignoredIds)) {
                connections.add(connection);
            }
        }
    }

    @Contract("null, _ -> false")
    private boolean validateConnection(@Nullable Connection connection, ArrayList<UUID> ignoredIds) {
        if (connection == null) {
            AdvancedNetworking.LOGGER.warn("Found malformed connection data. Removing");
            return false;
        }

        // Silently remove connections to removed nodes
        if (ignoredIds.contains(connection.inputUuid()) || ignoredIds.contains(connection.outputUuid()))
            return false;

        if (!nodes.containsKey(connection.inputUuid())) {
            AdvancedNetworking.LOGGER.warn("Found connections to non-existent node. Id: {}.", connection.inputUuid());
            ignoredIds.add(connection.inputUuid());
            return false;
        }
        if (!nodes.containsKey(connection.outputUuid())) {
            AdvancedNetworking.LOGGER.warn("Found connections to non-existent node. Id: {}.", connection.outputUuid());
            ignoredIds.add(connection.outputUuid());
            return false;
        }

        if (Arrays.stream(nodes.get(connection.inputUuid()).getInputs()).noneMatch(input -> input.id().equals(connection.inputName()))) {
            AdvancedNetworking.LOGGER.warn("Found connection to non-existent input. Name: {}, Node: {}.", connection.inputName(), connection.inputUuid());
            return false;
        }

        if (Arrays.stream(nodes.get(connection.outputUuid()).getOutputs()).noneMatch(output -> output.id().equals(connection.outputName()))) {
            AdvancedNetworking.LOGGER.warn("Found connection to non-existent output. Name: {}, Node: {}.", connection.outputName(), connection.outputUuid());
            return false;
        }

        return true;
    }
}
