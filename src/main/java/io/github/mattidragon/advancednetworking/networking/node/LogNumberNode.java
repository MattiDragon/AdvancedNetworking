package io.github.mattidragon.advancednetworking.networking.node;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.networking.Connector;
import io.github.mattidragon.advancednetworking.networking.data.DataType;
import io.github.mattidragon.advancednetworking.networking.data.DataValue;
import io.github.mattidragon.advancednetworking.ui.screen.NetworkingScreen;
import io.github.mattidragon.advancednetworking.ui.screen.NodeConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class LogNumberNode extends Node {
    private final Connector<?>[] inputs = new Connector[] { DataType.NUMBER.makeConnector("input", false, this) };

    public LogNumberNode() {
        super(NodeType.DEBUG);
    }

    @Override
    public Connector<?>[] getOutputs() {
        return new Connector[0];
    }

    @Override
    public Connector<?>[] getInputs() {
        return inputs;
    }

    @Override
    public DataValue<?>[] process(DataValue<?>[] inputs, ServerWorld world, BlockPos pos) {
        world.getPlayers(player -> pos.getSquaredDistance(player.getPos()) < 16 * 16).forEach(player -> {
            player.sendMessage(Text.translatable("advanced_networking.node.debug.message", inputs[0].getAs(DataType.NUMBER)));
        });

        return new DataValue[0];
    }
}
