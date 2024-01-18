package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface ClientScreenOpener {
    default void advancednetworking$openCableConfigScreen(BlockPos pos, Direction direction, CableBlockEntity cable) {
    }
}
