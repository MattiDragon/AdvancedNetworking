package io.github.mattidragon.advancednetworking.client.screen.node;

import io.github.mattidragon.advancednetworking.graph.node.base.CountNode;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;

public class CountNodeInterfaceSelectionScreen<N extends CountNode<?, ?>> extends InterfaceNodeConfigScreen<N> {
    private final CountNodeConfigScreen<N> configScreen;

    public CountNodeInterfaceSelectionScreen(N owner, EditorScreen parent, CountNodeConfigScreen<N> configScreen) {
        super(owner, parent);
        this.configScreen = configScreen;
    }

    @Override
    public void close() {
        client.setScreen(this.configScreen);
    }
}
