package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.block.AdventureModeAccessBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SetAdventureModeAccessPacket(BlockPos pos, boolean allowAccess) implements CustomPayload {
    private static final Id<SetAdventureModeAccessPacket> ID = new Id<>(AdvancedNetworking.id("set_adventure_mode_access"));
    private static final PacketCodec<PacketByteBuf, SetAdventureModeAccessPacket> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetAdventureModeAccessPacket::pos,
            PacketCodecs.BOOLEAN, SetAdventureModeAccessPacket::allowAccess,
            SetAdventureModeAccessPacket::new
    );

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (packet, context) -> {
            var player = context.player();
            if (!player.isCreativeLevelTwoOp()) return;
            var world = player.getWorld();
            if (world.getBlockEntity(packet.pos) instanceof AdventureModeAccessBlockEntity blockEntity) {
                blockEntity.setAdventureModeAccessAllowed(packet.allowAccess);
            }
        });
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
