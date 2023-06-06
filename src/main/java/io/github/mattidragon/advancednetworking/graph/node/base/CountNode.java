package io.github.mattidragon.advancednetworking.graph.node.base;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.misc.ResourceFilter;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CountNode<R, V extends TransferVariant<R>> extends InterfaceNode {
    private final ResourceFilter<R, V> filter;
    private final BlockApiLookup<Storage<V>, @Nullable Direction> lookup;

    public CountNode(NodeType<?> type, Graph graph, Registry<R> registry, BlockApiLookup<Storage<V>, @Nullable Direction> lookup) {
        super(type, List.of(NetworkControllerContext.TYPE, ContextType.SERVER_WORLD), graph);
        filter = new ResourceFilter<>(registry);
        this.lookup = lookup;
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { DataType.NUMBER.makeRequiredOutput("count", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    public List<Text> validate() {
        var list = new ArrayList<Text>();
        list.addAll(filter.validate());
        list.addAll(super.validate());
        return list;
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var controller = context.get(NetworkControllerContext.TYPE);
        var world = context.get(ContextType.SERVER_WORLD);

        var optionalPos = findInterface(world, controller.graphId());
        if (optionalPos.isEmpty()) {
            return Either.right(Text.translatable("node.advanced_networking.interface.missing", interfaceId));
        }

        var pos = optionalPos.get().pos();
        var side = optionalPos.get().side();

        var storage = lookup.find(world, pos.offset(side), side.getOpposite());
        if (storage == null) {
            return Either.right(Text.translatable("node.advanced_networking.item_source.missing", interfaceId));
        }

        var total = 0L;
        try (var transaction = Transaction.openOuter()) {
            for (var view : storage) {
                if (!view.isResourceBlank() && filter.isAllowed(view.getResource())) {
                    total += view.extract(view.getResource(), Long.MAX_VALUE, transaction);
                }
            }
            transaction.abort();
        }

        return Either.left(new DataValue<?>[]{ DataType.NUMBER.makeValue((double) total) });
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        filter.readNbt(data);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        filter.writeNbt(data);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return filter.createScreen(this, parent);
    }

    private class InterfaceSelectionScreen extends InterfaceNode.ConfigScreen {
        private final ConfigScreen configScreen;

        public InterfaceSelectionScreen(EditorScreen parent, ConfigScreen configScreen) {
            super(parent);
            this.configScreen = configScreen;
        }

        @Override
        public void close() {
            client.setScreen(this.configScreen);
        }
    }

    private class ConfigScreen extends ResourceFilter.ConfigScreen<R, V> {
        private final EditorScreen parent;

        public ConfigScreen(EditorScreen parent) {
            super(CountNode.this, parent, filter);
            this.parent = parent;
        }

        @Override
        protected void init() {
            super.init();

            var x = ((width - 200) / 2) - 50;
            var chooseInterfaceButton = ButtonWidget.builder(Text.translatable("node.advanced_networking.item_count.choose_interface"), button -> client.setScreen(new InterfaceSelectionScreen(parent, this)))
                    .width(100)
                    .position(x, 175)
                    .build();
            addDrawableChild(chooseInterfaceButton);
        }
    }
}
