package io.github.mattidragon.advancednetworking.graph.stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

final class ResourceStreams {
    private ResourceStreams() {}

    static final class Start<T> implements ResourceStream.Start<T> {
        final T storage;
        @Nullable
        ResourceStream.NotStart<T> next;

        Start(T storage) {
            this.storage = storage;
        }

        @Override
        public void setNext(@NotNull ResourceStream.NotStart<T> next) {
            this.next = next;
        }
    }

    static final class End<T> implements ResourceStream.NotStart<T> {
        final T storage;

        End(T storage) {
            this.storage = storage;
        }
    }

    static final class Split<T> implements ResourceStream.NotStart<T> {
        final ResourceStreamEvaluator.Ordering ordering = new ResourceStreamEvaluator.Ordering();
        final Dummy<T> first = new Dummy<>();
        final Dummy<T> second = new Dummy<>();

    }
    static final class Dummy<T> implements ResourceStream.Extendable<T>, ResourceStream.NotStart<T> {
        @Nullable
        ResourceStream.NotStart<T> next;

        @Override
        public void setNext(@NotNull ResourceStream.NotStart<T> next) {
            this.next = next;
        }
    }

    static final class Order<T> {
        final ResourceStreamEvaluator.Ordering ordering = new ResourceStreamEvaluator.Ordering();
        final Ordered first;
        final Ordered second;

        Order() {
            this.first = new Ordered(true);
            this.second = new Ordered(false);
        }

        final class Ordered implements ResourceStream.Extendable<T>, ResourceStream.NotStart<T> {
            @Nullable
            ResourceStream.NotStart<T> next;
            final boolean isFirst;

            private Ordered(boolean isFirst) {
                this.isFirst = isFirst;
            }

            public Order<T> getOwner() {
                return Order.this;
            }

            @Override
            public void setNext(@NotNull ResourceStream.NotStart<T> next) {
                this.next = next;
            }
        }
    }

    static final class Transform<T> implements ResourceStream.Extendable<T>, ResourceStream.NotStart<T> {
        final UnaryOperator<T> transformer;
        @Nullable
        ResourceStream.NotStart<T> next;

        Transform(UnaryOperator<T> transformer) {
            this.transformer = transformer;
        }

        @Override
        public void setNext(@NotNull ResourceStream.NotStart<T> next) {
            this.next = next;
        }
    }
}
