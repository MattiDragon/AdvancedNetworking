package io.github.mattidragon.advancednetworking.graph.node.item.filter;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FilterItemsNode extends Node {
    private String itemId = "";
    private String nbt = "";
    private Mode mode = Mode.ITEM;
    private boolean whitelist = true;

    public FilterItemsNode(Graph graph) {
        super(ModNodeTypes.FILTER_ITEMS, List.of(), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { ModDataTypes.ITEM_STREAM.makeRequiredOutput("out", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { ModDataTypes.ITEM_STREAM.makeRequiredInput("in", this) };
    }

    @Override
    public List<Text> validate() {
        var list = new ArrayList<Text>();
        if (!itemId.isBlank()) {
            var id = Identifier.tryParse(itemId.trim());
            if (id == null)
                list.add(Text.translatable("node.advanced_networking.filter_items.invalid_id"));
            else if (mode == Mode.ITEM && !Registries.ITEM.containsId(id))
                list.add(Text.translatable("node.advanced_networking.filter_items.unknown_item", id));
        }

        try {
            if (!nbt.isBlank())
                NbtPathArgumentType.nbtPath().parse(new StringReader(nbt.trim()));
        } catch (CommandSyntaxException | StringIndexOutOfBoundsException e) {
            list.add(Text.translatable("node.advanced_networking.filter_items.invalid_nbt_path", e.getMessage()));
        }

        list.addAll(super.validate());

        return list;
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var item = itemId.isBlank() ? null : Registries.ITEM.get(new Identifier(itemId));
        var tag = itemId.isBlank() ? null : TagKey.of(RegistryKeys.ITEM, new Identifier(itemId));
        NbtPathArgumentType.NbtPath path;
        try {
            path = nbt.isBlank() ? null : NbtPathArgumentType.nbtPath().parse(new StringReader(nbt.trim()));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Error while building nbt path not caught in validation", e);
        }

        var stream = inputs[0].getAs(ModDataTypes.ITEM_STREAM);
        stream.transform(new ItemTransformer.Filter(buildFilter(item, tag, path, whitelist)));
        return Either.left(new DataValue<?>[]{ ModDataTypes.ITEM_STREAM.makeValue(stream) });
    }

    @NotNull
    private Predicate<ItemVariant> buildFilter(Item item, TagKey<Item> tag, NbtPathArgumentType.NbtPath path, boolean whitelist) {
        return (itemVariant) -> {
            if (mode == Mode.ITEM && item != null && itemVariant.getItem() != item)
                return !whitelist;

            //noinspection deprecation
            if (mode == Mode.TAG && tag != null && !itemVariant.getItem().getRegistryEntry().isIn(tag))
                return !whitelist;

            if (path == null)
                return whitelist;
            if (path.count(itemVariant.getNbt()) > 0)
                return whitelist;
            return !whitelist;
        };
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        itemId = data.getString("itemId");
        nbt = data.getString("nbt");
        mode = Mode.byOrdinal(data.getInt("mode"));
        whitelist = data.getBoolean("whitelist");
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("itemId", itemId);
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
        ITEM, TAG;

        private static Mode byOrdinal(int ordinal) {
            return ordinal > 0 && ordinal < values().length ? values()[ordinal] : ITEM;
        }
    }

    protected class ConfigScreen extends NodeConfigScreen {
        public ConfigScreen(EditorScreen parent) {
            super(FilterItemsNode.this, parent);
        }

        @Override
        protected void init() {
            var x = ((width - 200) / 2) - 50;

            var whitelistButton = CyclingButtonWidget.onOffBuilder(Text.translatable("node.advanced_networking.filter_items.mode.whitelist"), Text.translatable("node.advanced_networking.filter_items.mode.blacklist"))
                    .initially(whitelist)
                    .omitKeyText()
                    .build(x, 70, 100, 20, Text.empty(), (button1, value) -> whitelist = value);
            addDrawableChild(whitelistButton);

            var button = CyclingButtonWidget.<Mode>builder(mode -> mode == Mode.ITEM ? Text.translatable("node.advanced_networking.filter_items.mode.item") : Text.translatable("node.advanced_networking.filter_items.mode.tag"))
                    .values(Mode.values())
                    .initially(mode)
                    .build(x, 95, 100, 20, Text.translatable("node.advanced_networking.filter_items.mode"), (button1, value) -> mode = value);
            addDrawableChild(button);

            var idField = new TextFieldWidget(textRenderer, x, 120, 100, 20, Text.empty());
            idField.setMaxLength(100);
            idField.setPlaceholder(Text.literal("id").formatted(Formatting.GRAY));
            idField.setText(itemId);
            idField.setChangedListener(newValue -> itemId = newValue);
            addDrawableChild(idField);

            var nbtField = new TextFieldWidget(textRenderer, x, 145, 100, 20, Text.empty());
            nbtField.setMaxLength(200);
            nbtField.setPlaceholder(Text.literal("nbt").formatted(Formatting.GRAY));
            nbtField.setText(nbt);
            nbtField.setChangedListener(newValue -> nbt = newValue);
            addDrawableChild(nbtField);
        }
    }
}
