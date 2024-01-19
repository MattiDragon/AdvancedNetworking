package io.github.mattidragon.advancednetworking.graph.node.redstone;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
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

public class WriteRedstoneNode extends InterfaceNode {
    public WriteRedstoneNode(Graph graph) {
        super(ModNodeTypes.WRITE_REDSTONE, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[]{DataType.NUMBER.makeRequiredInput("power", this)};
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var world = context.get(ContextType.SERVER_WORLD);
        var positions = findInterfaces(world, context.get(NetworkControllerContext.TYPE).graphId());
        var power = (int)(double) inputs[0].getAs(DataType.NUMBER);

        for (var position : positions) {
            var pos = position.pos();
            var side = position.side();

            var state = world.getBlockState(pos);
            world.setBlockState(pos, state.with(CableBlock.FACING_PROPERTIES.get(side), power > 0 ? CableBlock.ConnectionType.INTERFACE_POWERED : CableBlock.ConnectionType.INTERFACE));
            if (world.getBlockEntity(pos) instanceof CableBlockEntity cable) {
                cable.setPower(side, power);
            }
        }

        return Either.left(new DataValue<?>[0]);
    }
}
