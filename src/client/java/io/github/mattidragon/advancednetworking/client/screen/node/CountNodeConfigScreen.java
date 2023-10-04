package io.github.mattidragon.advancednetworking.client.screen.node;

import io.github.mattidragon.advancednetworking.client.screen.ResourceFilterConfigScreen;
import io.github.mattidragon.advancednetworking.graph.node.base.CountNode;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class CountNodeConfigScreen<N extends CountNode<?, ?>> extends ResourceFilterConfigScreen<N> {
    private final EditorScreen parent;

    public CountNodeConfigScreen(N owner, EditorScreen parent) {
        super(owner, parent, owner.getFilter());
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        var x = ((width - 200) / 2) - 50;
        var chooseInterfaceButton = ButtonWidget.builder(Text.translatable("node.advanced_networking.item_count.choose_interface"), button -> client.setScreen(new CountNodeInterfaceSelectionScreen<>(owner, parent, this)))
                .width(100)
                .position(x, 175)
                .build();
        addDrawableChild(chooseInterfaceButton);
    }
}
