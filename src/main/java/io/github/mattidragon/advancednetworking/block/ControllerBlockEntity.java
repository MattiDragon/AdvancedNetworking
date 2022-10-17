package io.github.mattidragon.advancednetworking.block;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.advancednetworking.utils.NbtUtils;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.misc.EvaluationError;
import io.github.mattidragon.nodeflow.misc.GraphProvidingBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ControllerBlockEntity extends GraphProvidingBlockEntity {
    private Graph graph = new Graph(AdvancedNetworking.ENVIRONMENT);
    private List<Text> errors = List.of();
    public byte ticks = 0;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        graph.readNbt(nbt.getCompound("graph"));
        ticks = nbt.getByte("ticks");
        errors = NbtUtils.readStrings(nbt, "errors").stream()
                .map((Function<String, Optional<Text>>) json -> Optional.ofNullable(Text.Serializer.fromJson(json)))
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        var graphNbt = new NbtCompound();
        graph.writeNbt(graphNbt);
        nbt.put("graph", graphNbt);
        nbt.putByte("ticks", ticks);
        NbtUtils.writeStrings(nbt, "errors", errors.stream().map(Text.Serializer::toJson).toList());
    }

    public static void tick(World world, BlockPos pos, BlockState state, ControllerBlockEntity controller) {
        if (world.isClient) return;
        if (controller.ticks-- <= 0) {
            if (state.get(ControllerBlock.POWERED)) {
                var result = controller.evaluate();
                if (result.isEmpty() != state.get(ControllerBlock.SUCCESS))
                    world.setBlockState(pos, state.cycle(ControllerBlock.SUCCESS), Block.NOTIFY_ALL);

                controller.errors = result.stream().map(EvaluationError::getName).toList();
                controller.ticks = 10;
            }
        }
    }

    public List<EvaluationError> evaluate() {
        var graphs = GraphLib.getController((ServerWorld) world).getGraphsAt(pos).toArray();
        if (graphs.length == 0) {
            AdvancedNetworking.LOGGER.warn("Controller missing graph at {}", pos);
            return List.of(EvaluationError.Type.MISSING_CONTEXTS.error("Controller missing"));
        }
        if (graphs.length > 1) {
            AdvancedNetworking.LOGGER.warn("Controller at {} has multiple graphs", pos);
            return List.of(EvaluationError.Type.MISSING_CONTEXTS.error("Multiple graphs"));
        }

        return graph.evaluate(Context.builder()
                        .put(ContextType.SERVER_WORLD, (ServerWorld) world)
                        .put(ContextType.SERVER, ((ServerWorld) world).getServer())
                        .put(ContextType.BLOCK_POS, pos)
                        .put(NetworkControllerContext.TYPE, new NetworkControllerContext(this, graphs[0]))
                        .build());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        super.writeScreenOpeningData(player, buf);
        buf.writeByte(errors.size());
        errors.forEach(buf::writeText);
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ControllerScreenHandler(syncId, this, ScreenHandlerContext.create(world, pos));
    }

    @Override
    public void setGraph(Graph graph, World world, BlockPos pos) {
        this.graph = graph;
    }

    @Override
    public Graph getGraph(World world, BlockPos pos) {
        return graph;
    }
}
