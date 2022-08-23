package io.github.mattidragon.advancednetworking.networking.node;

import io.github.mattidragon.advancednetworking.networking.Connector;
import io.github.mattidragon.advancednetworking.networking.Graph;
import io.github.mattidragon.advancednetworking.networking.data.DataValue;
import io.github.mattidragon.advancednetworking.ui.screen.NetworkingScreen;
import io.github.mattidragon.advancednetworking.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public abstract class Node {
    public UUID id = UUID.randomUUID();
    public final NodeType<?> type;
    public float guiX = 0;
    public float guiY = 0;

    protected Node(NodeType<?> type) {
        this.type = type;
    }

    public abstract Connector<?>[] getOutputs();
    public abstract Connector<?>[] getInputs();

    public abstract DataValue<?>[] process(DataValue<?>[] inputs, ServerWorld world, BlockPos pos);

    /**
     * Validates this nodes configuration as well as possible.
     * @return A list of all errors detected, may only contain some errors as long as others appear once they are fixed.
     */
    public List<String> validate() {
        return List.of();
    }

    public boolean isFullyConnected(Graph graph) {
        for (var input : getInputs()) {
            if (graph.getConnection(input).isEmpty())
                return false;
        }

        for (var output : getOutputs()) {
            if (graph.getConnection(output).isEmpty())
                return false;
        }

        return true;
    }

    @Environment(EnvType.CLIENT)
    public NodeConfigScreen createConfigScreen(NetworkingScreen parent) {
        return null;
    }

    public boolean hasConfig() {
        return false;
    }

    public void readNbt(NbtCompound data) {
        if (data.containsUuid("id")) // Default to old/random
            id = data.getUuid("id");
        guiX = data.getFloat("guiX");
        guiY = data.getFloat("guiY");
    }

    public void writeNbt(NbtCompound data) {
        data.putString("type", NodeType.REGISTRY.getId(type).toString());
        data.putUuid("id", id);
        data.putFloat("guiX", guiX);
        data.putFloat("guiY", guiY);
    }

    public Text getName() {
        return type.name();
    }
}
