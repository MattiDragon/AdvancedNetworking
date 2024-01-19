package io.github.mattidragon.advancednetworking.client.screen;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CableConfigScreen extends Screen {
    private final BlockPos pos;
    private final CableBlockEntity cable;
    private final World world;
    private Direction side;
    private InterfaceType type;
    private String name;
    private String group;
    private boolean adventureModeAccessAllowed;

    public CableConfigScreen(BlockPos pos, Direction side, CableBlockEntity cable) {
        super(Text.translatable("screen.advanced_networking.cable_config"));
        this.pos = pos;
        this.side = side;
        this.world = cable.getWorld();
        if (world == null) throw new IllegalStateException("Cable block entity has no world");
        this.cable = cable;
        this.type = getType();
        this.name = getName();
        this.group = getGroup();
    }

    private InterfaceType getType() {
        return InterfaceType.ofConnectionType(world.getBlockState(this.pos).getOrEmpty(CableBlock.FACING_PROPERTIES.get(this.side)).orElse(CableBlock.ConnectionType.NONE));
    }

    private String getName() {
        return this.cable.getName(this.side);
    }

    private String getGroup() {
        return this.cable.getGroup(this.side);
    }

    @Override
    protected void init() {
        var interfaceTypeButton = addDrawableChild(CyclingButtonWidget.<InterfaceType>builder(type -> Text.translatable("screen.advanced_networking.cable_config.interface_type." + type.id))
                .values(InterfaceType.values())
                .initially(type)
                .build(calcRightX() - 150, 40, 150, 20, Text.translatable("screen.advanced_networking.cable_config.interface_type"), (button, value) -> type = value));

        var nameField = addDrawableChild(new TextFieldWidget(textRenderer, calcRightX() - 100, 70, 100, 20, Text.empty()));
        nameField.setText(name);
        nameField.setPlaceholder(cable.getBackupName(side).copy().formatted(Formatting.GRAY));
        nameField.setChangedListener(value -> name = value.trim());

        var groupField = addDrawableChild(new TextFieldWidget(textRenderer, calcRightX() - 100, 90, 100, 20, Text.empty()));
        groupField.setText(group);
        groupField.setChangedListener(value -> group = value.trim());

        var buttons = new ButtonWidget[6];
        for (int i = 0; i < 6; i++) {
            var direction = Direction.byId(i);
            var button = ButtonWidget.builder(Text.translatable("side.advanced_networking." + direction.asString()), button1 -> {
                button1.active = false;
                buttons[side.getId()].active = true;

                ClientPlayNetworking.send(new UpdateInterfacePacket(pos, side, type, name, group));
                side = direction;
                type = getType();
                name = getName();
                group = getGroup();
                interfaceTypeButton.setValue(type);
                nameField.setText(name);
                nameField.setPlaceholder(cable.getBackupName(side).copy().formatted(Formatting.GRAY));
                groupField.setText(group);
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
        ClientPlayNetworking.send(new UpdateInterfacePacket(pos, side, type, name, group));
    }

    private int calcLeftX() {
        return Math.max(width / 2 - 200, 10);
    }

    private int calcRightX() {
        return Math.min(width / 2 + 200, width - 10);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawText(textRenderer, title, (width - textRenderer.getWidth(title.asOrderedText())) / 2, 10, 0xffffff, false);

        // Info rows
        context.drawText(textRenderer, Text.translatable("screen.advanced_networking.cable_config.pos", pos.getX(), pos.getY(), pos.getZ()), calcRightX() - 150, 120, 0xffffff, false);
        context.drawText(textRenderer, Text.translatable("screen.advanced_networking.cable_config.id", CableBlock.calcInterfaceId(pos, side)), calcRightX() - 150, 130, 0xffffff, false);

        // Name field tag
        var nameText = Text.translatable("screen.advanced_networking.cable_config.name");
        context.drawText(textRenderer, nameText, calcRightX() - 110 - textRenderer.getWidth(nameText), 75, 0xffffff, false);
        var groupText = Text.translatable("screen.advanced_networking.cable_config.group");
        context.drawText(textRenderer, groupText, calcRightX() - 110 - textRenderer.getWidth(groupText), 95, 0xffffff, false);
    }
}
