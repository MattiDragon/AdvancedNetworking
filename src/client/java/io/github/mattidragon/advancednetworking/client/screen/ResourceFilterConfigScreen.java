package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.misc.ResourceFilter;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class ResourceFilterConfigScreen<N extends Node> extends NodeConfigScreen<N> {
    private final ResourceFilter<?, ?> filter;

    public ResourceFilterConfigScreen(N owner, EditorScreen parent, ResourceFilter<?, ?> filter) {
        super(owner, parent);
        this.filter = filter;
    }

    @Override
    protected void init() {
        var x = ((width - 200) / 2) - 100;

        var regexButton = CyclingButtonWidget.onOffBuilder()
                .initially(filter.shouldUseRegex())
                .build(x + 50, 45, 100, 20, Text.translatable("node.advanced_networking.filter.use_regex"), (button1, value) -> filter.setUseRegex(value));
        if (AdvancedNetworking.CONFIG.get().disableRegexFilter()) {
            regexButton.active = false;
            regexButton.setTooltip(Tooltip.of(Text.translatable("node.advanced_networking.filter.use_regex.disabled")));
        }
        addDrawableChild(regexButton);

        var whitelistButton = CyclingButtonWidget.onOffBuilder(Text.translatable("node.advanced_networking.filter.mode.whitelist"), Text.translatable("node.advanced_networking.filter.mode.blacklist"))
                .initially(filter.isWhitelist())
                .omitKeyText()
                .build(x + 50, 70, 100, 20, Text.empty(), (button1, value) -> filter.setWhitelist(value));
        addDrawableChild(whitelistButton);
        
        var infoText1 = Text.translatable("node.advanced_networking.filter.syntax_info.line1").formatted(Formatting.YELLOW);
        var infoText2 = Text.translatable("node.advanced_networking.filter.syntax_info.line2");
        var textWidth = Math.max(textRenderer.getWidth(infoText1), textRenderer.getWidth(infoText2));
        addDrawableChild(new TextWidget(((width - 200 - textWidth) / 2),
                95,
                textWidth,
                10,
                infoText1,
                textRenderer));
        addDrawableChild(new PressableTextWidget(((width - 200 - textWidth) / 2),
                105,
                textWidth,
                10,
                infoText2,
                button -> Util.getOperatingSystem().open("https://minecraft.wiki/w/Argument_types#item_predicate"),
                textRenderer));

        var idField = new TextFieldWidget(textRenderer, x, 120, 200, 20, Text.empty());
        idField.setMaxLength(500);
        idField.setPlaceholder(Text.literal("filter").formatted(Formatting.GRAY));
        idField.setText(filter.getFilter());
        idField.setChangedListener(filter::setFilter);
        addDrawableChild(idField);
    }
}
