package io.github.mattidragon.advancednetworking.block;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.advancednetworking.config.AdvancedNetworkingConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ControllerBlock extends BlockWithEntity {
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty SUCCESS = BooleanProperty.of("success");

    public ControllerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(POWERED, false).with(SUCCESS, true));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world instanceof ServerWorld serverWorld) {
            boolean active = state.get(POWERED);
            boolean hasRedstone = world.isReceivingRedstonePower(pos);

            if (active && !hasRedstone) { // Extra tick in case ticking stopped
                world.scheduleBlockTick(pos, this, AdvancedNetworkingConfig.CONTROLLER_TICK_RATE.get());
            }

            if (!active && hasRedstone) {
                var newState = state.cycle(POWERED);
                world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);
                scheduledTick(newState, serverWorld, pos, world.random);
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED)) {
            if (world.isReceivingRedstonePower(pos)) {
                if (!(world.getBlockEntity(pos) instanceof ControllerBlockEntity controller))
                    return;
                ControllerBlockEntity.tick(world, pos, state, controller);
                world.scheduleBlockTick(pos, this, AdvancedNetworkingConfig.CONTROLLER_TICK_RATE.get());
            } else {
                world.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, SUCCESS);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        if (world instanceof ServerWorld serverWorld) {
            GraphLib.getController(serverWorld).updateNodes(pos);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return null;
//        return checkType(type, ModBlocks.CONTROLLER_BLOCK_ENTITY, ControllerBlockEntity::tick);
    }
}
