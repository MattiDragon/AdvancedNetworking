package io.github.mattidragon.advancednetworking.block;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.client.screen.ControllerScreenHandler;
import io.github.mattidragon.advancednetworking.config.Config;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.stream.ResourceStream;
import io.github.mattidragon.advancednetworking.graph.stream.ResourceStreamEvaluator;
import io.github.mattidragon.advancednetworking.misc.NbtUtils;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.misc.EvaluationError;
import io.github.mattidragon.nodeflow.misc.GraphProvidingBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ControllerBlockEntity extends GraphProvidingBlockEntity {
    private Graph graph = new Graph(AdvancedNetworking.ENVIRONMENT);
    private final List<ResourceStream.Start<Storage<ItemVariant>>> itemStreams = new ArrayList<>();
    private final List<ResourceStream.Start<Storage<FluidVariant>>> fluidStreams = new ArrayList<>();
    private final List<ResourceStream.Start<EnergyStorage>> energyStreams = new ArrayList<>();
    public double viewX = 0;
    public double viewY = 0;
    public int zoom = 0;
    private List<Text> errors = List.of();
    public byte ticks = 0;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    public void addItemStream(ResourceStream.Start<Storage<ItemVariant>> stream) {
        itemStreams.add(stream);
    }

    public void addFluidStream(ResourceStream.Start<Storage<FluidVariant>> stream) {
        fluidStreams.add(stream);
    }

    public void addEnergyStream(ResourceStream.Start<EnergyStorage> stream) {
        energyStreams.add(stream);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        viewX = nbt.getDouble("viewX");
        viewY = nbt.getDouble("viewY");
        zoom = nbt.getInt("zoom");

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
        nbt.putDouble("viewX", viewX);
        nbt.putDouble("viewY", viewY);
        nbt.putInt("zoom", zoom);

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
                var errors = controller.evaluate();
                var itemSuccess = ResourceStreamEvaluator.evaluate(controller.itemStreams, Config.CONTROLLER_ITEM_TRANSFER_RATE.get(), (from, to, context) -> context <= 0 ? 0 : context - StorageUtil.move(from, to, variant -> true, context, null));
                var fluidSuccess = ResourceStreamEvaluator.evaluate(controller.fluidStreams, Config.CONTROLLER_FLUID_TRANSFER_RATE.get(), (from, to, context) -> context <= 0 ? 0 : context - StorageUtil.move(from, to, variant -> true, context, null));
                var energySuccess = ResourceStreamEvaluator.evaluate(controller.energyStreams, Config.CONTROLLER_ENERGY_TRANSFER_RATE.get(), (from, to, context) -> context <= 0 ? 0 : context - EnergyStorageUtil.move(from, to, context, null));
                controller.itemStreams.clear();
                controller.fluidStreams.clear();
                controller.energyStreams.clear();

                // Update error status in world
                if ((errors.isEmpty() && itemSuccess && fluidSuccess && energySuccess) != state.get(ControllerBlock.SUCCESS))
                    world.setBlockState(pos, state.cycle(ControllerBlock.SUCCESS), Block.NOTIFY_ALL);

                // Set up error list for gui
                controller.errors = new ArrayList<>(errors.stream().map(EvaluationError::getName).toList());
                if (!itemSuccess)
                    controller.errors.add(Text.translatable("block.advanced_networking.controller.item_sort_failed"));
                if (!fluidSuccess)
                    controller.errors.add(Text.translatable("block.advanced_networking.controller.fluid_sort_failed"));
                if (!energySuccess)
                    controller.errors.add(Text.translatable("block.advanced_networking.controller.energy_sort_failed"));

                controller.ticks = (byte) MathHelper.clamp(Config.CONTROLLER_TICK_RATE.get(), 1, 125);
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
        buf.writeInt(zoom);
        buf.writeDouble(viewX);
        buf.writeDouble(viewY);
        buf.writeCollection(errors, PacketByteBuf::writeText);
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
