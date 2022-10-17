package io.github.mattidragon.advancednetworking.graph;

import io.github.mattidragon.advancednetworking.graph.node.ReadRedstoneNode;
import io.github.mattidragon.advancednetworking.graph.node.SetRedstoneNode;
import io.github.mattidragon.advancednetworking.graph.node.WriteRedstoneNode;
import io.github.mattidragon.nodeflow.graph.node.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.minecraft.text.Text;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public class ModNodeTypes {
    public static final NodeType<ReadRedstoneNode> READ_REDSTONE = new NodeType<>(ReadRedstoneNode::new);
    public static final NodeType<WriteRedstoneNode> WRITE_REDSTONE = new NodeType<>(WriteRedstoneNode::new);
    public static final NodeType<SetRedstoneNode> SET_REDSTONE = new NodeType<>(SetRedstoneNode::new);
    public static final NodeGroup REDSTONE_GROUP = new NodeGroup(Text.translatable("group.advanced_networking.redstone"), READ_REDSTONE, WRITE_REDSTONE, SET_REDSTONE);

    public static void register() {
        NodeType.register(READ_REDSTONE, id("read_redstone"));
        NodeType.register(WRITE_REDSTONE, id("write_redstone"));
        NodeType.register(SET_REDSTONE, id("set_redstone"));
    }
}
