package io.github.mattidragon.advancednetworking.graph.node.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import java.util.function.Predicate;

public interface ItemTransformer {
    void subtract(long value, TransactionContext transaction);
    long getLimit(ItemVariant variant);

    final class Filter implements ItemTransformer {
        private final Predicate<ItemVariant> predicate;

        public Filter(Predicate<ItemVariant> predicate) {
            this.predicate = predicate;
        }

        @Override
        public void subtract(long value, TransactionContext transaction) {
        }

        @Override
        public long getLimit(ItemVariant variant) {
            return predicate.test(variant) ? Long.MAX_VALUE : 0;
        }
    }

    final class Limit extends SnapshotParticipant<Long> implements ItemTransformer {
        private long counter;

        public Limit(long counter) {
            this.counter = counter;
        }

        public long getLimit(ItemVariant variant) {
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
