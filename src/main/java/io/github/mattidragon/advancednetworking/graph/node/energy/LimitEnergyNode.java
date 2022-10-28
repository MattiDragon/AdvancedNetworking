package io.github.mattidragon.advancednetworking.graph.node.energy;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.client.screen.SliderConfigScreen;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class LimitEnergyNode extends Node {
    private int limit = 256;

    public LimitEnergyNode(Graph graph) {
        super(ModNodeTypes.LIMIT_ENERGY, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.ENERGY_STREAM.makeRequiredOutput("out", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { ModDataTypes.ENERGY_STREAM.makeRequiredInput("in", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        // Shared counter for all paths
        var counter = new MutableInt(limit);

        var stream = inputs[0].getAs(ModDataTypes.ENERGY_STREAM).transform(storage -> new LimitingStorage(storage, counter));

        return Either.left(new DataValue<?>[]{ ModDataTypes.ENERGY_STREAM.makeValue(stream) });
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        limit = MathHelper.clamp(data.getInt("limit"), 1, 256);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putInt("limit", limit);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new SliderConfigScreen(this, parent, value -> limit = value, () -> limit, Text.translatable("node.advanced_networking.limit"), 1, 256);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    private record LimitingStorage(EnergyStorage delegate, MutableInt counter) implements EnergyStorage {
        @Override
        public boolean supportsInsertion() {
            return delegate.supportsInsertion();
        }

        @Override
        public boolean supportsExtraction() {
            return delegate.supportsExtraction();
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            var inserted = delegate.insert(Math.min(maxAmount, counter.getValue()), transaction);
            counter.subtract(inserted);
            return inserted;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return delegate.extract(maxAmount, transaction);
        }

        @Override
        public long getAmount() {
            return delegate.getAmount();
        }

        @Override
        public long getCapacity() {
            return delegate.getCapacity();
        }
    }
}
