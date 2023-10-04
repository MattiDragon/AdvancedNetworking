package io.github.mattidragon.advancednetworking.client.mixin;

import io.github.mattidragon.advancednetworking.client.screen.CableConfigScreen;
import io.github.mattidragon.advancednetworking.misc.InterfaceType;
import io.github.mattidragon.advancednetworking.mixin.PlayerEntityMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends PlayerEntityMixin {
    @Shadow @Final protected MinecraftClient client;

    @Override
    public void advancednetworking$openCableConfigScreen(BlockPos pos, Direction direction, Function<Direction, InterfaceType> typeSupplier, Function<Direction, String> nameSupplier, boolean allowAdventureModeAccess) {
        client.setScreen(new CableConfigScreen(pos, direction, typeSupplier, nameSupplier, allowAdventureModeAccess));
    }
}
