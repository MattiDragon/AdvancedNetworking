package io.github.mattidragon.advancednetworking.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mattidragon.configloader.api.DefaultedFieldCodec;
import io.github.mattidragon.configloader.api.GenerateMutable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

import java.util.function.Function;

@GenerateMutable
public record ConfigData(int controllerTickRate, long controllerFluidTransferRate, long controllerItemTransferRate, long controllerEnergyTransferRate, boolean disableRegexFilter) {
    public static final ConfigData DEFAULT = new ConfigData(10, FluidConstants.BUCKET, 64, 256, false);
    public static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DefaultedFieldCodec.of(Codec.intRange(0, 120), "controller_tick_rate", DEFAULT.controllerTickRate).forGetter(ConfigData::controllerTickRate),
            DefaultedFieldCodec.of(longRange(FluidConstants.BUCKET * 100), "controller_fluid_transfer_rate", DEFAULT.controllerFluidTransferRate).forGetter(ConfigData::controllerFluidTransferRate),
            DefaultedFieldCodec.of(longRange(64 * 10), "controller_item_transfer_rate", DEFAULT.controllerItemTransferRate).forGetter(ConfigData::controllerItemTransferRate),
            DefaultedFieldCodec.of(longRange(256 * 100), "controller_energy_transfer_rate", DEFAULT.controllerEnergyTransferRate).forGetter(ConfigData::controllerEnergyTransferRate),
            DefaultedFieldCodec.of(Codec.BOOL, "disable_regex_filtering", DEFAULT.disableRegexFilter).forGetter(ConfigData::disableRegexFilter)
    ).apply(instance, ConfigData::new));

    public MutableConfigData toMutable() {
        return new MutableConfigData(this);
    }

    private static Codec<Long> longRange(long maxInclusive) {
        var check = rangeCheck(maxInclusive);
        return Codec.LONG.flatXmap(check, check);
    }

    private static Function<Long, DataResult<Long>> rangeCheck(long maxInclusive) {
        return value -> value >= 0 && value <= maxInclusive ? DataResult.success(value) : DataResult.error(() -> "%s is outside of allowed range: 0-%s".formatted(value, maxInclusive));
    }
}
