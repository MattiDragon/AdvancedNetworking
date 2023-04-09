package io.github.mattidragon.advancednetworking.graph.node.base;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.config.AdvancedNetworkingConfig;
import io.github.mattidragon.advancednetworking.graph.path.PathBundle;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class FilterResourceNode<R, T> extends Node {
    private String idFilter = "";
    private String nbtFilter = "";
    private Mode mode = Mode.RESOURCE;
    private boolean useRegex = false;
    private boolean isWhitelist = true;

    public FilterResourceNode(NodeType<? extends FilterResourceNode<R, T>> type, Graph graph) {
        super(type, List.of(), graph);
    }

    protected abstract DataType<PathBundle<Storage<R>, T>> getDataType();

    @Nullable
    protected abstract NbtCompound getNbt(R resource);

    protected abstract Identifier getId(R resource);

    protected abstract T createTransformer(Predicate<R> predicate);

    protected abstract RegistryEntry<?> getRegistryEntry(R resource);

    protected abstract Registry<?> getRegistry();

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[] { getDataType().makeRequiredOutput("out", this) };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[] { getDataType().makeRequiredInput("in", this) };
    }

    @Override
    public List<Text> validate() {
        var list = new ArrayList<Text>();
        if (!idFilter.isBlank()) {
            if (shouldUseRegex()) {
                try {
                    Pattern.compile(idFilter);
                } catch (PatternSyntaxException e) {
                    list.add(Text.translatable("node.advanced_networking.filter.invalid_id_regex", e.getDescription(), e.getIndex()));
                }
            } else {
                var id = Identifier.tryParse(idFilter.trim());
                if (id == null) {
                    list.add(Text.translatable("node.advanced_networking.filter.invalid_id", idFilter));
                } else {
                    if (mode == Mode.RESOURCE && !getRegistry().containsId(id))
                        list.add(Text.translatable("node.advanced_networking.filter.unknown_resource", id));
                    if (mode == Mode.TAG && getRegistry().streamTags().map(TagKey::id).noneMatch(id::equals))
                        list.add(Text.translatable("node.advanced_networking.filter.unknown_tag", id));
                }
            }
        }

        try {
            if (!nbtFilter.isBlank())
                NbtPathArgumentType.nbtPath().parse(new StringReader(nbtFilter.trim()));
        } catch (CommandSyntaxException | StringIndexOutOfBoundsException e) {
            list.add(Text.translatable("node.advanced_networking.filter.invalid_nbt_path", e.getMessage()));
        }

        list.addAll(super.validate());

        return list;
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var stream = inputs[0].getAs(getDataType());
        stream.transform(createTransformer(this::isAllowed));
        return Either.left(new DataValue<?>[]{ getDataType().makeValue(stream) });
    }

    private boolean isAllowed(R resource) {
        NbtPathArgumentType.NbtPath nbtPath;
        try {
            nbtPath = nbtFilter.isBlank() ? null : NbtPathArgumentType.nbtPath().parse(new StringReader(nbtFilter.trim()));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Error while building nbt path not caught in validation", e);
        }

        var idMatches = idFilter.isBlank() || switch (mode) {
            case RESOURCE -> checkId(getId(resource));
            case TAG -> getRegistryEntry(resource)
                        .streamTags()
                        .map(TagKey::id)
                        .anyMatch(this::checkId);
        };
        var nbtMatches = nbtPath == null || nbtPath.count(getNbt(resource)) > 0;
        var matches = idMatches && nbtMatches;

        if (isWhitelist) {
            return matches;
        } else {
            return !matches;
        }
    }

    private boolean checkId(Identifier id) {
        if (shouldUseRegex()) {
            return Pattern.matches(idFilter, id.toString());
        } else {
            return new Identifier(idFilter).equals(id);
        }
    }

    private boolean shouldUseRegex() {
        return useRegex && !AdvancedNetworkingConfig.DISABLE_REGEX_FILTERING.get();
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);

        // Can't go breaking old saves
        if (data.contains("itemId", NbtElement.STRING_TYPE)) {
            idFilter = data.getString("itemId");
        } else if (data.contains("fluidId", NbtElement.STRING_TYPE)) {
            idFilter = data.getString("fluidId");
        } else {
            idFilter = data.getString("idFilter");
        }

        // Can't go breaking old saves
        if (data.contains("nbt", NbtElement.STRING_TYPE)) {
            nbtFilter = data.getString("nbt");
        } else {
            nbtFilter = data.getString("nbtFilter");
        }

        mode = Mode.byOrdinal(data.getInt("mode"));
        isWhitelist = data.getBoolean("whitelist");
        useRegex = data.getBoolean("regex");
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("idFilter", idFilter);
        data.putString("nbtFilter", nbtFilter);
        data.putInt("mode", mode.ordinal());
        data.putBoolean("whitelist", isWhitelist);
        data.putBoolean("regex", useRegex);
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
        RESOURCE, TAG;

        private static Mode byOrdinal(int ordinal) {
            return ordinal > 0 && ordinal < values().length ? values()[ordinal] : RESOURCE;
        }
    }

    protected class ConfigScreen extends NodeConfigScreen {
        public ConfigScreen(EditorScreen parent) {
            super(FilterResourceNode.this, parent);
        }

        @Override
        protected void init() {
            var x = ((width - 200) / 2) - 50;

            var regexButton = CyclingButtonWidget.onOffBuilder()
                    .initially(shouldUseRegex())
                    .tooltip(value -> {
                        if (AdvancedNetworkingConfig.DISABLE_REGEX_FILTERING.get()) {
                            return List.of(Text.translatable("node.advanced_networking.filter.use_regex.disabled").asOrderedText());
                        } else return List.of();
                    })
                    .build(x, 45, 100, 20, Text.translatable("node.advanced_networking.filter.use_regex"), (button1, value) -> useRegex = value);
            if (AdvancedNetworkingConfig.DISABLE_REGEX_FILTERING.get()) {
                regexButton.active = false;
            }
            addDrawableChild(regexButton);

            var whitelistButton = CyclingButtonWidget.onOffBuilder(Text.translatable("node.advanced_networking.filter.mode.whitelist"), Text.translatable("node.advanced_networking.filter.mode.blacklist"))
                    .initially(isWhitelist)
                    .omitKeyText()
                    .build(x, 70, 100, 20, Text.empty(), (button1, value) -> isWhitelist = value);
            addDrawableChild(whitelistButton);

            var button = CyclingButtonWidget.<Mode>builder(mode -> mode == Mode.RESOURCE ? Text.translatable("node.advanced_networking.filter.mode.resource") : Text.translatable("node.advanced_networking.filter.mode.tag"))
                    .values(Mode.values())
                    .initially(mode)
                    .build(x, 95, 100, 20, Text.translatable("node.advanced_networking.filter.mode"), (button1, value) -> mode = value);
            addDrawableChild(button);

            var idField = new TextFieldWidget(textRenderer, x, 120, 100, 20, Text.empty());
            idField.setMaxLength(100);
            idField.setSuggestion(idFilter.isEmpty() ? "id" : "");
            idField.setText(idFilter);
            idField.setChangedListener(newValue -> {
                idFilter = newValue;
                idField.setSuggestion(newValue.isEmpty() ? "id" : "");
            });
            addDrawableChild(idField);

            var nbtField = new TextFieldWidget(textRenderer, x, 145, 100, 20, Text.empty());
            nbtField.setMaxLength(200);
            idField.setSuggestion(nbtFilter.isEmpty() ? "nbt" : "");
            nbtField.setText(nbtFilter);
            nbtField.setChangedListener(newValue -> {
                nbtFilter = newValue;
                idField.setSuggestion(newValue.isEmpty() ? "nbt" : "");
            });
            addDrawableChild(nbtField);
        }
    }
}
