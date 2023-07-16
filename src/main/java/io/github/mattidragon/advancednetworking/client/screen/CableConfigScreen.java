package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.misc.InterfaceType;
import io.github.mattidragon.advancednetworking.misc.SetAdventureModeAccessPacket;
import io.github.mattidragon.advancednetworking.misc.UpdateInterfacePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.Function;

public class CableConfigScreen extends Screen {
    private final BlockPos pos;
    private final Function<Direction, InterfaceType> typeSupplier;
    private final Function<Direction, String> nameSupplier;
    private Direction side;
    private InterfaceType type;
    private String name;
    private boolean adventureModeAccessAllowed;

    public CableConfigScreen(BlockPos pos, Direction side, Function<Direction, InterfaceType> typeSupplier, Function<Direction, String> nameSupplier, boolean adventureModeAccessAllowed) {
        super(Text.translatable("screen.advanced_networking.cable_config"));
        this.pos = pos;
        this.typeSupplier = typeSupplier;
        this.nameSupplier = nameSupplier;
        this.side = side;
        this.adventureModeAccessAllowed = adventureModeAccessAllowed;
        this.type = typeSupplier.apply(side);
        this.name = nameSupplier.apply(side);
    }

    @Override
    protected void init() {
        var interfaceTypeButton = addDrawableChild(CyclingButtonWidget.<InterfaceType>builder(type -> Text.translatable("screen.advanced_networking.cable_config.interface_type." + type.id))
                .values(InterfaceType.values())
                .initially(type)
                .build(calcRightX() - 150, 40, 150, 20, Text.translatable("screen.advanced_networking.cable_config.interface_type"), (button, value) -> type = value));

        var nameField = addDrawableChild(new TextFieldWidget(textRenderer, calcRightX() - 100, 70, 100, 20, Text.empty()));
        nameField.setText(name);
        nameField.setChangedListener(value -> name = value.trim());

        var buttons = new ButtonWidget[6];
        for (int i = 0; i < 6; i++) {
            var direction = Direction.byId(i);
            var button = ButtonWidget.builder(Text.translatable("side.advanced_networking." + direction.asString()), button1 -> {
                button1.active = false;
                buttons[side.getId()].active = true;

                ClientPlayNetworking.send(new UpdateInterfacePacket(pos, side, type, name));
                side = direction;
                type = typeSupplier.apply(side);
                name = nameSupplier.apply(side);
                interfaceTypeButton.setValue(type);
                nameField.setText(name);
            }).width(100).position(calcLeftX(), 40 + 20 * i).build();
            addDrawableChild(button);
            buttons[i] = button;
        }
        buttons[side.getId()].active = false;

        if (client != null && client.player != null && client.player.isCreativeLevelTwoOp()) {
            addDrawableChild(CyclingButtonWidget.onOffBuilder()
                    .initially(adventureModeAccessAllowed)
                    .build(calcLeftX(), 170, 150, 20, Text.translatable("screen.advanced_networking.adventure_mode_access"), (button, value) -> {
                        adventureModeAccessAllowed = value;
                        ClientPlayNetworking.send(new SetAdventureModeAccessPacket(pos, adventureModeAccessAllowed));
                    }));
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
        ClientPlayNetworking.send(new UpdateInterfacePacket(pos, side, type, name));
    }

    private int calcLeftX() {
        return Math.max(width / 2 - 200, 10);
    }

    private int calcRightX() {
        return Math.min(width / 2 + 200, width - 10);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawText(textRenderer, title, (width - textRenderer.getWidth(title.asOrderedText())) / 2, 10, 0xffffff, false);

        // Info rows
        context.drawText(textRenderer, Text.translatable("screen.advanced_networking.cable_config.pos", pos.getX(), pos.getY(), pos.getZ()), calcRightX() - 150, 100, 0xffffff, false);
        context.drawText(textRenderer, Text.translatable("screen.advanced_networking.cable_config.id", CableBlock.calcInterfaceId(pos, side)), calcRightX() - 150, 110, 0xffffff, false);

        // Name field tag
        var nameText = Text.translatable("screen.advanced_networking.cable_config.name");
        context.drawText(textRenderer, nameText, calcRightX() - 110 - textRenderer.getWidth(nameText), 75, 0xffffff, false);
    }
}
