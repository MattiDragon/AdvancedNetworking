package io.github.mattidragon.advancednetworking.registry;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.ui.screen.handler.NetworkingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;

public final class ModScreens {
    public static final ScreenHandlerType<NetworkingScreenHandler> NETWORKING_HANDLER = new ExtendedScreenHandlerType<>(NetworkingScreenHandler::new);

    private ModScreens() { throw new UnsupportedOperationException(); }

    public static void register() {
        Registry.register(Registry.SCREEN_HANDLER, AdvancedNetworking.id("networking"), NETWORKING_HANDLER);
    }
}
