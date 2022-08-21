package io.github.mattidragon.advancednetworking.networking.node;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.networking.Connector;
import io.github.mattidragon.advancednetworking.networking.data.DataType;
import io.github.mattidragon.advancednetworking.networking.data.DataValue;
import io.github.mattidragon.advancednetworking.ui.screen.NetworkingScreen;
import io.github.mattidragon.advancednetworking.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MathNode extends Node {
    private final Connector<?>[] outputs = {DataType.NUMBER.makeConnector("result", true, this)};
    private final List<String> variables = new ArrayList<>();
    private String function = "a+b";

    {
        variables.add("a");
        variables.add("b");
    }

    public MathNode() {
        super(NodeType.MATH);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return outputs;
    }

    @Override
    public Connector<?>[] getInputs() {
        return variables.stream().map(name -> DataType.NUMBER.makeConnector(name, false, this)).toArray(Connector[]::new);
    }

    @Override
    public List<String> validate() {
        var counts = new HashMap<String, MutableInt>();
        for (var variable : variables) {
            counts.computeIfAbsent(variable, variable2 -> new MutableInt()).increment();
        }
        var variableErrors = new ArrayList<String>();
        counts.forEach((name, count) -> {
            if (name.isEmpty())
                variableErrors.add("Variable with empty name");
            else if (count.intValue() > 1)
                variableErrors.add("Multiple variables with name:" + name);
        });
        if (!variableErrors.isEmpty())
            return variableErrors;

        try {
            var exp = new ExpressionBuilder(function).variables(Set.copyOf(variables)).build();
            var result = exp.validate(false);
            if (!result.isValid())
                return result.getErrors();
        } catch (IllegalArgumentException e) {
            return List.of(e.getMessage().isBlank() ? "<unknown error>" : e.getMessage());
        } catch (EmptyStackException e) {
            return List.of("Mismatched parentheses (extra closing)");
        } catch (RuntimeException e) {
            AdvancedNetworking.LOGGER.warn("Math node encountered unexpected exception during validation", e);
            return List.of("Unexpected error: " + e);
        }
        return List.of();
    }

    @Override
    public DataValue<?>[] process(DataValue<?>[] inputs, ServerWorld world, BlockPos pos) {
        var expression = new ExpressionBuilder(function).variables(Set.copyOf(variables)).build();

        for (int i = 0; i < variables.size(); i++) {
            expression.setVariable(variables.get(i), inputs[i].getAs(DataType.NUMBER));
        }

        double result;
        try {
            result = expression.evaluate();
        } catch (ArithmeticException e) {
            result = 0;
        }
        return new DataValue[]{ DataType.NUMBER.makeValue(result) };
    }

    @Environment(EnvType.CLIENT)
    @Override
    public NodeConfigScreen createConfigScreen(NetworkingScreen parent) {
        return new ConfigScreen(parent);
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        function = data.getString("function");
        variables.clear();
        data.getList("variables", NbtElement.STRING_TYPE)
                .stream()
                .map(NbtString.class::cast)
                .map(NbtString::asString)
                .forEach(variables::add);
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("function", function);
        data.put("variables", variables.stream()
                .map(NbtString::of)
                .collect(Collectors.toCollection(NbtList::new)));
    }

    private class ConfigScreen extends NodeConfigScreen {

        private VariableList variableList;

        public ConfigScreen(NetworkingScreen parent) {
            super(MathNode.this, parent);
        }

        @Override
        protected void init() {
            super.init();
            var functionInput = addDrawableChild(new TextFieldWidget(textRenderer, 40, 40, width - 256, 20, Text.translatable("advanced_networking.node.math.function")));
            functionInput.setText(function);
            functionInput.setChangedListener(text -> function = text);

            variableList = addDrawableChild(new VariableList(client, width  - 256, 100, height - 40));
            variables.stream()
                    .map(name -> variableList.new Variable(name))
                    .forEach(variableList::addEntry);

            addDrawableChild(new ButtonWidget(40, 70, 100, 20, Text.translatable("advanced_networking.node.math.new_variable"), button -> {
                variableList.addEntryToTop(variableList.new Variable());
                variables.add(0, "");
            }));
        }

        @Override
        public void tick() {
            super.tick();
            variableList.children().forEach(child -> child.name.tick());
        }

        @Override
        public void setFocused(@Nullable Element focused) {
            if (focused != variableList) {
                var old = variableList.getFocused();
                if (old != null) {
                    old.name.setTextFieldFocused(false);
                }
            }
            super.setFocused(focused);
        }

        private class VariableList extends ElementListWidget<VariableList.Variable> {
            public VariableList(MinecraftClient minecraftClient, int width, int top, int bottom) {
                super(minecraftClient, width, bottom - top, top, bottom, 24);
                setRenderBackground(false);
                setRenderHeader(false, 0);
                setRenderHorizontalShadows(false);
                setLeftPos(40);
            }

            @Override
            public void setFocused(@Nullable Element focused) {
                var old = this.getFocused();
                if (old != null) {
                    old.name.setTextFieldFocused(false);
                }
                super.setFocused(focused);
            }

            @Override
            public int addEntry(Variable entry) {
                return super.addEntry(entry);
            }

            @Override
            public void addEntryToTop(Variable entry) {
                super.addEntryToTop(entry);
            }

            @Override
            public int getRowWidth() {
                return width - 40;
            }

            @Override
            protected int getScrollbarPositionX() {
                return left + width - 6;
            }

            @Override
            protected void renderList(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                // We need to scale from screen pixels to window pixels when using scissors
                var scale = client.getWindow().getScaleFactor();

                // Note to self: Y is bottom of screen for some reason
                RenderSystem.enableScissor(0, (int) ((ConfigScreen.this.height - bottom) * scale), client.getWindow().getWidth(), (int) (height * scale));
                super.renderList(matrices, mouseX, mouseY, delta);
                RenderSystem.disableScissor();
            }

            private class Variable extends ElementListWidget.Entry<Variable> {
                private final ButtonWidget removeButton;
                private final TextFieldWidget name;
                private final List<ClickableWidget> children;

                private Variable() {
                    removeButton = new ButtonWidget( getRowLeft() + getRowWidth() - 20, 0, 20, 20, Text.literal("X"), button -> {
                        variables.remove(VariableList.this.children().indexOf(this));
                        removeEntry(this);
                    });
                    name = new TextFieldWidget(textRenderer, getRowLeft(), 0, getRowWidth() - 40, 18, Text.literal("Hello"));
                    name.setChangedListener(newName -> {
                        var index = VariableList.this.children().indexOf(this);
                        if (index != -1)
                            variables.set(index, newName.trim());
                    });
                    name.setFocusUnlocked(true);
                    children = new ArrayList<>(List.of(removeButton, name));
                }

                private Variable(String name) {
                    this();
                    this.name.setText(name);
                }

                @Override
                public List<? extends Selectable> selectableChildren() {
                    return children;
                }

                @Override
                public List<? extends Element> children() {
                    return children;
                }

                @Override
                public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                    DrawableHelper.fill(matrices, x, y, x + entryWidth, y + entryHeight, 0x55000000);
                    removeButton.y = y;
                    name.y = y + 1;
                    removeButton.render(matrices, mouseX, mouseY, tickDelta);
                    name.render(matrices, mouseX, mouseY, tickDelta);
                }
            }
        }
    }
}
