package io.github.mattidragon.advancednetworking.block;

import com.mojang.serialization.Codec;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CableBlockEntity extends BlockEntity implements AdventureModeAccessBlockEntity {
    private final int[] power = new int[6];
    private final String[] names = new String[6];
    private final String[] groups = new String[6];
    private boolean allowAdventureModeAccess = false;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CABLE_BLOCK_ENTITY, pos, state);
        Arrays.fill(names, "");
        Arrays.fill(groups, "");
    }

    public void setPower(Direction direction, int power) {
        this.power[direction.getIndex()] = power;
        world.updateNeighbors(pos, ModBlocks.CABLE);
        world.updateNeighbors(pos.offset(direction), ModBlocks.CABLE);
        markDirty();
    }

    public int getPower(Direction direction) {
        return this.power[direction.getIndex()];
    }

    public void setGroup(Direction direction, String group) {
        this.groups[direction.getIndex()] = group;
        markDirty();
        if (world instanceof ServerWorld serverWorld)
            serverWorld.getChunkManager().markForUpdate(pos);
    }

    public String getGroup(Direction direction) {
        return groups[direction.getIndex()];
    }

    public void setName(Direction direction, String name) {
        this.names[direction.getIndex()] = name;
        markDirty();
        if (world instanceof ServerWorld serverWorld)
            serverWorld.getChunkManager().markForUpdate(pos);
    }

    public String getName(Direction direction) {
        return names[direction.getIndex()];
    }

    public Text getDisplayName(Direction direction) {
        var customName = this.names[direction.getIndex()];
        if (!customName.isBlank()) return Text.literal(customName);
        return getBackupName(direction);
    }

    public Text getBackupName(Direction direction) {
        if (world != null) {
            if (world.getBlockEntity(pos.offset(direction)) instanceof Nameable nameable) {
                return nameable.getDisplayName();
            }
        }
        return Text.literal(CableBlock.calcInterfaceId(pos, direction));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    protected void readData(ReadView view) {
        var power = view.getOptionalIntArray("power").orElseGet(() -> new int[0]);
        System.arraycopy(power, 0, this.power, 0, Math.min(power.length, 6));
        allowAdventureModeAccess = view.getBoolean("allowAdventureModeAccess", false);

        var names = view.getTypedListView("names", Codec.STRING).stream().toList();
        for (int i = 0; i < Math.min(names.size(), 6); i++) {
            this.names[i] = names.get(i);
        }

        var groups = view.getTypedListView("groups", Codec.STRING).stream().toList();
        for (int i = 0; i < Math.min(groups.size(), 6); i++) {
            this.groups[i] = groups.get(i);
        }
    }

    @Override
    protected void writeData(WriteView view) {
        view.putIntArray("power", power);
        view.putBoolean("allowAdventureModeAccess", allowAdventureModeAccess);

        var names = view.getListAppender("names", Codec.STRING);
        for (var name : this.names) {
            names.add(name);
        }

        var groups = view.getListAppender("groups", Codec.STRING);
        for (var group : this.groups) {
            groups.add(group);
        }
    }

    public boolean isAdventureModeAccessAllowed() {
        return allowAdventureModeAccess;
    }

    public void setAdventureModeAccessAllowed(boolean allowed) {
        this.allowAdventureModeAccess = allowed;
        markDirty();
    }
}
