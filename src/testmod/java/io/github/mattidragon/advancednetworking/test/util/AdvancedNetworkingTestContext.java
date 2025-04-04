package io.github.mattidragon.advancednetworking.test.util;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AdvancedNetworkingTestContext extends TestContext {
    public AdvancedNetworkingTestContext(GameTestState test) {
        super(test);
    }

    public void expectEmptyPot(BlockPos pos) {
        var blockPos = this.getAbsolutePos(pos);
        var blockEntity = this.getWorld().getBlockEntity(blockPos);
        if (blockEntity instanceof DecoratedPotBlockEntity pot && !pot.isEmpty()) {
            throwPositionedException(Text.literal("Container should be empty"), pos);
        }
    }

    public void expectPotWith(BlockPos pos, Item item, int count) {
        var blockPos = this.getAbsolutePos(pos);
        var blockEntity = this.getWorld().getBlockEntity(blockPos);
        if (!(blockEntity instanceof DecoratedPotBlockEntity pot)) {
            throwPositionedException(Text.literal("Expected a pot at " + pos + ", found " + blockEntity), pos);
        } else if (pot.count(item) != count) {
            throwPositionedException(Text.literal("Container should contain: " + item), pos);
        }
    }

    public ControllerBlockEntity controller(BlockPos pos) {
        setBlockState(pos, ModBlocks.CONTROLLER);
        return getBlockEntity(pos, ControllerBlockEntity.class);
    }

    @SafeVarargs
    public final CableBlockEntity cable(BlockPos pos, EnumProperty<CableBlock.ConnectionType>... interfaces) {
        var cable = ModBlocks.CABLE.getDefaultState();
        for (var side : interfaces) {
            cable = cable.with(side, CableBlock.ConnectionType.INTERFACE);
        }
        setBlockState(pos, cable);
        return getBlockEntity(pos, CableBlockEntity.class);
    }

    public String interfaceId(BlockPos pos, Direction direction) {
        return CableBlock.calcInterfaceId(getAbsolutePos(pos), direction);
    }
}
