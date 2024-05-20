package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.block.CableBlockEntity;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record UpdateInterfacePacket(BlockPos pos, Direction side, InterfaceType type, String name, String group) implements CustomPayload {
    private static final Id<UpdateInterfacePacket> ID = new Id<>(AdvancedNetworking.id("update_interface"));
    private static final PacketCodec<PacketByteBuf, UpdateInterfacePacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, UpdateInterfacePacket::pos,
            Direction.PACKET_CODEC, UpdateInterfacePacket::side,
            InterfaceType.PACKET_CODEC, UpdateInterfacePacket::type,
            PacketCodecs.STRING, UpdateInterfacePacket::name,
            PacketCodecs.STRING, UpdateInterfacePacket::group,
            UpdateInterfacePacket::new
    );

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> {
            var player = context.player();
            if (player.squaredDistanceTo(packet.pos.toCenterPos()) > 64.0)
                return; // Player too far away
            if (!player.getWorld().getBlockState(packet.pos).isOf(ModBlocks.CABLE))
                return; // Block changed
            if (!(player.getWorld().getBlockEntity(packet.pos) instanceof CableBlockEntity cable))
                return;

            cable.setName(packet.side, packet.name.trim());
            cable.setGroup(packet.side, packet.group.trim());
            CableBlock.changeMode(player.getWorld(), player.getWorld().getBlockState(packet.pos), packet.pos, packet.side, packet.type);
        });
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}