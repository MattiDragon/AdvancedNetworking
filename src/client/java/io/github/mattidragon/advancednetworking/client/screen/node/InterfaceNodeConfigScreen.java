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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    public void setInterfaces(Map<String, Text> interfaces, Map<String, List<String>> groups) {
        interfaceList.children().clear();
        if (interfaces.isEmpty()) {
            this.interfaceList.children().add(interfaceList.new MessageEntry(Text.translatable("node.advanced_networking.interface.no_interfaces")));
            return;
        }

        interface Sortable extends Comparable<Sortable> {
            String getName();

            @Override
            default int compareTo(@NotNull Sortable o) {
                return getName().compareTo(o.getName());
            }
        }

        record Single(String id, Text name) implements Sortable {
            @Override
            public String getName() {
                return name.getString();
            }
        }

        record Group(String id, List<Single> interfaces) implements Sortable {
            @Override
            public String getName() {
                return id;
            }
        }

        var entries = new ArrayList<Sortable>();
        interfaces.keySet()
                .stream()
                .filter(id -> groups.values().stream().noneMatch(group -> group.contains(id)))
                .forEach(id -> entries.add(new Single(id, interfaces.get(id))));

        groups.forEach((id, groupEntries) -> {
            var filtered = groupEntries.stream().filter(interfaces::containsKey).toList();
            if (filtered.isEmpty()) return;
            var convertedEntries = filtered.stream()
                    .map(entry -> new Single(entry, interfaces.get(entry)))
                    .sorted()
                    .toList();
            entries.add(new Group(id, convertedEntries));
        });

        entries.sort(null);

        for (var entry : entries) {
            if (entry instanceof Single single) {
                var added = interfaceList.new InterfaceEntry(single.id(), single.name(), false, false);
                interfaceList.children().add(added);
                if (!owner.isGroup && owner.interfaceId.equals(single.id)) {
                    ((CheckboxWidgetAccess) added.checkbox).setChecked(true);
                }
            } else if (entry instanceof Group group) {
                var groupEntry = interfaceList.new InterfaceEntry(group.id(), Text.literal(group.id()), true, false);
                interfaceList.children().add(groupEntry);
                for (var child : group.interfaces()) {
                    var childEntry = interfaceList.new InterfaceEntry(child.id(), child.name(), false, true);
                    interfaceList.children().add(childEntry);
                    if (!owner.isGroup && owner.interfaceId.equals(child.id)) {
                        ((CheckboxWidgetAccess) childEntry.checkbox).setChecked(true);
                    }
                }
                if (owner.isGroup && owner.interfaceId.equals(group.id)) {
                    ((CheckboxWidgetAccess) groupEntry.checkbox).setChecked(true);
                }
            }
        }
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
            private final CheckboxWidget checkbox;
            private final boolean isUnderGroup;

            private InterfaceEntry(String id, Text name, boolean isGroup, boolean isUnderGroup) {
                this.checkbox = CheckboxWidget.builder(name, textRenderer)
                        .callback((clickedBox, checked) -> {
                            for (int i = 0; i < getEntryCount(); i++) {
                                if (getEntry(i) instanceof InterfaceList.InterfaceEntry entry && entry.checkbox != clickedBox) {
                                    ((CheckboxWidgetAccess) entry.checkbox).setChecked(false);
                                }
                            }

                            owner.interfaceId = id;
                            owner.isGroup = isGroup;
                        })
                        .build();
                checkbox.setWidth(isUnderGroup ? 130 : 150);
                checkbox.setHeight(20);
                this.isUnderGroup = isUnderGroup;
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
                checkbox.setX(isUnderGroup ? x + 20 : x);
                checkbox.setY(y);
                checkbox.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}
