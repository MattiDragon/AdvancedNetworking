package io.github.mattidragon.advancednetworking.screen;

import io.github.mattidragon.nodeflow.ui.screen.HandledEditorScreen;
import net.minecraft.client.util.math.MatrixStack;
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

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
                //.flatMap(text -> textRenderer.wrapLines(text, 130).stream())
                //.collect(Text::empty, (builder, text) -> builder.append("\n").append(text), MutableText::append);

        var x = GRID_OFFSET + 220;
        var y = GRID_OFFSET - 20;

        if (!errors.isEmpty()) {
            var text = Text.translatable("advanced_networking.editor.errors");
            textRenderer.draw(matrices, text, x, y, 0xff5555);
            if (mouseX > x && mouseX < x + textRenderer.getWidth(text) && mouseY > y && mouseY < y + 9)
                renderTextHoverEffect(matrices, Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, texts.stream().collect(Text::empty, MutableText::append, MutableText::append))), mouseX, mouseY);
        } else {
            textRenderer.draw(matrices, Text.translatable("advanced_networking.editor.no_errors"), x, y, 0x55ff55);
        }
    }
}
