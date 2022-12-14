package io.github.mattidragon.advancednetworking.config;

import com.kyanite.paragon.api.ConfigOption;
import com.kyanite.paragon.api.interfaces.configtypes.JSONModConfig;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public class Config implements JSONModConfig {
    public static final Config INSTANCE = new Config();

    public static final ConfigOption<Integer> CONTROLLER_TICK_RATE = new ValidatingConfigOption<>("controller_tick_rate", 10, value -> value <= 120 && value >= 0);
    public static final ConfigOption<Long> CONTROLLER_FLUID_TRANSFER_RATE = new ValidatingConfigOption<>("controller_fluid_transfer_rate", FluidConstants.BUCKET, value -> value <= 100 * FluidConstants.BUCKET && value >= 0);
    public static final ConfigOption<Long> CONTROLLER_ITEM_TRANSFER_RATE = new ValidatingConfigOption<>("controller_item_transfer_rate", 64L, value -> value <= 10 * 64 && value >= 0);
    public static final ConfigOption<Long> CONTROLLER_ENERGY_TRANSFER_RATE = new ValidatingConfigOption<>("controller_energy_transfer_rate", 256L, value -> value <= 100 * 256 && value >= 0);

    @Override
    public String getModId() {
        return "advanced_networking";
    }
}
