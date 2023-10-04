package io.github.mattidragon.advancednetworking.client.mixin;

import net.minecraft.client.gui.widget.CheckboxWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CheckboxWidget.class)
public interface CheckboxWidgetAccess {
    @Accessor
    void setChecked(boolean value);
}
