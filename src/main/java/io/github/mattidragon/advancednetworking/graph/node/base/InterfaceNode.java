package io.github.mattidragon.advancednetworking.graph.node.base;

import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.SidedPos;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.misc.RequestInterfacesPacket;
import io.github.mattidragon.advancednetworking.mixin.CheckboxWidgetAccess;
import io.github.mattidragon.advancednetworking.network.NetworkRegistry;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.HandledEditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class InterfaceNode extends Node {
    public String interfaceId = "";

    protected InterfaceNode(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        super(type, contexts, graph);
    }

    protected final Optional<SidedPos> findInterface(ServerWorld world, long graphId) {
        var graph = NetworkRegistry.UNIVERSE.getServerGraphWorld(world).getGraph(graphId);
        if (graph == null)
            return Optional.empty();

        return graph.getNodes()
                .filter(node -> node.getNode() instanceof SidedBlockNode)
                .map(node -> new SidedPos(node.getBlockPos(), node.cast(SidedBlockNode.class).getNode().getSide()))
                .filter(pos -> interfaceId.equals(CableBlock.calcInterfaceId(pos.pos(), pos.side())))
                .findFirst();
    }

    @Override
    public void readNbt(NbtCompound data) {
        super.readNbt(data);
        interfaceId = data.getString("interfaceId");
    }

    @Override
    public void writeNbt(NbtCompound data) {
        super.writeNbt(data);
        data.putString("interfaceId", interfaceId);
    }

    @Override
    public List<Text> validate() {
        return interfaceId.length() == 12 ? List.of() : List.of(Text.translatable("node.advanced_networking.interface.invalid"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public NodeConfigScreen createConfigScreen(EditorScreen parent) {
        return new ConfigScreen(parent);
    }

    @Override
    public final boolean hasConfig() {
        return true;
    }

    public class ConfigScreen extends NodeConfigScreen {
        private final EditorScreen parent;
        private InterfaceList interfaceList;

        public ConfigScreen(EditorScreen parent) {
            super(InterfaceNode.this, parent);
            this.parent = parent;
        }

        @Override
        protected void init() {
            parent.syncGraph();

            interfaceList = addDrawableChild(new InterfaceList(client, width, height));
            RequestInterfacesPacket.send(((HandledEditorScreen) parent).getScreenHandler().syncId);
        }

        public void setInterfaces(Map<String, String> interfaces) {
            interfaceList.children().clear();
            if (interfaces.isEmpty())
                this.interfaceList.children().add(interfaceList.new MessageEntry(Text.translatable("node.advanced_networking.interface.no_interfaces")));

            for (var entry : interfaces.entrySet()) {
                var interfaceEntry = interfaceList.new InterfaceEntry(new Interface(entry.getKey(), entry.getValue()));
                this.interfaceList.children().add(interfaceEntry);
                if (entry.getKey().equals(InterfaceNode.this.interfaceId))
                    interfaceEntry.checkbox.onPress();
            }
            interfaceList.children().sort((first, second) -> {
                if (first instanceof InterfaceList.InterfaceEntry firstInterface) {
                    if (second instanceof InterfaceList.InterfaceEntry secondInterface) {
                        if (firstInterface.value.name.isBlank() && secondInterface.value.name.isBlank())
                            return firstInterface.value.id.compareTo(secondInterface.value.id);
                        if (firstInterface.value.name.isBlank())
                            return 1;
                        if (secondInterface.value.name.isBlank())
                            return -1;
                        return firstInterface.value.name.compareTo(secondInterface.value.name);
                    }
                    return -1;
                }
                return second instanceof InterfaceList.InterfaceEntry ? 1 : 0;
            });
        }

        record Interface(String id, String name) {
        }

        private class InterfaceList extends ElementListWidget<InterfaceList.Entry> {
            public InterfaceList(MinecraftClient minecraftClient, int screenWidth, int screenHeight) {
                super(minecraftClient, 150, screenHeight - 50, 30, screenHeight - 20, 25);
                setRenderHorizontalShadows(false);
                setRenderBackground(false);

                setLeftPos(((screenWidth - 200) / 2) - this.width / 2);

                addEntry(new MessageEntry(Text.translatable("node.advanced_networking.interface.loading")));
            }

            @Override
            protected int getScrollbarPositionX() {
                return left + width;
            }

            @Override
            public int getRowWidth() {
                return width;
            }

            private abstract static class Entry extends ElementListWidget.Entry<Entry> {
            }

            private class MessageEntry extends Entry {
                private final Text message;

                private MessageEntry(Text message) {
                    this.message = message;
                }

                @Override
                public List<? extends Selectable> selectableChildren() {
                    return List.of();
                }

                @Override
                public List<? extends Element> children() {
                    return List.of();
                }

                @Override
                public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                    context.drawCenteredTextWithShadow(InterfaceList.this.client.textRenderer, message, x + entryWidth / 2, y, 0xaaaaaa);
                }
            }

            private class InterfaceEntry extends Entry {
                private final Interface value;
                private final CheckBox checkbox;

                private InterfaceEntry(Interface value) {
                    this.value = value;
                    var text = value.name.isBlank() ? value.id : value.name;
                    this.checkbox = new CheckBox(0, 0, 150, 20, Text.literal(text), false);
                }

                @Override
                public List<? extends Selectable> selectableChildren() {
                    return List.of(checkbox);
                }

                @Override
                public List<? extends Element> children() {
                    return List.of(checkbox);
                }

                @Override
                public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                    checkbox.setX(x);
                    checkbox.setY(y);
                    checkbox.render(context, mouseX, mouseY, tickDelta);
                }

                private class CheckBox extends CheckboxWidget {
                    public CheckBox(int x, int y, int width, int height, Text message, boolean checked) {
                        super(x, y, width, height, message, checked);
                    }

                    @Override
                    public void onPress() {
                        for (int i = 0; i < getEntryCount(); i++) {
                            if (getEntry(i) instanceof InterfaceEntry entry)
                                ((CheckboxWidgetAccess) entry.checkbox).setChecked(false);
                        }

                        InterfaceNode.this.interfaceId = value.id;

                        super.onPress();
                    }
                }
            }
        }
    }
}
