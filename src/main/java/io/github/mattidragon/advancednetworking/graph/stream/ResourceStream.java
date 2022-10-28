package io.github.mattidragon.advancednetworking.graph.stream;

import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public sealed interface ResourceStream {
    static <T> ResourceStreams.Start<T> start(T storage) {
        return new ResourceStreams.Start<>(storage);
    }

   sealed interface Extendable<T> extends ResourceStream permits Start, ResourceStreams.Dummy, ResourceStreams.Order.Ordered, ResourceStreams.Transform {
        void setNext(@NotNull ResourceStream.NotStart<T> next);

        default ResourceStreams.End<T> end(T storage) {
            var end = new ResourceStreams.End<>(storage);
            setNext(end);
            return end;
        }

        default ResourceStreams.Transform<T> transform(UnaryOperator<T> transformer) {
            var transform = new ResourceStreams.Transform<>(transformer);
            setNext(transform);
            return transform;
        }

        default Pair<ResourceStreams.Dummy<T>, ResourceStreams.Dummy<T>> split() {
            var split = new ResourceStreams.Split<T>();
            setNext(split);
            return new Pair<>(split.first, split.second);
        }

        default ResourceStreams.Dummy<T> join(Extendable<T> other) {
            var order = new ResourceStreams.Order<T>();
            setNext(order.first);
            other.setNext(order.second);
            var next = new ResourceStreams.Dummy<T>();
            order.first.setNext(next);
            order.second.setNext(next);
            return next;
        }
    }

    sealed interface Start<T> extends Extendable<T> permits ResourceStreams.Start {}

    @SuppressWarnings("unused") // Type parameter is necessary
    sealed interface NotStart<T> extends ResourceStream permits ResourceStreams.Dummy, ResourceStreams.End, ResourceStreams.Order.Ordered, ResourceStreams.Split, ResourceStreams.Transform { }
}
