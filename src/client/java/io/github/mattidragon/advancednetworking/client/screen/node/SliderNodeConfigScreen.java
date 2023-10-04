package io.github.mattidragon.advancednetworking.client.screen.node;

import io.github.mattidragon.advancednetworking.graph.node.base.SingleSliderNode;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SliderNodeConfigScreen<T extends SingleSliderNode> extends NodeConfigScreen<T> {
    public SliderNodeConfigScreen(T owner, EditorScreen parent) {
        super(owner, parent);
    }

    @Override
    protected void init() {
        super.init();
        var x = ((width - 200) / 2) - 50;
        addDrawableChild(new SliderWidget(x, 70, 100, 20, getMessage(), (owner.getValue() - owner.getMin()) / (owner.getMax() - owner.getMin() - 1.0)) {
            @Override
            protected void updateMessage() {
                setMessage(SliderNodeConfigScreen.this.getMessage());
            }

            @Override
            protected void applyValue() {
                owner.setValue((int) (value * (owner.getMax() - owner.getMin()) + owner.getMin()));
            }
        });
    }

    @NotNull
    private MutableText getMessage() {
        return ScreenTexts.composeGenericOptionText(this.owner.getSliderText(), Text.literal(String.valueOf(owner.getValue())));
    }
}