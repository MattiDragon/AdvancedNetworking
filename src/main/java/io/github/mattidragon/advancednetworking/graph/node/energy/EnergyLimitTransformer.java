package io.github.mattidragon.advancednetworking.graph.node.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class EnergyLimitTransformer extends SnapshotParticipant<Long> {
    private long counter;

    public EnergyLimitTransformer(long counter) {
        this.counter = counter;
    }

    public long getCounter() {
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
