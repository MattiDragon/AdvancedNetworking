package io.github.mattidragon.advancednetworking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.data.DataOutput;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Temporary hack to generate tags correctly. See: https://github.com/FabricMC/fabric/issues/3894
@Mixin(DataOutput.class)
public class DataOutputMixin {
    @ModifyExpressionValue(method = "getTagResolver", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/RegistryKeys;getTagPath(Lnet/minecraft/registry/RegistryKey;)Ljava/lang/String;"))
    private String fixTagPath(String original, RegistryKey<? extends Registry<?>> registryRef) {
        var id = registryRef.getValue();
        if (id.getNamespace().equals("minecraft")) {
            return original;
        }
        return "tags/" + id.getNamespace() + "/" + id.getPath();
    }
}
