package io.github.mattidragon.advancednetworking.graph.node.redstone;

import com.mojang.datafixers.util.Either;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.InterfaceNode;
import io.github.mattidragon.nodeflow.graph.Connector;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.data.DataValue;
import net.minecraft.text.Text;

import java.util.List;

public class SetRedstoneNode extends InterfaceNode {
    public SetRedstoneNode(Graph graph) {
        super(ModNodeTypes.SET_REDSTONE, List.of(ContextType.SERVER_WORLD, NetworkControllerContext.TYPE), graph);
    }

    @Override
    public Connector<?>[] getInputs() {
        return new Connector[]{DataType.BOOLEAN.makeRequiredInput("enabled", this)};
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[0];
    }

    @Override
    protected Either<DataValue<?>[], Text> process(DataValue<?>[] inputs, ContextProvider context) {
        var world = context.get(ContextType.SERVER_WORLD);
        var optionalPos = findInterface(world, context.get(NetworkControllerContext.TYPE).graphId());
        if (optionalPos.isEmpty())
            return Either.right(Text.translatable("node.advanced_networking.interface.missing", interfaceId));

        var pos = optionalPos.get().pos();
        var side = optionalPos.get().side();

        var enabled = inputs[0].getAs(DataType.BOOLEAN);

        var state = world.getBlockState(pos);
        world.setBlockState(pos, state.with(CableBlock.FACING_PROPERTIES.get(side), enabled ? CableBlock.ConnectionType.INTERFACE_POWERED : CableBlock.ConnectionType.INTERFACE));
        if (world.getBlockEntity(pos) instanceof CableBlockEntity cable)  {
            cable.setPower(side, enabled ? 15 : 0);
        }

        return Either.left(new DataValue<?>[0]);
    }
}
