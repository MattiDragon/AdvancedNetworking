package io.github.mattidragon.advancednetworking.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.function.Function;

public interface ClientScreenOpener {
    default void advancednetworking$openCableConfigScreen(BlockPos pos, Direction direction, Function<Direction, InterfaceType> typeSupplier, Function<Direction, String> nameSupplier, boolean allowAdventureModeAccess) {
    }
}
