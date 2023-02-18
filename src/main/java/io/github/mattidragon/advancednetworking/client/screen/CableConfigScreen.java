package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.misc.InterfaceType;
import io.github.mattidragon.advancednetworking.misc.UpdateInterfacePacket;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
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

    private CyclingButtonWidget<InterfaceType> interfaceTypeButton;
    private TextFieldWidget nameField;

    public CableConfigScreen(BlockPos pos, Direction side, Function<Direction, InterfaceType> typeSupplier, Function<Direction, String> nameSupplier) {
        super(Text.translatable("screen.advanced_networking.cable_config"));
        this.pos = pos;
        this.typeSupplier = typeSupplier;
        this.nameSupplier = nameSupplier;
        this.side = side;
        this.type = typeSupplier.apply(side);
        this.name = nameSupplier.apply(side);
    }

    @Override
    protected void init() {
        addDrawableChild(CyclingButtonWidget.<Direction>builder(direction -> Text.translatable("side.advanced_networking." + direction.asString()))
                .values(Direction.values())
                .initially(side)
                .build(calcLeftX(), 40, 150, 20, Text.translatable("screen.advanced_networking.cable_config.side"), (button, value) -> {
                    UpdateInterfacePacket.send(pos, side, type, name);
                    side = value;
                    type = typeSupplier.apply(side);
                    name = nameSupplier.apply(side);
                    interfaceTypeButton.setValue(type);
                    nameField.setText(name);
                }));

        interfaceTypeButton = addDrawableChild(CyclingButtonWidget.<InterfaceType>builder(type -> Text.translatable("screen.advanced_networking.cable_config.interface_type." + type.id))
                .values(InterfaceType.values())
                .initially(type)
                .build(calcRightX() - 150, 40, 150, 20, Text.translatable("screen.advanced_networking.cable_config.interface_type"), (button, value) -> type = value));

        nameField = addDrawableChild(new TextFieldWidget(textRenderer, calcRightX() - 100, 70, 100, 20, Text.empty()));
        nameField.setText(name);
        nameField.setChangedListener(value -> name = value.trim());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
        UpdateInterfacePacket.send(pos, side, type, name);
    }

    private int calcLeftX() {
        return Math.max(width / 2 - 200, 10);
    }

    private int calcRightX() {
        return Math.min(width / 2 + 200, width - 10);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        // Title
        textRenderer.draw(matrices, title, (width - textRenderer.getWidth(title.asOrderedText())) / 2f, 10, 0xffffff);

        // Left info rows
        textRenderer.draw(matrices, Text.translatable("screen.advanced_networking.cable_config.pos", pos.getX(), pos.getY(), pos.getZ()), calcLeftX(), 70, 0xffffff);
        textRenderer.draw(matrices, Text.translatable("screen.advanced_networking.cable_config.id", CableBlock.calcInterfaceId(pos, side)), calcLeftX(), 80, 0xffffff);

        // Name field tag
        var nameText = Text.translatable("screen.advanced_networking.cable_config.name");
        textRenderer.draw(matrices, nameText, calcRightX() - 110 - textRenderer.getWidth(nameText), 75, 0xffffff);
    }
}
