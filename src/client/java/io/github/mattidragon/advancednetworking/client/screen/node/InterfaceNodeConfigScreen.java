package io.github.mattidragon.advancednetworking.client.screen.node;

import io.github.mattidragon.advancednetworking.client.mixin.CheckboxWidgetAccess;
import io.github.mattidragon.advancednetworking.graph.node.base.InterfaceNode;
import io.github.mattidragon.advancednetworking.misc.RequestInterfacesPacket;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.HandledEditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

public class InterfaceNodeConfigScreen<T extends InterfaceNode> extends NodeConfigScreen<T> {
    private final EditorScreen parent;
    private InterfaceList interfaceList;

    public InterfaceNodeConfigScreen(T owner, EditorScreen parent) {
        super(owner, parent);
        this.parent = parent;
    }

    @Override
    protected void init() {
        parent.syncGraph();

        interfaceList = addDrawableChild(new InterfaceList(client, width, height));

        var buf = PacketByteBufs.create();
        buf.writeByte(((HandledEditorScreen) parent).getScreenHandler().syncId);
        ClientPlayNetworking.send(RequestInterfacesPacket.ID, buf);
    }

    public void setInterfaces(Map<String, String> interfaces) {
        interfaceList.children().clear();
        if (interfaces.isEmpty())
            this.interfaceList.children().add(interfaceList.new MessageEntry(Text.translatable("node.advanced_networking.interface.no_interfaces")));

        for (var entry : interfaces.entrySet()) {
            var interfaceEntry = interfaceList.new InterfaceEntry(new Interface(entry.getKey(), entry.getValue()));
            this.interfaceList.children().add(interfaceEntry);
            if (entry.getKey().equals(owner.interfaceId))
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
            super(minecraftClient, 150, screenHeight - 50, 30, 25);
            setRenderBackground(false);

            setX(((screenWidth - 200) / 2) - this.width / 2);

            addEntry(new InterfaceList.MessageEntry(Text.translatable("node.advanced_networking.interface.loading")));
        }

        @Override
        protected int getScrollbarPositionX() {
            return getX() + width;
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        private abstract class Entry extends ElementListWidget.Entry<InterfaceList.Entry> {
        }

        private class MessageEntry extends InterfaceList.Entry {
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

        private class InterfaceEntry extends InterfaceList.Entry {
            private final Interface value;
            private final CheckboxWidget checkbox;

            private InterfaceEntry(Interface value) {
                this.value = value;
                var text = value.name.isBlank() ? value.id : value.name;
                this.checkbox = CheckboxWidget.builder(Text.literal(text), textRenderer)
                        .callback((clickedBox, checked) -> {
                            for (int i = 0; i < getEntryCount(); i++) {
                                if (getEntry(i) instanceof InterfaceList.InterfaceEntry entry && entry.checkbox != clickedBox) {
                                    ((CheckboxWidgetAccess) entry.checkbox).setChecked(false);
                                }
                            }

                            owner.interfaceId = value.id;
                        })
                        .build();
                checkbox.setWidth(150);
                checkbox.setHeight(20);
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
        }
    }
}
