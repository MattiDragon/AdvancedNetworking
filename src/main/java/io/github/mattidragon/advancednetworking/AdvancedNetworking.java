package io.github.mattidragon.advancednetworking;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.network.NetworkRegistry;
import io.github.mattidragon.advancednetworking.network.UpdateScheduler;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.advancednetworking.registry.ModItems;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.nodeflow.graph.GraphEnvironment;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class AdvancedNetworking implements ModInitializer {
    public static final String MOD_ID = "advanced_networking";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ExtendedScreenHandlerType<ControllerScreenHandler> CONTROLLER_SCREEN = new ExtendedScreenHandlerType<>(ControllerScreenHandler::new);
    public static final GraphEnvironment ENVIRONMENT = GraphEnvironment.builder()
            .addContextTypes(ContextType.SERVER_WORLD, ContextType.BLOCK_POS, ContextType.SERVER, NetworkControllerContext.TYPE)
            .addDataTypes(DataType.BOOLEAN, DataType.NUMBER, DataType.STRING)
            .addNodeGroups(NodeGroup.MATH, NodeGroup.ADVANCED_MATH, NodeGroup.LOGIC, ModNodeTypes.REDSTONE_GROUP)
            .addNodeTypes(NodeType.TIME)
            //.printDisableReasons()
            .build();

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registry.SCREEN_HANDLER, id("controller"), CONTROLLER_SCREEN);

        ModBlocks.register();
        ModItems.register();
        NetworkRegistry.register();
        UpdateScheduler.register();
        NetworkControllerContext.register();
        ModNodeTypes.register();

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("advanced_networking")
                        .then(CommandManager.literal("info")
                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                        .executes(context -> {
                                            var pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                            var controller = GraphLib.getController(context.getSource().getWorld());
                                            var message = new StringBuilder();
                                            for (long graph : controller.getGraphsAt(pos).toArray()) {
                                                message.append(graph).append("\n");
                                                for (var node : controller.getGraph(graph).getNodes().sorted(Comparator.comparing(node -> node.data().getNode().getTypeId())).toList()) {
                                                    message.append("  ")
                                                            .append(node.data().getPos().toShortString())
                                                            .append(" | ")
                                                            .append(node.data().getNode())
                                                            .append("\n");
                                                }
                                            }
                                            context.getSource().sendMessage(Text.literal(message.toString()));
                                            return 1;
                                        }))))));
    }
}
