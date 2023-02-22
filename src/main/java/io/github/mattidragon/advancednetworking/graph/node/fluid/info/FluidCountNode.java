package io.github.mattidragon.advancednetworking.graph.node.fluid.info;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.InterfaceNode;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FluidCountNode extends InterfaceNode {
    private String fluidId = "";
    private String nbt = "";
    private FilterMode filterMode = FilterMode.FLUID;
    private boolean whitelist = true;

    public FluidCountNode(Graph graph) {
        super(ModNodeTypes.FLUID_COUNT, List.of(NetworkControllerContext.TYPE, ContextType.SERVER_WORLD), graph);
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
        if (!fluidId.isBlank()) {
            var id = Identifier.tryParse(fluidId.trim());
            if (id == null)
                list.add(Text.translatable("node.advanced_networking.filter_fluid.invalid_id"));
            else if (filterMode == FilterMode.FLUID && !Registry.FLUID.containsId(id))
                list.add(Text.translatable("node.advanced_networking.filter_fluid.unknown_fluid", id));
        }

        try {
            if (!nbt.isBlank())
                NbtPathArgumentType.nbtPath().parse(new StringReader(nbt.trim()));
        } catch (CommandSyntaxException e) {
            list.add(Text.translatable("node.advanced_networking.filter_fluid.invalid_nbt_path", e.getMessage()));
        }

        list.addAll(super.validate());

        return list;
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var controller = context.get(NetworkControllerContext.TYPE);
        var world = context.get(ContextType.SERVER_WORLD);

        var fluid = fluidId.isBlank() ? null : Registry.FLUID.get(new Identifier(fluidId));
        var tag = fluidId.isBlank() ? null : TagKey.of(Registry.FLUID_KEY, new Identifier(fluidId));
        NbtPathArgumentType.NbtPath path;
        try {
            path = nbt.isBlank() ? null : NbtPathArgumentType.nbtPath().parse(new StringReader(nbt.trim()));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Error while building nbt path not caught in validation", e);
        }
        var filter = buildFilter(fluid, tag, path, whitelist);

        var optionalPos = findInterface(world, controller.graphId());
        if (optionalPos.isEmpty()) {
            return Either.right(Text.translatable("node.advanced_networking.interface.missing", interfaceId));
        }

        var pos = optionalPos.get().pos();
        var side = optionalPos.get().side();

        var storage = FluidStorage.SIDED.find(world, pos.offset(side), side.getOpposite());
        if (storage == null) {
            return Either.right(Text.translatable("node.advanced_networking.fluid_source.missing", interfaceId));
        }

        var total = 0L;
        try (var transaction = Transaction.openOuter()) {
            for (var view : storage) {
                if (!view.isResourceBlank() && filter.test(view.getResource())) {
                    total += view.extract(view.getResource(), Long.MAX_VALUE, transaction);
                }
            }
            transaction.abort();
        }

        return Either.left(new DataValue<?>[]{ DataType.NUMBER.makeValue((double) total) });
    }

    @NotNull
    private Predicate<FluidVariant> buildFilter(Fluid fluid, TagKey<Fluid> tag, NbtPathArgumentType.NbtPath path, boolean whitelist) {
        return (fluidVariant) -> {
            if (filterMode == FilterMode.FLUID && fluid != null && fluidVariant.getFluid() != fluid)
                return !whitelist;

            //noinspection deprecation
            if (filterMode == FilterMode.TAG && tag != null && !fluidVariant.getFluid().getRegistryEntry().isIn(tag))
                return !whitelist;

            if (path == null)
                return whitelist;
            if (path.count(fluidVariant.getNbt()) > 0)
                return whitelist;
            return !whitelist;
        };
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        fluidId = data.getString("fluidId");
        nbt = data.getString("nbt");
        filterMode = FilterMode.byOrdinal(data.getInt("mode"));
        whitelist = data.getBoolean("whitelist");
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("fluidId", fluidId);
        data.putString("nbt", nbt);
        data.putInt("mode", filterMode.ordinal());
        data.putBoolean("whitelist", whitelist);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new ConfigScreen(parent);
    }

    private enum FilterMode {
        FLUID, TAG;

        private static FilterMode byOrdinal(int ordinal) {
            return ordinal > 0 && ordinal < values().length ? values()[ordinal] : FLUID;
        }
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

    private class ConfigScreen extends NodeConfigScreen {
        private final EditorScreen parent;

        public ConfigScreen(EditorScreen parent) {
            super(FluidCountNode.this, parent);
            this.parent = parent;
        }

        @Override
        protected void init() {
            var x = ((width - 200) / 2) - 50;

            var whitelistButton = CyclingButtonWidget.onOffBuilder(Text.translatable("node.advanced_networking.filter_fluid.mode.whitelist"), Text.translatable("node.advanced_networking.filter_fluid.mode.blacklist"))
                    .initially(whitelist)
                    .omitKeyText()
                    .build(x, 70, 100, 20, Text.empty(), (button1, value) -> whitelist = value);
            addDrawableChild(whitelistButton);

            var modeButton = CyclingButtonWidget.<FilterMode>builder(filterMode -> filterMode == FilterMode.FLUID ? Text.translatable("node.advanced_networking.filter_fluid.mode.fluid") : Text.translatable("node.advanced_networking.filter_fluid.mode.tag"))
                    .values(FilterMode.values())
                    .initially(filterMode)
                    .build(x, 95, 100, 20, Text.translatable("node.advanced_networking.filter_fluid.mode"), (button1, value) -> filterMode = value);
            addDrawableChild(modeButton);

            var idField = new TextFieldWidget(textRenderer, x, 120, 100, 20, Text.empty());
            if (fluidId.isEmpty()) {
                idField.setSuggestion("id");
            }
            idField.setText(fluidId);
            idField.setChangedListener(newValue -> {
                fluidId = newValue;
                idField.setSuggestion(newValue.isEmpty() ? "id" : "");
            });
            addDrawableChild(idField);

            var nbtField = new TextFieldWidget(textRenderer, x, 145, 100, 20, Text.empty());
            nbtField.setMaxLength(200);
            if (nbt.isEmpty()) {
                nbtField.setSuggestion("nbt");
            }
            nbtField.setText(nbt);
            nbtField.setChangedListener(newValue -> {
                nbt = newValue;
                nbtField.setSuggestion(newValue.isEmpty() ? "nbt" : "");
            });
            addDrawableChild(nbtField);

            var chooseInterfaceButton = new ButtonWidget(x, 175, 100, 20, Text.translatable("node.advanced_networking.fluid_count.choose_interface"), button -> client.setScreen(new InterfaceSelectionScreen(parent, this)));
            addDrawableChild(chooseInterfaceButton);

        }
    }
}
