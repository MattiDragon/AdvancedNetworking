package io.github.mattidragon.advancednetworking.block;

import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CableBlockEntity extends BlockEntity {
    private final int[] power = new int[6];

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CABLE_BLOCK_ENTITY, pos, state);
    }

    public void setPower(Direction direction, int power) {
        this.power[direction.getId()] = power;
        world.updateNeighborsAlways(pos, ModBlocks.CABLE);
        world.updateNeighborsAlways(pos.offset(direction), ModBlocks.CABLE);
        markDirty();
    }

    public int getPower(Direction direction) {
        return this.power[direction.getId()];
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        var power = nbt.getIntArray("power");
        System.arraycopy(power, 0, this.power, 0, Math.min(power.length, 6));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putIntArray("power", power);
    }
}
