package io.github.mattidragon.advancednetworking.test.util;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.block.ControllerBlockEntity;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AdvancedNetworkingTestContext extends TestContext {
    public AdvancedNetworkingTestContext(GameTestState test) {
        super(test);
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos pos, Class<? extends T> type) {
         var entity = getBlockEntity(pos);
         if (entity == null) throwPositionedException("Expected block entity", pos);
         if (!type.isInstance(entity)) throwPositionedException("Expected block entity to be of class " + type.getSimpleName(), pos);
         return type.cast(entity);
    }

    @Override
    public void expectEmptyContainer(BlockPos pos) {
        try {
            super.expectEmptyContainer(pos);
        } catch (GameTestException e) {
            throwPositionedException(e.getMessage(), pos);
        }
    }

    public void expectEmptyPot(BlockPos pos) {
        try {
            var blockPos = this.getAbsolutePos(pos);
            var blockEntity = this.getWorld().getBlockEntity(blockPos);
            if (blockEntity instanceof DecoratedPotBlockEntity pot && !pot.isEmpty()) {
                throw new GameTestException("Container should be empty");
            }
        } catch (GameTestException e) {
            throwPositionedException(e.getMessage(), pos);
        }
    }

    @Override
    public void expectContainerWith(BlockPos pos, Item item) {
        try {
            super.expectContainerWith(pos, item);
        } catch (GameTestException e) {
            throwPositionedException(e.getMessage(), pos);
        }
    }

    public void expectPotWith(BlockPos pos, Item item, int count) {
        try {
            BlockPos blockPos = this.getAbsolutePos(pos);
            BlockEntity blockEntity = this.getWorld().getBlockEntity(blockPos);
            if (!(blockEntity instanceof DecoratedPotBlockEntity pot)) {
                throw new GameTestException("Expected a container at " + pos + ", found " + Registries.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()));
            } else if (pot.count(item) != count) {
                throw new GameTestException("Container should contain: " + item);
            }
        } catch (GameTestException e) {
            throwPositionedException(e.getMessage(), pos);
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
