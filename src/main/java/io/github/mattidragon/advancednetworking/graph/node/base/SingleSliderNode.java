package io.github.mattidragon.advancednetworking.graph.node.base;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.text.Text;

import java.util.List;

public abstract class SingleSliderNode extends Node {
    protected SingleSliderNode(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        super(type, contexts, graph);
    }

    public abstract Text getSliderText();

    public abstract int getMin();

    public abstract int getMax();

    public abstract int getValue();

    public abstract void setValue(int value);
}
