package io.github.mattidragon.advancednetworking.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.screen.ControllerScreen;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
import io.github.mattidragon.nodeflow.ui.screen.HandledEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.text.Text;

public class AdvancedNetworkingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.<EditorScreenHandler, HandledEditorScreen>register(AdvancedNetworking.CONTROLLER_SCREEN, (handler, inventory, title) -> new ControllerScreen((ControllerScreenHandler) handler, inventory, title));

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(GraphDebugRenderer.INSTANCE::render);

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("graph_debug")
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("toggle")
                                .executes(context -> {
                                    if (context.getSource().getClient().getServer() == null) {
                                        context.getSource().sendError(Text.literal("Debug rendering only available in singleplayer"));
                                        return 0;
                                    }
                                    GraphDebugRenderer.INSTANCE.active = !GraphDebugRenderer.INSTANCE.active;
                                    context.getSource().sendFeedback(Text.literal("Toggled debug rendering"));
                                    return 1;
                                }))
                        .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("classview")
                                .executes(context -> {
                                    if (context.getSource().getClient().getServer() == null) {
                                        context.getSource().sendError(Text.literal("Debug rendering only available in singleplayer"));
                                        return 0;
                                    }
                                    GraphDebugRenderer.INSTANCE.classview = !GraphDebugRenderer.INSTANCE.classview;
                                    context.getSource().sendFeedback(Text.literal("Toggled class rendering"));
                                    return 1;
                                })))));
    }
}
