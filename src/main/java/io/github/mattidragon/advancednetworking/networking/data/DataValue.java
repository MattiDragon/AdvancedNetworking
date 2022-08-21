package io.github.mattidragon.advancednetworking.networking.data;

public record DataValue<T>(DataType<T> type, T value) {
    public <O> O getAs(DataType<O> other) {
        if (type != other) throw new ClassCastException("Tried to get input as wrong type!");
        //noinspection unchecked
        return (O) value;
    }
}
