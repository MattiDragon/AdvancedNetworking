package io.github.mattidragon.advancednetworking.screen;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.nodeflow.misc.GraphProvider;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreenHandler;
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

    public ControllerScreenHandler(int syncId, GraphProvider provider, ScreenHandlerContext context) {
        super(syncId, provider, context);
        errors = new ArrayList<>();
    }

    public ControllerScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        super(syncId, inv, buf);
        var count = buf.readByte();
        errors = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            errors.add(buf.readText());
    }

    public List<Text> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return AdvancedNetworking.CONTROLLER_SCREEN;
    }
}
