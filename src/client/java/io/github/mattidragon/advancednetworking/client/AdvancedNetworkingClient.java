package io.github.mattidragon.advancednetworking.client;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.client.screen.ControllerScreen;
import io.github.mattidragon.advancednetworking.client.screen.ResourceFilterConfigScreen;
import io.github.mattidragon.advancednetworking.client.screen.node.CountNodeConfigScreen;
import io.github.mattidragon.advancednetworking.client.screen.node.InterfaceNodeConfigScreen;
import io.github.mattidragon.advancednetworking.client.screen.node.SliderNodeConfigScreen;
import io.github.mattidragon.advancednetworking.graph.ModNodeTypes;
import io.github.mattidragon.advancednetworking.misc.RequestInterfacesPacket;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.nodeflow.client.compat.controlify.ControlifyProxy;
import io.github.mattidragon.nodeflow.client.ui.NodeConfigScreenRegistry;
import io.github.mattidragon.nodeflow.client.ui.screen.HandledEditorScreen;
import io.github.mattidragon.nodeflow.screen.EditorScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.network.PacketByteBuf;

public class AdvancedNetworkingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.<EditorScreenHandler, HandledEditorScreen>register(AdvancedNetworking.CONTROLLER_SCREEN, (handler, inventory, title) -> new ControllerScreen((ControllerScreenHandler) handler, inventory, title));
        ControlifyProxy.INSTANCE.registerScreenType(ControllerScreen.class);

        NodeConfigScreenRegistry.register(InterfaceNodeConfigScreen::new,
                ModNodeTypes.READ_REDSTONE,
                ModNodeTypes.SET_REDSTONE,
                ModNodeTypes.WRITE_REDSTONE,
                ModNodeTypes.ITEM_SOURCE,
                ModNodeTypes.ITEM_TARGET,
                ModNodeTypes.ITEM_CAPACITY,
                ModNodeTypes.ENERGY_SOURCE,
                ModNodeTypes.ENERGY_TARGET,
                ModNodeTypes.ENERGY_CAPACITY,
                ModNodeTypes.ENERGY_AMOUNT,
                ModNodeTypes.FLUID_SOURCE,
                ModNodeTypes.FLUID_TARGET,
                ModNodeTypes.FLUID_CAPACITY
        );
        NodeConfigScreenRegistry.register(CountNodeConfigScreen::new, ModNodeTypes.ITEM_COUNT, ModNodeTypes.FLUID_COUNT);

        NodeConfigScreenRegistry.register(SliderNodeConfigScreen::new,
                ModNodeTypes.LIMIT_ITEMS,
                ModNodeTypes.LIMIT_FLUID,
                ModNodeTypes.LIMIT_ENERGY,
                ModNodeTypes.SPLIT_ITEMS,
                ModNodeTypes.SPLIT_FLUID,
                ModNodeTypes.SPLIT_ENERGY,
                ModNodeTypes.MERGE_ITEMS,
                ModNodeTypes.MERGE_FLUID,
                ModNodeTypes.MERGE_ENERGY
        );
        NodeConfigScreenRegistry.register((node, editorScreen) -> new ResourceFilterConfigScreen<>(node, editorScreen, node.getFilter()),
                ModNodeTypes.FILTER_FLUID, ModNodeTypes.FILTER_ITEMS);
        NodeConfigScreenRegistry.register(CountNodeConfigScreen::new,
                ModNodeTypes.ITEM_COUNT,
                ModNodeTypes.FLUID_COUNT);

        ClientPlayNetworking.registerGlobalReceiver(RequestInterfacesPacket.RESPONSE_ID, (client, handler, buf, responseSender) -> {
            var syncId = buf.readByte();
            var interfaces = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readString);

            client.execute(() -> {
                if (client.player != null && client.player.currentScreenHandler.syncId == syncId && client.currentScreen instanceof InterfaceNodeConfigScreen<?> configScreen) {
                    configScreen.setInterfaces(interfaces);
                }
            });
        });
    }
}
