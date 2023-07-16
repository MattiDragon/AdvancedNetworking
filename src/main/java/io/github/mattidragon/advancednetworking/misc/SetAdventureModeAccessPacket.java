package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.block.AdventureModeAccessBlockEntity;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static io.github.mattidragon.advancednetworking.AdvancedNetworking.id;

public record SetAdventureModeAccessPacket(BlockPos pos, boolean allowAccess) implements FabricPacket {
    private static final Identifier ID = id("set_adventure_mode_access");
    private static final PacketType<SetAdventureModeAccessPacket> TYPE = PacketType.create(ID, SetAdventureModeAccessPacket::new);

    public SetAdventureModeAccessPacket(PacketByteBuf buf) {
        this(buf.readBlockPos(), buf.readBoolean());
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(TYPE, (packet, player, responseSender) -> {
            if (!player.isCreativeLevelTwoOp()) return;
            var world = player.getWorld();
            if (world.getBlockEntity(packet.pos) instanceof AdventureModeAccessBlockEntity blockEntity) {
                blockEntity.setAdventureModeAccessAllowed(packet.allowAccess);
            }
        });
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(allowAccess);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
