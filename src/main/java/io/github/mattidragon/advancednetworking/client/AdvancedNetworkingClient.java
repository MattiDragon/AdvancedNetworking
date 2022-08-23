package io.github.mattidragon.advancednetworking.client;

import io.github.mattidragon.advancednetworking.registry.ModScreens;
import io.github.mattidragon.advancednetworking.ui.screen.NetworkingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class AdvancedNetworkingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.NETWORKING_HANDLER, NetworkingScreen::new);
    }
}
