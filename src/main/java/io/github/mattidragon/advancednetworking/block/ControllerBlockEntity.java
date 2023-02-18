package io.github.mattidragon.advancednetworking.block;

import com.kneelawk.graphlib.GraphLib;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.advancednetworking.config.Config;
import io.github.mattidragon.advancednetworking.graph.NetworkControllerContext;
import io.github.mattidragon.advancednetworking.graph.node.energy.EnergyLimitTransformer;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import io.github.mattidragon.advancednetworking.graph.path.PathEnvironment;
import io.github.mattidragon.advancednetworking.misc.NbtUtils;
import io.github.mattidragon.advancednetworking.misc.StorageHelper;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import io.github.mattidragon.advancednetworking.screen.ControllerScreenHandler;
import io.github.mattidragon.nodeflow.graph.Graph;
import io.github.mattidragon.nodeflow.graph.context.Context;
import io.github.mattidragon.nodeflow.graph.context.ContextType;
import io.github.mattidragon.nodeflow.misc.EvaluationError;
import io.github.mattidragon.nodeflow.misc.GraphProvidingBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ControllerBlockEntity extends GraphProvidingBlockEntity {
    private Graph graph = new Graph(AdvancedNetworking.ENVIRONMENT);

    public final PathEnvironment<Storage<ItemVariant>, ItemTransformer> itemEnvironment = new PathEnvironment<>();
    public final PathEnvironment<Storage<FluidVariant>, FluidTransformer> fluidEnvironment = new PathEnvironment<>();
    public final PathEnvironment<EnergyStorage, EnergyLimitTransformer> energyEnvironment = new PathEnvironment<>();

    public double viewX = 0;
    public double viewY = 0;
    public int zoom = 0;
    private List<Text> errors = List.of();

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
        viewX = nbt.getDouble("viewX");
        viewY = nbt.getDouble("viewY");
        zoom = nbt.getInt("zoom");

        graph.readNbt(nbt.getCompound("graph"));
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
        NbtUtils.writeStrings(nbt, "errors", errors.stream().map(Text.Serializer::toJson).toList());
    }

    public static void tick(World world, BlockPos pos, BlockState state, ControllerBlockEntity controller) {
        if (world.isClient) return;


        controller.itemEnvironment.clear();
        controller.fluidEnvironment.clear();
        controller.energyEnvironment.clear();

        var errors = controller.evaluate();
        boolean itemSuccess, fluidSuccess, energySuccess;
        try (var transaction = Transaction.openOuter()) {
            itemSuccess = controller.itemEnvironment.evaluate(Config.CONTROLLER_ITEM_TRANSFER_RATE.get(), (from, to, transformers, context) -> context <= 0 ? 0 : context - StorageHelper.moveItems(from, to, transformers, context, transaction));
            fluidSuccess = controller.fluidEnvironment.evaluate(Config.CONTROLLER_FLUID_TRANSFER_RATE.get(), (from, to, transformers, context) -> context <= 0 ? 0 : context - StorageHelper.moveFluids(from, to, transformers, context, transaction));
            energySuccess = controller.energyEnvironment.evaluate(Config.CONTROLLER_ENERGY_TRANSFER_RATE.get(), (from, to, transformers, context) -> context <= 0 ? 0 : context - StorageHelper.moveEnergy(from, to, transformers, context, transaction));
            if (errors.isEmpty() && itemSuccess && fluidSuccess && energySuccess)
                transaction.commit();
        }

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
