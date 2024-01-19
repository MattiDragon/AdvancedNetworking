package io.github.mattidragon.advancednetworking.graph.node.base;

import com.kneelawk.graphlib.api.util.SidedPos;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TransferNodeUtils {
    private TransferNodeUtils() {}

    @NotNull
    public static <V> CombinedStorage<V, Storage<V>> buildCombinedStorage(List<SidedPos> positions, ServerWorld world, BlockApiLookup<Storage<V>, @Nullable Direction> lookup) {
        return new CombinedStorage<>(positions.stream()
                .map(sidePos -> {
                    var pos = sidePos.pos();
                    var side = sidePos.side();
                    return lookup.find(world, pos.offset(side), side.getOpposite());
                })
                .filter(Objects::nonNull)
                .toList());
    }
}
