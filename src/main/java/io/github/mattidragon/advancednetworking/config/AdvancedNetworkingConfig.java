package io.github.mattidragon.advancednetworking.config;

import com.kyanite.paragon.api.ConfigOption;
import com.kyanite.paragon.api.interfaces.Config;
import com.kyanite.paragon.api.interfaces.serializers.ConfigSerializer;
import com.kyanite.paragon.api.interfaces.serializers.JSONSerializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public class AdvancedNetworkingConfig implements Config {
    public static final AdvancedNetworkingConfig INSTANCE = new AdvancedNetworkingConfig();

    public static final ConfigOption<Integer> CONTROLLER_TICK_RATE = new ValidatingConfigOption<>("controller_tick_rate", 10, value -> value <= 120 && value >= 0);
    public static final ConfigOption<Long> CONTROLLER_FLUID_TRANSFER_RATE = new ValidatingConfigOption<>("controller_fluid_transfer_rate", FluidConstants.BUCKET, value -> value <= 100 * FluidConstants.BUCKET && value >= 0);
    public static final ConfigOption<Long> CONTROLLER_ITEM_TRANSFER_RATE = new ValidatingConfigOption<>("controller_item_transfer_rate", 64L, value -> value <= 10 * 64 && value >= 0);
    public static final ConfigOption<Long> CONTROLLER_ENERGY_TRANSFER_RATE = new ValidatingConfigOption<>("controller_energy_transfer_rate", 256L, value -> value <= 100 * 256 && value >= 0);
    public static final ConfigOption<Boolean> DISABLE_REGEX_FILTERING = new ConfigOption<>("disable_regex_filtering", false);

    @Override
    public ConfigSerializer getSerializer() {
        //noinspection deprecation
        return JSONSerializer.builder(this).build();
    }
}
