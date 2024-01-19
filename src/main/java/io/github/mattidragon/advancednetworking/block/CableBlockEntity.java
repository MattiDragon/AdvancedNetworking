package io.github.mattidragon.advancednetworking.block;

import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
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
        this.power[direction.getId()] = power;
        world.updateNeighborsAlways(pos, ModBlocks.CABLE);
        world.updateNeighborsAlways(pos.offset(direction), ModBlocks.CABLE);
        markDirty();
    }

    public int getPower(Direction direction) {
        return this.power[direction.getId()];
    }

    public void setGroup(Direction direction, String group) {
        this.groups[direction.getId()] = group;
        markDirty();
        if (world instanceof ServerWorld serverWorld)
            serverWorld.getChunkManager().markForUpdate(pos);
    }

    public String getGroup(Direction direction) {
        return groups[direction.getId()];
    }

    public void setName(Direction direction, String name) {
        this.names[direction.getId()] = name;
        markDirty();
        if (world instanceof ServerWorld serverWorld)
            serverWorld.getChunkManager().markForUpdate(pos);
    }

    public String getName(Direction direction) {
        return names[direction.getId()];
    }

    public Text getDisplayName(Direction direction) {
        var customName = this.names[direction.getId()];
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
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        var power = nbt.getIntArray("power");
        System.arraycopy(power, 0, this.power, 0, Math.min(power.length, 6));
        allowAdventureModeAccess = nbt.getBoolean("allowAdventureModeAccess");

        var names = nbt.getList("names", NbtElement.STRING_TYPE);
        for (int i = 0; i < Math.min(names.size(), 6); i++) {
            this.names[i] = names.get(i).asString();
        }

        var groups = nbt.getList("groups", NbtElement.STRING_TYPE);
        for (int i = 0; i < Math.min(groups.size(), 6); i++) {
            this.groups[i] = groups.get(i).asString();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putIntArray("power", power);
        nbt.putBoolean("allowAdventureModeAccess", allowAdventureModeAccess);

        var names = new NbtList();
        for (var name : this.names) {
            names.add(NbtString.of(name));
        }
        nbt.put("names", names);

        var groups = new NbtList();
        for (var group : this.groups) {
            groups.add(NbtString.of(group));
        }
        nbt.put("groups", groups);
    }

    public boolean isAdventureModeAccessAllowed() {
        return allowAdventureModeAccess;
    }

    public void setAdventureModeAccessAllowed(boolean allowed) {
        this.allowAdventureModeAccess = allowed;
    }
}
