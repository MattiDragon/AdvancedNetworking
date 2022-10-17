package io.github.mattidragon.advancednetworking.mixin;

import io.github.mattidragon.advancednetworking.block.CableBlock;
import io.github.mattidragon.advancednetworking.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
    @Inject(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void advanced_networking$injectCableConnections(BlockState state, Direction dir, CallbackInfoReturnable<Boolean> cir) {
        if (state.isOf(ModBlocks.CABLE)) {
            cir.setReturnValue(dir != null && state.get(CableBlock.FACING_PROPERTIES.get(dir.getOpposite())).isInterface());
        }
    }
}
