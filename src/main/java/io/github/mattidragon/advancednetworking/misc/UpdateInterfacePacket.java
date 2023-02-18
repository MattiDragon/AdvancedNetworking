package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public class UpdateInterfacePacket {
    private static final Identifier ID = id("update_interface");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, ((server, player, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();
            var side = buf.readEnumConstant(Direction.class);
            var type = buf.readEnumConstant(InterfaceType.class);
            var name = buf.readString();

            server.execute(() -> {
                if (player.squaredDistanceTo(pos.toCenterPos()) > 64.0)
                    return; // Player too far away
                if (!player.world.getBlockState(pos).isOf(ModBlocks.CABLE))
                    return; // Block changed
                if (!(player.world.getBlockEntity(pos) instanceof CableBlockEntity cable))
                    return;

                cable.setName(side, name.trim());
                CableBlock.changeMode(player.world, player.world.getBlockState(pos), pos, side, type);
            });
        }));
    }

    public static void send(BlockPos pos, Direction side, InterfaceType type, String name) {
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeEnumConstant(side);
        buf.writeEnumConstant(type);
        buf.writeString(name);

        ClientPlayNetworking.send(ID, buf);
    }
}