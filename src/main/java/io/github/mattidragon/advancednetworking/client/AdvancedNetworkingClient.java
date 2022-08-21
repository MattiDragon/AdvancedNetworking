package io.github.mattidragon.advancednetworking.client;

import io.github.mattidragon.advancednetworking.networking.node.MathNode;
import io.github.mattidragon.advancednetworking.registry.ModScreens;
import io.github.mattidragon.advancednetworking.ui.screen.NetworkingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class AdvancedNetworkingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.NETWORKING_HANDLER, NetworkingScreen::new);

        var key = KeyBindingHelper.registerKeyBinding(new KeyBinding("testing", GLFW.GLFW_KEY_K, "testing"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

        });
    }
}
