package io.github.mattidragon.advancednetworking.graph.node.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import java.util.function.Predicate;

public sealed interface FluidTransformer {
    void subtract(long value, TransactionContext transaction);
    long getLimit(FluidVariant variant);

    final class Filter implements FluidTransformer {
        private final Predicate<FluidVariant> predicate;

        public Filter(Predicate<FluidVariant> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void subtract(long value, TransactionContext transaction) {
        }

        @Override
        public long getLimit(FluidVariant variant) {
            return predicate.test(variant) ? Long.MAX_VALUE : 0;
        }
    }

    final class Limit extends SnapshotParticipant<Long> implements FluidTransformer {
        private long counter;

        public Limit(long counter) {
            this.counter = counter;
        }

        public long getLimit(FluidVariant variant) {
            return counter;
        }

        public void subtract(long value, TransactionContext transaction) {
            updateSnapshots(transaction);
            counter -= value;
        }

        @Override
        protected Long createSnapshot() {
            return counter;
        }

        @Override
        protected void readSnapshot(Long snapshot) {
            this.counter = snapshot;
        }
    }
}
