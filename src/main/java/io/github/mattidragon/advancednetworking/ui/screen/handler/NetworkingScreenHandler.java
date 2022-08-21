package io.github.mattidragon.advancednetworking.ui.screen.handler;

import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import io.github.mattidragon.advancednetworking.networking.Graph;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.advancednetworking.registry.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

import java.util.Objects;

public class NetworkingScreenHandler extends ScreenHandler {
    public final Graph graph;
    public final ScreenHandlerContext context;

    public NetworkingScreenHandler(int syncId, PlayerInventory inv, ControllerBlockEntity entity, ScreenHandlerContext context) {
        super(ModScreens.NETWORKING_HANDLER, syncId);
        this.graph = entity.graph.copy();
        this.context = context;
    }

    public NetworkingScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf packetByteBuf) {
        super(ModScreens.NETWORKING_HANDLER, syncId);
        this.graph = new Graph();
        graph.readNbt(Objects.requireNonNull(packetByteBuf.readNbt()));
        context = ScreenHandlerContext.EMPTY;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, ModBlocks.CONTROLLER);
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        context.run((world, pos) -> {
            if (world.getBlockEntity(pos) instanceof ControllerBlockEntity controllerBlockEntity) {
                controllerBlockEntity.graph = graph.copy();
                controllerBlockEntity.markDirty();
            }
        });
    }
}
