package io.github.mattidragon.advancednetworking;

import io.github.mattidragon.advancednetworking.config.ConfigData;
import io.github.mattidragon.advancednetworking.graph.ModDataTypes;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.misc.RequestInterfacesPacket;
import io.github.mattidragon.advancednetworking.misc.ScreenPosSyncPacket;
import io.github.mattidragon.advancednetworking.misc.UpdateInterfacePacket;
import io.github.mattidragon.advancednetworking.network.NetworkRegistry;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.advancednetworking.registry.ModItems;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.configloader.api.ConfigManager;
import io.github.mattidragon.nodeflow.graph.GraphEnvironment;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.graph.data.DataType;
import io.github.mattidragon.nodeflow.graph.node.NodeGroup;
import io.github.mattidragon.nodeflow.graph.node.NodeType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedNetworking implements ModInitializer {
    public static final String MOD_ID = "advanced_networking";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigManager<ConfigData> CONFIG = ConfigManager.create(ConfigData.CODEC, ConfigData.DEFAULT, MOD_ID);
    public static final ExtendedScreenHandlerType<ControllerScreenHandler> CONTROLLER_SCREEN = new ExtendedScreenHandlerType<>(ControllerScreenHandler::new);
    public static final GraphEnvironment ENVIRONMENT = GraphEnvironment.builder()
            .addContextTypes(ContextType.SERVER_WORLD, ContextType.BLOCK_POS, ContextType.SERVER, NetworkControllerContext.TYPE)
            .addDataTypes(DataType.BOOLEAN, DataType.NUMBER, ModDataTypes.ITEM_STREAM, ModDataTypes.FLUID_STREAM, ModDataTypes.ENERGY_STREAM)
            .addNodeGroups(NodeGroup.MATH, NodeGroup.ADVANCED_MATH, NodeGroup.LOGIC, NodeGroup.FLOW, NodeGroup.COMPARE_NUMBER, NodeGroup.CONSTANTS)
            .addNodeGroups(ModNodeTypes.REDSTONE_GROUP, ModNodeTypes.ITEM_GROUP, ModNodeTypes.ENERGY_GROUP, ModNodeTypes.FLUID_GROUP)
            .addNodeTypes(NodeType.TIME)
            //.printDisableReasons()
            .build();

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.SCREEN_HANDLER, id("controller"), CONTROLLER_SCREEN);
        CONFIG.get();

        ModBlocks.register();
        ModItems.register();
        ModNodeTypes.register();
        ModDataTypes.register();
        NetworkRegistry.register();
        ScreenPosSyncPacket.register();
        UpdateInterfacePacket.register();
        RequestInterfacesPacket.register();
        NetworkControllerContext.register();
    }
}
