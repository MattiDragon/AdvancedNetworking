package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.misc.ResourceFilter;
import io.github.mattidragon.nodeflow.client.ui.screen.EditorScreen;
import io.github.mattidragon.nodeflow.client.ui.screen.NodeConfigScreen;
import io.github.mattidragon.nodeflow.graph.node.Node;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ResourceFilterConfigScreen<N extends Node> extends NodeConfigScreen<N> {
    private final ResourceFilter<?, ?> filter;

    public ResourceFilterConfigScreen(N owner, EditorScreen parent, ResourceFilter<?, ?> filter) {
        super(owner, parent);
        this.filter = filter;
    }

    @Override
    protected void init() {
        var x = ((width - 200) / 2) - 50;

        var regexButton = CyclingButtonWidget.onOffBuilder()
                .initially(filter.shouldUseRegex())
                .build(x, 45, 100, 20, Text.translatable("node.advanced_networking.filter.use_regex"), (button1, value) -> filter.setUseRegex(value));
        if (AdvancedNetworking.CONFIG.get().disableRegexFilter()) {
            regexButton.active = false;
            regexButton.setTooltip(Tooltip.of(Text.translatable("node.advanced_networking.filter.use_regex.disabled")));
        }
        addDrawableChild(regexButton);

        var whitelistButton = CyclingButtonWidget.onOffBuilder(Text.translatable("node.advanced_networking.filter.mode.whitelist"), Text.translatable("node.advanced_networking.filter.mode.blacklist"))
                .initially(filter.isWhitelist())
                .omitKeyText()
                .build(x, 70, 100, 20, Text.empty(), (button1, value) -> filter.setWhitelist(value));
        addDrawableChild(whitelistButton);

        var button = CyclingButtonWidget.<ResourceFilter.Mode>builder(mode -> mode == ResourceFilter.Mode.RESOURCE ? Text.translatable("node.advanced_networking.filter.mode.resource") : Text.translatable("node.advanced_networking.filter.mode.tag"))
                .values(ResourceFilter.Mode.values())
                .initially(filter.getMode())
                .build(x, 95, 100, 20, Text.translatable("node.advanced_networking.filter.mode"), (button1, value) -> filter.setMode(value));
        addDrawableChild(button);

        var idField = new TextFieldWidget(textRenderer, x, 120, 100, 20, Text.empty());
        idField.setMaxLength(100);
        idField.setPlaceholder(Text.literal("id").formatted(Formatting.GRAY));
        idField.setText(filter.getIdFilter());
        idField.setChangedListener(filter::setIdFilter);
        addDrawableChild(idField);

        var nbtField = new TextFieldWidget(textRenderer, x, 145, 100, 20, Text.empty());
        nbtField.setMaxLength(200);
        nbtField.setPlaceholder(Text.literal("nbt").formatted(Formatting.GRAY));
        nbtField.setText(filter.getNbtFilter());
        nbtField.setChangedListener(filter::setNbtFilter);
        addDrawableChild(nbtField);
    }
}
