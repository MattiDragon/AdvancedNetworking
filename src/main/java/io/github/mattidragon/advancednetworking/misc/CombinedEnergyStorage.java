package io.github.mattidragon.advancednetworking.misc;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class CombinedEnergyStorage implements EnergyStorage {
    private final List<EnergyStorage> children;

    public CombinedEnergyStorage(List<EnergyStorage> children) {
        this.children = children;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        var inserted = 0L;
        for (var child : children) {
            inserted += child.insert(maxAmount - inserted, transaction);
            if (inserted >= maxAmount) break;
        }
        return inserted;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        var extracted = 0L;
        for (var child : children) {
            extracted += child.extract(maxAmount - extracted, transaction);
            if (extracted >= maxAmount) break;
        }
        return extracted;
    }

    @Override
    public long getAmount() {
        return children.stream().mapToLong(EnergyStorage::getAmount).sum();
    }

    @Override
    public long getCapacity() {
        return children.stream().mapToLong(EnergyStorage::getCapacity).sum();
    }

    @Override
    public boolean supportsInsertion() {
        return children.stream().anyMatch(EnergyStorage::supportsInsertion);
    }

    @Override
    public boolean supportsExtraction() {
        return children.stream().anyMatch(EnergyStorage::supportsExtraction);
    }
}
