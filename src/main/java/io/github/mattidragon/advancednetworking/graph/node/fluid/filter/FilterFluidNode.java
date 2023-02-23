package io.github.mattidragon.advancednetworking.graph.node.fluid.filter;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
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

public class FilterFluidNode extends Node {
    private String fluidId = "";
    private String nbt = "";
    private Mode mode = Mode.FLUID;
    private boolean whitelist = true;

    public FilterFluidNode(Graph graph) {
        super(ModNodeTypes.FILTER_FLUID, List.of(), graph);
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
    public List<Text> validate() {
        var list = new ArrayList<Text>();
        if (!fluidId.isBlank()) {
            var id = Identifier.tryParse(fluidId.trim());
            if (id == null)
                list.add(Text.translatable("node.advanced_networking.filter_fluid.invalid_id"));
            else if (mode == Mode.FLUID && !Registry.FLUID.containsId(id))
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
        var fluid = fluidId.isBlank() ? null : Registry.FLUID.get(new Identifier(fluidId));
        var tag = fluidId.isBlank() ? null : TagKey.of(Registry.FLUID_KEY, new Identifier(fluidId));
        NbtPathArgumentType.NbtPath path;
        try {
            path = nbt.isBlank() ? null : NbtPathArgumentType.nbtPath().parse(new StringReader(nbt.trim()));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Error while building nbt path not caught in validation", e);
        }

        var stream = inputs[0].getAs(ModDataTypes.FLUID_STREAM);
        stream.transform(new FluidTransformer.Filter(buildFilter(fluid, tag, path, whitelist)));
        return Either.left(new DataValue<?>[]{ ModDataTypes.FLUID_STREAM.makeValue(stream) });
    }

    @NotNull
    private Predicate<FluidVariant> buildFilter(Fluid fluid, TagKey<Fluid> tag, NbtPathArgumentType.NbtPath path, boolean whitelist) {
        return (fluidVariant) -> {
            if (mode == Mode.FLUID && fluid != null && fluidVariant.getFluid() != fluid)
                return !whitelist;

            //noinspection deprecation
            if (mode == Mode.TAG && tag != null && !fluidVariant.getFluid().getRegistryEntry().isIn(tag))
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
        mode = Mode.byOrdinal(data.getInt("mode"));
        whitelist = data.getBoolean("whitelist");
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("fluidId", fluidId);
        data.putString("nbt", nbt);
        data.putInt("mode", mode.ordinal());
        data.putBoolean("whitelist", whitelist);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new ConfigScreen(parent);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    private enum Mode {
        FLUID, TAG;

        private static Mode byOrdinal(int ordinal) {
            return ordinal > 0 && ordinal < values().length ? values()[ordinal] : FLUID;
        }
    }

    protected class ConfigScreen extends NodeConfigScreen {
        public ConfigScreen(EditorScreen parent) {
            super(FilterFluidNode.this, parent);
        }

        @Override
        protected void init() {
            var x = ((width - 200) / 2) - 50;

            var whitelistButton = CyclingButtonWidget.onOffBuilder(Text.translatable("node.advanced_networking.filter_fluid.mode.whitelist"), Text.translatable("node.advanced_networking.filter_fluid.mode.blacklist"))
                    .initially(whitelist)
                    .omitKeyText()
                    .build(x, 70, 100, 20, Text.empty(), (button1, value) -> whitelist = value);
            addDrawableChild(whitelistButton);

            var button = CyclingButtonWidget.<Mode>builder(mode -> mode == Mode.FLUID ? Text.translatable("node.advanced_networking.filter_fluid.mode.fluid") : Text.translatable("node.advanced_networking.filter_fluid.mode.tag"))
                    .values(Mode.values())
                    .initially(mode)
                    .build(x, 95, 100, 20, Text.translatable("node.advanced_networking.filter_fluid.mode"), (button1, value) -> mode = value);
            addDrawableChild(button);

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
        }
    }
}
