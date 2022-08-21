package io.github.mattidragon.advancednetworking.networking;

import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public record Connection(UUID inputUuid, String inputName, UUID outputUuid, String outputName) {
    public static Connection fromNbt(NbtCompound data) {
        return new Connection(data.getUuid("inputUuid"), data.getString("inputName"), data.getUuid("outputUuid"), data.getString("outputName"));
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        nbt.putUuid("inputUuid", inputUuid);
        nbt.putString("inputName", inputName);
        nbt.putUuid("outputUuid", outputUuid);
        nbt.putString("outputName", outputName);
        return nbt;
    }

    public Connector<?> getInputConnector(Graph graph) {
        for (var input : graph.getNode(inputUuid).getInputs()) {
            if (input.id().equals(inputName))
                return input;
        }
        return null;
    }

    public Connector<?> getOutputConnector(Graph graph) {
        for (var output : graph.getNode(outputUuid).getOutputs()) {
            if (output.id().equals(outputName))
                return output;
        }
        return null;
    }
}
