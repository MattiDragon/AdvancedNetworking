package io.github.mattidragon.advancednetworking.client;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.client.screen.ControllerScreen;
import io.github.mattidragon.advancednetworking.misc.RequestInterfacesPacket;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.nodeflow.compat.controlify.ControlifyProxy;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
import io.github.mattidragon.nodeflow.ui.screen.HandledEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AdvancedNetworkingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.<EditorScreenHandler, HandledEditorScreen>register(AdvancedNetworking.CONTROLLER_SCREEN, (handler, inventory, title) -> new ControllerScreen((ControllerScreenHandler) handler, inventory, title));
        RequestInterfacesPacket.registerClient();
        ControlifyProxy.INSTANCE.registerScreenType(ControllerScreen.class);
    }
}
