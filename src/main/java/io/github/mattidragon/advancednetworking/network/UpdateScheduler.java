package io.github.mattidragon.advancednetworking.network;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.kneelawk.graphlib.GraphLib;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateScheduler {
    public static final Multimap<RegistryKey<World>, BlockPos> UPDATES = HashMultimap.create();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var key : UPDATES.keySet()) {
                var world = server.getWorld(key);
                if (world == null)
                    continue;
                GraphLib.getController(world).updateNodes(UPDATES.get(key));
            }
        });
    }
}
