package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControllerScreenHandler extends EditorScreenHandler {
    private final List<Text> errors;
    private final ScreenHandlerContext context;
    public double viewX;
    public double viewY;
    public int zoom;

    public ControllerScreenHandler(int syncId, ControllerBlockEntity provider, ScreenHandlerContext context) {
        super(syncId, provider, context);
        this.context = context;
        errors = new ArrayList<>();
        viewX = provider.viewX;
        viewY = provider.viewY;
        zoom = provider.zoom;
    }

    public ControllerScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        super(syncId, inv, buf);
        context = ScreenHandlerContext.EMPTY;
        zoom = buf.readInt();
        viewX = buf.readDouble();
        viewY = buf.readDouble();
        errors = buf.readList(PacketByteBuf::readText);
    }

    public List<Text> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return AdvancedNetworking.CONTROLLER_SCREEN;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        context.run((world, pos) -> {
            var blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ControllerBlockEntity controller) {
                controller.viewX = viewX;
                controller.viewY = viewY;
                controller.zoom = zoom;
                controller.markDirty();
            }
        });
    }
}
