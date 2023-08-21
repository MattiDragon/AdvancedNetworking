package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.misc.ScreenPosSyncPacket;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.nodeflow.ui.screen.HandledEditorScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.List;

public class ControllerScreen extends HandledEditorScreen {
    private final List<Text> errors;

    public ControllerScreen(ControllerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        errors = handler.getErrors();
    }

    @Override
    protected void init() {
        super.init();
        var handler = (ControllerScreenHandler) getScreenHandler();
        area.setViewX(handler.viewX);
        area.setViewY(handler.viewY);
        area.setZoom(handler.zoom);

        if (client != null && client.player != null && client.player.isCreativeLevelTwoOp() && AdvancedNetworking.CONFIG.get().showAdventureModeToggles()) {
            addDrawableChild(CyclingButtonWidget.onOffBuilder()
                    .initially(handler.adventureModeAccessAllowed)
                    .build(GRID_OFFSET, GRID_OFFSET + getBoxHeight() + BORDER_SIZE, 150, 20, Text.translatable("screen.advanced_networking.adventure_mode_access"), (button, value) -> {
                        if (client.interactionManager == null) return;
                        handler.adventureModeAccessAllowed = !handler.adventureModeAccessAllowed;
                        client.interactionManager.clickButton(handler.syncId, 0);
                    }));
        }
    }

    @Override
    public void syncGraph() {
        super.syncGraph();
        ClientPlayNetworking.send(new ScreenPosSyncPacket(getScreenHandler().syncId, area.getViewX(), area.getViewY(), area.getZoom()));
    }

    @Override
    public void close() {
        syncGraph(); // Syncs pos and zoom
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        var isGray = new MutableBoolean(false);

        var texts = errors.stream()
                .map(text -> {
                    var formatting = isGray.booleanValue() ? Formatting.GRAY : Formatting.WHITE;
                    isGray.setValue(!isGray.booleanValue());

                    return text.copy().formatted(formatting);
                }).toList();
        for (int i = 0; i < texts.size() - 1; i++) {
            texts.get(i).append("\n");
        }

        var x = GRID_OFFSET + 220;
        var y = GRID_OFFSET - 20;

        if (!errors.isEmpty()) {
            var text = Text.translatable("advanced_networking.editor.errors");
            context.drawText(textRenderer, text, x, y, 0xff5555, false);
            if (mouseX > x && mouseX < x + textRenderer.getWidth(text) && mouseY > y && mouseY < y + 9)
                context.drawHoverEvent(textRenderer, Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, texts.stream().collect(Text::empty, MutableText::append, MutableText::append))), mouseX, mouseY);
        } else {
            context.drawText(textRenderer, Text.translatable("advanced_networking.editor.no_errors"), x, y, 0x55ff55, false);
        }
    }
}
