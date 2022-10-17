package io.github.mattidragon.advancednetworking.graph.node;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.text.Text;

import java.util.List;

public class ReadRedstoneNode extends AbstractInterfaceNode {
    public ReadRedstoneNode(Graph graph) {
        super(ModNodeTypes.READ_REDSTONE, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[]{
                DataType.BOOLEAN.makeOptionalOutput("powered", this),
                DataType.NUMBER.makeOptionalOutput("power", this)
        };
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var world = context.get(ContextType.SERVER_WORLD);
        var pos = findInterface(world, context.get(NetworkControllerContext.TYPE).graphId());
        if (pos.isEmpty())
            return Either.right(Text.translatable("node.advanced_networking.interface.missing", interfaceId));

        var targetPos = pos.get().pos().offset(pos.get().side());
        int power = world.getEmittedRedstonePower(targetPos, pos.get().side());
        return Either.left(new DataValue<?>[]{
                DataType.BOOLEAN.makeValue(power > 0),
                DataType.NUMBER.makeValue((double) power)
        });
    }
}
