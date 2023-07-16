package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public record UpdateInterfacePacket(BlockPos pos, Direction side, InterfaceType type, String name) implements FabricPacket {
    private static final Identifier ID = id("update_interface");
    private static final PacketType<UpdateInterfacePacket> TYPE = PacketType.create(ID, UpdateInterfacePacket::new);

    public UpdateInterfacePacket(PacketByteBuf buf) {
        this(buf.readBlockPos(), buf.readEnumConstant(Direction.class), buf.readEnumConstant(InterfaceType.class), buf.readString());
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (packet, player, responseSender) -> {
            if (player.squaredDistanceTo(packet.pos.toCenterPos()) > 64.0)
                return; // Player too far away
            if (!player.getWorld().getBlockState(packet.pos).isOf(ModBlocks.CABLE))
                return; // Block changed
            if (!(player.getWorld().getBlockEntity(packet.pos) instanceof CableBlockEntity cable))
                return;

            cable.setName(packet.side, packet.name.trim());
            CableBlock.changeMode(player.getWorld(), player.getWorld().getBlockState(packet.pos), packet.pos, packet.side, packet.type);
        });
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnumConstant(side);
        buf.writeEnumConstant(type);
        buf.writeString(name);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}