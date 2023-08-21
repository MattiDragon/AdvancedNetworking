package io.github.mattidragon.advancednetworking.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigClient {
    private static final Function<Integer, Text> INT_FORMATTER = (value) -> Text.of(String.format("%d", value));
    private static final Function<Long, Text> LONG_FORMATTER = (value) -> Text.of(String.format("%d", value));

    public static Screen createScreen(Screen parent, ConfigData config, Consumer<ConfigData> saveConsumer) {
        var data = config.toMutable();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.of("Advanced Networking"))
                .category(createCategory(data))
                .save(() -> saveConsumer.accept(data.toImmutable()))
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory createCategory(MutableConfigData instance) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.advanced_networking.category.controller"))
                .option(Option.<Integer>createBuilder()
                        .name(Text.translatable("config.advanced_networking.option.controller_tick_rate"))
                        .description(OptionDescription.of(Text.translatable("config.advanced_networking.option.controller_tick_rate.tooltip")))
                        .binding(ConfigData.DEFAULT.controllerTickRate(), instance::controllerTickRate, instance::controllerTickRate)
                        .controller(option -> IntegerFieldControllerBuilder.create(option).range(0, 125).valueFormatter(INT_FORMATTER))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.advanced_networking.option.controller_item_transfer_rate"))
                        .description(OptionDescription.of(Text.translatable("config.advanced_networking.option.controller_item_transfer_rate.tooltip")))
                        .binding(ConfigData.DEFAULT.controllerItemTransferRate(), instance::controllerItemTransferRate, instance::controllerItemTransferRate)
                        .controller(option -> LongFieldControllerBuilder.create(option).range(0L, 10 * 64L).valueFormatter(LONG_FORMATTER))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.advanced_networking.option.controller_fluid_transfer_rate"))
                        .description(OptionDescription.of(Text.translatable("config.advanced_networking.option.controller_fluid_transfer_rate.tooltip")))
                        .binding(ConfigData.DEFAULT.controllerFluidTransferRate(), instance::controllerFluidTransferRate, instance::controllerFluidTransferRate)
                        .controller(option -> LongFieldControllerBuilder.create(option).range(0L, 100 * FluidConstants.BUCKET).valueFormatter(LONG_FORMATTER))
                        .build())
                .option(Option.<Long>createBuilder()
                        .name(Text.translatable("config.advanced_networking.option.controller_energy_transfer_rate"))
                        .description(OptionDescription.of(Text.translatable("config.advanced_networking.option.controller_energy_transfer_rate.tooltip")))
                        .binding(ConfigData.DEFAULT.controllerEnergyTransferRate(), instance::controllerEnergyTransferRate, instance::controllerEnergyTransferRate)
                        .controller(option -> LongFieldControllerBuilder.create(option).range(0L, 100 * 256L).valueFormatter(LONG_FORMATTER))
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("config.advanced_networking.option.disable_regex_filtering"))
                        .description(OptionDescription.of(Text.translatable("config.advanced_networking.option.disable_regex_filtering.tooltip")))
                        .binding(ConfigData.DEFAULT.disableRegexFilter(), instance::disableRegexFilter, instance::disableRegexFilter)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("config.advanced_networking.option.show_adventure_mode_toggles"))
                        .description(OptionDescription.of(Text.translatable("config.advanced_networking.option.show_adventure_mode_toggles.tooltip")))
                        .binding(ConfigData.DEFAULT.showAdventureModeToggles(), instance::showAdventureModeToggles, instance::showAdventureModeToggles)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                .build();
    }
}
