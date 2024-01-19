package io.github.mattidragon.advancednetworking.graph.node.redstone;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.base.InterfaceNode;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.text.Text;

import java.util.List;

public class ReadRedstoneNode extends InterfaceNode {
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
        var positions = findInterfaces(world, context.get(NetworkControllerContext.TYPE).graphId());

        var power = positions.stream()
                .mapToInt(pos -> world.getEmittedRedstonePower(pos.pos().offset(pos.side()), pos.side()))
                .max()
                .orElse(0);

        return Either.left(new DataValue<?>[]{
                DataType.BOOLEAN.makeValue(power > 0),
                DataType.NUMBER.makeValue((double) power)
        });
    }
}
