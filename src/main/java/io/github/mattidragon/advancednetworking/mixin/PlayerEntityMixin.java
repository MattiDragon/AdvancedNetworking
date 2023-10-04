package io.github.mattidragon.advancednetworking.mixin;

import io.github.mattidragon.advancednetworking.misc.ClientScreenOpener;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements ClientScreenOpener {
}
