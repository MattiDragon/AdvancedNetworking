package io.github.mattidragon.advancednetworking.config;

import com.kyanite.paragon.api.ConfigOption;
import dev.isxander.yacl.api.Binding;
import dev.isxander.yacl.api.ConfigCategory;
import dev.isxander.yacl.api.Option;
import dev.isxander.yacl.api.YetAnotherConfigLib;
import dev.isxander.yacl.gui.controllers.slider.IntegerSliderController;
import dev.isxander.yacl.gui.controllers.slider.LongSliderController;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import io.github.mattidragon.nodeflow.ui.MessageToast;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.function.Function;

public class ConfigClient {
    private static final Function<Integer, Text> INT_FORMATTER = (value) -> Text.of(String.format("%d", value));
    private static final Function<Long, Text> LONG_FORMATTER = (value) -> Text.of(String.format("%d", value));

    private static final YetAnotherConfigLib CONFIG = YetAnotherConfigLib.createBuilder()
            .save(() -> {
                try {
                    Config.INSTANCE.save();
                } catch (IOException e) {
                    AdvancedNetworking.LOGGER.warn("Failed to save config", e);
                    MinecraftClient.getInstance().getToastManager().add(new MessageToast(Text.translatable("config.advanced_networking.save.fail")));
                }
            })
            .title(Text.of("Advanced Networking"))
            .category(ConfigCategory.createBuilder()
                    .name(Text.translatable("config.advanced_networking.category.controller"))
                    .option(Option.createBuilder(Integer.class)
                            .name(Text.translatable("config.advanced_networking.option.controller_tick_rate"))
                            .tooltip(Text.translatable("config.advanced_networking.option.controller_tick_rate.tooltip"))
                            .binding(binding(Config.CONTROLLER_TICK_RATE))
                            .controller(option -> new IntegerSliderController(option, 0, 125, 5, INT_FORMATTER))
                            .build())
                    .option(Option.createBuilder(Long.class)
                            .name(Text.translatable("config.advanced_networking.option.controller_item_transfer_rate"))
                            .tooltip(Text.translatable("config.advanced_networking.option.controller_item_transfer_rate.tooltip"))
                            .binding(binding(Config.CONTROLLER_ITEM_TRANSFER_RATE))
                            .controller(option -> new LongSliderController(option, 0, 10 * 64, 16, LONG_FORMATTER))
                            .build())
                    .option(Option.createBuilder(Long.class)
                            .name(Text.translatable("config.advanced_networking.option.controller_fluid_transfer_rate"))
                            .tooltip(Text.translatable("config.advanced_networking.option.controller_fluid_transfer_rate.tooltip"))
                            .binding(binding(Config.CONTROLLER_FLUID_TRANSFER_RATE))
                            .controller(option -> new LongSliderController(option, 0, 100 * FluidConstants.BUCKET, 1000, LONG_FORMATTER))
                            .build())
                    .option(Option.createBuilder(Long.class)
                            .name(Text.translatable("config.advanced_networking.option.controller_energy_transfer_rate"))
                            .tooltip(Text.translatable("config.advanced_networking.option.controller_energy_transfer_rate.tooltip"))
                            .binding(binding(Config.CONTROLLER_ENERGY_TRANSFER_RATE))
                            .controller(option -> new LongSliderController(option, 0, 100 * 256, 64, LONG_FORMATTER))
                            .build())
                    .build())
            .build();

    public static Screen createConfigScreen(Screen parent) {
        return CONFIG.generateScreen(parent);
    }

    private static <T> Binding<T> binding(ConfigOption<T> option) {
        return new Binding<>() {
            @Override
            public void setValue(T value) {
                option.setValue(value);
            }

            @Override
            public T getValue() {
                return option.get();
            }

            @Override
            public T defaultValue() {
                return option.getDefaultValue();
            }
        };
    }
}
