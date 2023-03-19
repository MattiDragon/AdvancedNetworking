package io.github.mattidragon.advancednetworking.config;

import com.kyanite.paragon.api.ConfigOption;

import java.util.function.Predicate;

public class ValidatingConfigOption<T> extends ConfigOption<T>  {
    private final Predicate<T> predicate;

    public ValidatingConfigOption(String title, T defaultValue, Predicate<T> predicate) {
        super(title, defaultValue);
        this.predicate = predicate;
    }

    @Override
    public void setValue(T value) {
        super.setValue(value != null && predicate.test(value) ? value : null);
    }
}
