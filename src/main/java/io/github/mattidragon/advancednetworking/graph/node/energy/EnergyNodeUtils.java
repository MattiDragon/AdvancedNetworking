package io.github.mattidragon.advancednetworking.graph.node.energy;

import com.kneelawk.graphlib.api.util.SidedPos;
import io.github.mattidragon.advancednetworking.misc.CombinedEnergyStorage;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;
import java.util.Objects;

public class EnergyNodeUtils {
    private EnergyNodeUtils() {
    }

    @NotNull
    public static CombinedEnergyStorage buildCombinedStorage(List<SidedPos> positions, ServerWorld world) {
        return new CombinedEnergyStorage(positions.stream()
                .map(sidePos -> {
                    var pos = sidePos.pos();
                    var side = sidePos.side();
                    return EnergyStorage.SIDED.find(world, pos.offset(side), side.getOpposite());
                })
                .filter(Objects::nonNull)
                .toList());
    }
}
