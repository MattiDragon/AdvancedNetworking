package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.nodeflow.graph.node.Node;
import io.github.mattidragon.nodeflow.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.ui.screen.NodeConfigScreen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class SliderConfigScreen extends NodeConfigScreen {
    private final IntConsumer setter;
    private final IntSupplier getter;
    private final Text baseText;
    private final int min;
    private final int max;

    public SliderConfigScreen(Node owner, EditorScreen parent, IntConsumer setter, IntSupplier getter, Text baseText, int min, int max) {
        super(owner, parent);
        this.setter = setter;
        this.getter = getter;
        this.baseText = baseText;
        this.min = min;
        this.max = max;
    }

    @Override
    protected void init() {
        super.init();
        var x = ((width - 200) / 2) - 50;
        addDrawableChild(new SliderWidget(x, 70, 100, 20, getMessage(), (getter.getAsInt() - min) / (max - min - 1.0)) {
            @Override
            protected void updateMessage() {
                setMessage(SliderConfigScreen.this.getMessage());
            }

            @Override
            protected void applyValue() {
                setter.accept((int) ((value * (max - min)) + min));
            }
        });
    }

    @NotNull
    private MutableText getMessage() {
        return ScreenTexts.composeGenericOptionText(baseText, Text.literal(String.valueOf(getter.getAsInt())));
    }
}