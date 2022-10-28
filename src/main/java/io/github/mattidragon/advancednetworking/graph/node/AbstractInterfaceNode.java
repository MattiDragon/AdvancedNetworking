package io.github.mattidragon.advancednetworking.graph.node;

import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.graph.SidedBlockNode;
import com.kneelawk.graphlib.util.SidedPos;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public abstract class AbstractInterfaceNode extends Node {
    protected String interfaceId = "";

    protected AbstractInterfaceNode(NodeType<?> type, List<ContextType<?>> contexts, Graph graph) {
        super(type, contexts, graph);
    }

    protected final Optional<SidedPos> findInterface(ServerWorld world, long graphId) {
        var graph = GraphLib.getController(world).getGraph(graphId);
        if (graph == null)
            return Optional.empty();

        return graph.getNodes()
                .map(com.kneelawk.graphlib.graph.struct.Node::data)
                .filter(node -> node.getNode() instanceof SidedBlockNode)
                .map(node -> new SidedPos(node.getPos(), ((SidedBlockNode) node.getNode()).getSide()))
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

    protected class ConfigScreen extends NodeConfigScreen {
        public ConfigScreen(EditorScreen parent) {
            super(AbstractInterfaceNode.this, parent);
        }

        @Override
        protected void init() {
            var x = ((width - 200) / 2) - 50;
            var textField = addDrawableChild(new TextFieldWidget(textRenderer, x, 70, 100, 20, Text.empty()));
            textField.setText(interfaceId);
            textField.setChangedListener(newValue -> interfaceId = newValue);
        }
    }
}
