package io.github.mattidragon.advancednetworking.graph.node.fluid;

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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class LimitFluidNode extends Node {
    private int limit = (int) FluidConstants.BUCKET;

    public LimitFluidNode(Graph graph) {
        super(ModNodeTypes.LIMIT_FLUID, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.FLUID_STREAM.makeRequiredOutput("out", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { ModDataTypes.FLUID_STREAM.makeRequiredInput("in", this) };
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        // Shared counter for all paths
        var counter = new MutableInt(limit);

        var stream = inputs[0].getAs(ModDataTypes.FLUID_STREAM).transform(storage -> new LimitingStorage(storage, counter));

        return Either.left(new DataValue<?>[]{ ModDataTypes.FLUID_STREAM.makeValue(stream) });
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        limit = MathHelper.clamp(data.getInt("limit"), 1, (int) FluidConstants.BUCKET);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putInt("limit", limit);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new SliderConfigScreen(this,
                parent,
                value -> limit = (int) (Math.round(value / 1000.0) * 1000),
                () -> limit,
                Text.translatable("node.advanced_networking.limit"),
                1,
                (int) FluidConstants.BUCKET);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    private record LimitingStorage(Storage<FluidVariant> delegate, MutableInt counter) implements Storage<FluidVariant> {
        @Override
        public boolean supportsInsertion() {
            return delegate.supportsInsertion();
        }

        @Override
        public boolean supportsExtraction() {
            return delegate.supportsExtraction();
        }

        @Override
        public @Nullable StorageView<FluidVariant> exactView(FluidVariant resource) {
            return delegate.exactView(resource);
        }

        @Override
        public long getVersion() {
            return delegate.getVersion();
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            var inserted = delegate.insert(resource, Math.min(maxAmount, counter.getValue()), transaction);
            counter.subtract(inserted);
            return inserted;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return delegate.extract(resource, maxAmount, transaction);
        }

        @Override
        public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
            return delegate.iterator();
        }
    }
}
