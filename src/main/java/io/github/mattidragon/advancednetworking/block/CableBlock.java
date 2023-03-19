package io.github.mattidragon.advancednetworking.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import com.kneelawk.graphlib.GraphLib;
import com.kneelawk.graphlib.graph.BlockNode;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.client.screen.CableConfigScreen;
import io.github.mattidragon.advancednetworking.misc.InterfaceType;
import io.github.mattidragon.advancednetworking.network.UpdateScheduler;
import io.github.mattidragon.advancednetworking.network.node.CableNode;
import io.github.mattidragon.advancednetworking.network.node.InterfaceNode;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.advancednetworking.registry.ModTags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Mojank
@SuppressWarnings("deprecation")
public class CableBlock extends BlockWithEntity {
    public static final EnumProperty<ConnectionType> NORTH = EnumProperty.of("north", ConnectionType.class);
    public static final EnumProperty<ConnectionType> EAST = EnumProperty.of("east", ConnectionType.class);
    public static final EnumProperty<ConnectionType> SOUTH = EnumProperty.of("south", ConnectionType.class);
    public static final EnumProperty<ConnectionType> WEST = EnumProperty.of("west", ConnectionType.class);
    public static final EnumProperty<ConnectionType> UP = EnumProperty.of("up", ConnectionType.class);
    public static final EnumProperty<ConnectionType> DOWN = EnumProperty.of("down", ConnectionType.class);
    public static final Map<Direction, EnumProperty<ConnectionType>> FACING_PROPERTIES = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), directions -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
        directions.put(Direction.UP, UP);
        directions.put(Direction.DOWN, DOWN);
    }));
    protected final VoxelShape[] connectionsToShape;

    public CableBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.connectionsToShape = this.generateShapeMap(2 / 16f);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var state = getDefaultState();
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();

        for (var direction : Direction.values()) {
            if (canConnect(state, world.getBlockState(pos.offset(direction)), direction)) {
                state = state.with(FACING_PROPERTIES.get(direction), ConnectionType.CONNECTED);
            }
        }
        return state;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (canConnect(state, neighborState, direction)) {
            return state.with(FACING_PROPERTIES.get(direction), ConnectionType.CONNECTED);
        } else if (state.get(FACING_PROPERTIES.get(direction)) == ConnectionType.CONNECTED) {
            return state.with(FACING_PROPERTIES.get(direction), ConnectionType.NONE);
        }

        return state;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return (world.getBlockEntity(pos) instanceof CableBlockEntity cable) ? cable.getPower(direction.getOpposite()) : 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getStrongRedstonePower(state, world, pos, direction);
    }

    private static boolean canConnect(BlockState state, BlockState other, Direction direction) {
        if (other.isOf(ModBlocks.CONTROLLER))
            return state.get(FACING_PROPERTIES.get(direction)).canConnect();

        return other.isOf(ModBlocks.CABLE) && state.get(FACING_PROPERTIES.get(direction)).canConnect() && other.get(FACING_PROPERTIES.get(direction.getOpposite())).canConnect();
    }

    /**
     * Stolen from {@link net.minecraft.block.ConnectingBlock ConnectingBlock}
     */
    private VoxelShape[] generateShapeMap(float radius) {
        float min = 0.5F - radius;
        float max = 0.5F + radius;
        VoxelShape voxelShape = Block.createCuboidShape(
                min * 16.0F, min * 16.0F, min * 16.0F, max * 16.0F, max * 16.0F, max * 16.0F
        );
        VoxelShape[] partShapes = new VoxelShape[Direction.values().length];

        for(int i = 0; i < Direction.values().length; ++i) {
            Direction direction = Direction.values()[i];
            partShapes[i] = VoxelShapes.cuboid(
                    0.5 + Math.min(-radius, direction.getOffsetX() * 0.5),
                    0.5 + Math.min(-radius, direction.getOffsetY() * 0.5),
                    0.5 + Math.min(-radius, direction.getOffsetZ() * 0.5),
                    0.5 + Math.max(radius, direction.getOffsetX() * 0.5),
                    0.5 + Math.max(radius, direction.getOffsetY() * 0.5),
                    0.5 + Math.max(radius, direction.getOffsetZ() * 0.5)
            );
        }

        VoxelShape[] combinedShapes = new VoxelShape[64];

        for(int j = 0; j < 64; ++j) {
            VoxelShape combinedShape = voxelShape;

            for(int k = 0; k < Direction.values().length; ++k) {
                if ((j & 1 << k) != 0) {
                    combinedShape = VoxelShapes.union(combinedShape, partShapes[k]);
                }
            }

            combinedShapes[j] = combinedShape;
        }

        return combinedShapes;
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.connectionsToShape[this.getConnectionMask(state)];
    }

    protected int getConnectionMask(BlockState state) {
        int i = 0;

        for(int j = 0; j < Direction.values().length; ++j) {
            if (state.get(FACING_PROPERTIES.get(Direction.values()[j])).hasCollision()) {
                i |= 1 << j;
            }
        }

        return i;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        if (world instanceof World realWorld) {
            UpdateScheduler.UPDATES.put(realWorld.getRegistryKey(), pos);
        }

        // Zero power for non-interface faces to be safe
        for (var direction : Direction.values()) {
            if (!state.get(FACING_PROPERTIES.get(direction)).isInterface() &&
                    world.getBlockEntity(pos) instanceof CableBlockEntity cable)
                cable.setPower(direction, 0);
        }
    }

    @NotNull
    public static String calcInterfaceId(BlockPos pos, Direction dir) {
        long id = pos.asLong();
        id ^= id >>> 33;
        id *= 0xff51afd7ed558ccdL;
        id ^= id >>> 33;
        id *= 0xc4ceb9fe1a85ec53L;
        id ^= id >>> 33;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ArrayUtils.add(Longs.toByteArray(id), (byte) dir.getId()));
    }

    public static void changeMode(World world, BlockState state, BlockPos pos, Direction side, InterfaceType newType) {
        var connectionType = switch (newType) {
            case INTERFACE -> ConnectionType.INTERFACE;
            case BLOCKED -> ConnectionType.DISABLED;
            case DEFAULT -> ConnectionType.NONE;
        };

        var newState = state.with(FACING_PROPERTIES.get(side), connectionType);
        world.setBlockState(pos, newState);

        if (canConnect(newState, world.getBlockState(pos.offset(side)), side)) {
            world.setBlockState(pos, state.with(FACING_PROPERTIES.get(side), ConnectionType.CONNECTED));
        }

        if (world instanceof ServerWorld serverWorld)
            GraphLib.getController(serverWorld).updateConnections(pos);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var direction = calcHitDirection(hit.getPos().subtract(Vec3d.of(pos)));
        var property = FACING_PROPERTIES.get(direction);

        if (player.getStackInHand(hand).isIn(ModTags.Items.WRENCHES)) {
            changeMode(world, state, pos, direction, switch (state.get(property)) {
                case NONE -> InterfaceType.INTERFACE;
                case DISABLED -> InterfaceType.DEFAULT;
                case INTERFACE, INTERFACE_POWERED, CONNECTED -> InterfaceType.BLOCKED;
            });

            return ActionResult.SUCCESS;
        }
        if (hand == Hand.MAIN_HAND && player.isSneaking()) {
            if (world.isClient && world.getBlockEntity(pos) instanceof CableBlockEntity cable) {
                openConfig(pos.toImmutable(), direction, (side) -> InterfaceType.ofConnectionType(world.getBlockState(pos).get(FACING_PROPERTIES.get(side))), cable::getName);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Environment(EnvType.CLIENT)
    private void openConfig(BlockPos pos, Direction direction, Function<Direction, InterfaceType> typeSupplier, Function<Direction, String> nameSupplier) {
        MinecraftClient.getInstance().setScreen(new CableConfigScreen(pos, direction, typeSupplier, nameSupplier));
    }

    private Direction calcHitDirection(Vec3d pos) {
        if (pos.x >= 6 / 16.0 && pos.x <= 10 / 16.0) {
            if (pos.z >= 6 / 16.0 && pos.z <= 10 / 16.0) {
                if (pos.y >= 10 / 16.0) return Direction.UP;
                if (pos.y <= 6 / 16.0) return Direction.DOWN;
            }
            if (pos.y >= 6 / 16.0 && pos.y <= 10 / 16.0) {
                if (pos.z >= 10 / 16.0) return Direction.SOUTH;
                if (pos.z <= 6 / 16.0) return Direction.NORTH;
            }
        }
        if (pos.y >= 6 / 16.0 && pos.y <= 10 / 16.0) {
            if (pos.z >= 6 / 16.0 && pos.z <= 10 / 16.0) {
                if (pos.x >= 10 / 16.0) return Direction.EAST;
                if (pos.x <= 6 / 16.0) return Direction.WEST;
            }
        }
        AdvancedNetworking.LOGGER.warn("Clicked on cable, but not any part");
        return Direction.UP;
    }

    public List<BlockNode> getNodes(BlockState state) {
        var nodes = Direction.stream()
                .filter(dir -> state.get(FACING_PROPERTIES.get(dir)).isInterface())
                .map(InterfaceNode.INSTANCES::get)
                .collect(Collectors.<BlockNode, ArrayList<BlockNode>>toCollection(ArrayList::new));
        nodes.add(CableNode.INSTANCE);
        return nodes;
    }

    public enum ConnectionType implements StringIdentifiable {
        NONE,
        DISABLED,
        CONNECTED,
        INTERFACE,
        INTERFACE_POWERED;

        public boolean hasCollision() {
            return this == CONNECTED || this == INTERFACE || this == INTERFACE_POWERED;
        }

        public boolean canConnect() {
            return this == NONE || this == CONNECTED;
        }

        public boolean isInterface() {
            return this == INTERFACE || this == INTERFACE_POWERED;
        }

        @Override
        public String asString() {
            return name().toLowerCase();
        }
    }
}
