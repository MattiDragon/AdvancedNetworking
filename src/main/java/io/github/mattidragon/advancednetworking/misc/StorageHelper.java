package io.github.mattidragon.advancednetworking.misc;

import io.github.mattidragon.advancednetworking.graph.node.energy.EnergyLimitTransformer;
import io.github.mattidragon.advancednetworking.graph.node.fluid.FluidTransformer;
import io.github.mattidragon.advancednetworking.graph.node.item.ItemTransformer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public final class StorageHelper {
    private StorageHelper() {
    }

    public static long moveItems(@Nullable Storage<ItemVariant> from, @Nullable Storage<ItemVariant> to, List<ItemTransformer> transformers, long maxAmount, TransactionContext transaction) {
        if (from == null || to == null)
            return 0;

        var totalMoved = 0L;

        try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
            for (StorageView<ItemVariant> view : from) {
                if (view.isResourceBlank()) continue;
                var resource = view.getResource();
                var allowed = maxAmount - totalMoved;
                for (var transformer : transformers) {
                    allowed = Math.min(allowed, transformer.getLimit(resource));
                }
                if (allowed == 0)
                    continue;

                long maxExtracted;

                // check how much can be extracted
                try (Transaction extractionTestTransaction = iterationTransaction.openNested()) {
                    maxExtracted = view.extract(resource, allowed, extractionTestTransaction);
                    extractionTestTransaction.abort();
                }

                try (Transaction transferTransaction = iterationTransaction.openNested()) {
                    // check how much can be inserted
                    var accepted = to.insert(resource, maxExtracted, transferTransaction);

                    for (var transformer : transformers) {
                        transformer.subtract(accepted, transaction);
                    }

                    // extract it, or rollback if the amounts don't match
                    if (view.extract(resource, accepted, transferTransaction) == accepted) {
                        totalMoved += accepted;
                        transferTransaction.commit();
                    }
                }

                if (maxAmount == totalMoved) {
                    // early return if nothing can be moved anymore
                    iterationTransaction.commit();
                    return totalMoved;
                }
            }

            iterationTransaction.commit();
        }

        return totalMoved;
    }

    public static long moveFluids(@Nullable Storage<FluidVariant> from, @Nullable Storage<FluidVariant> to, List<FluidTransformer> transformers, long maxAmount, TransactionContext transaction) {
        if (from == null || to == null)
            return 0;

        var totalMoved = 0L;

        try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
            for (StorageView<FluidVariant> view : from) {
                if (view.isResourceBlank()) continue;
                var resource = view.getResource();
                var allowed = maxAmount - totalMoved;
                for (var transformer : transformers) {
                    allowed = Math.min(allowed, transformer.getLimit(resource));
                }
                if (allowed == 0)
                    continue;

                long maxExtracted;

                // check how much can be extracted
                try (Transaction extractionTestTransaction = iterationTransaction.openNested()) {
                    maxExtracted = view.extract(resource, allowed, extractionTestTransaction);
                    extractionTestTransaction.abort();
                }

                try (Transaction transferTransaction = iterationTransaction.openNested()) {
                    // check how much can be inserted
                    var accepted = to.insert(resource, maxExtracted, transferTransaction);

                    for (var transformer : transformers) {
                        transformer.subtract(maxAmount - allowed, transaction);
                    }

                    // extract it, or rollback if the amounts don't match
                    if (view.extract(resource, accepted, transferTransaction) == accepted) {
                        totalMoved += accepted;
                        transferTransaction.commit();
                    }
                }

                if (maxAmount == totalMoved) {
                    // early return if nothing can be moved anymore
                    iterationTransaction.commit();
                    return totalMoved;
                }
            }

            iterationTransaction.commit();
        }

        return totalMoved;
    }

    public static long moveEnergy(@Nullable EnergyStorage from, @Nullable EnergyStorage to, List<EnergyLimitTransformer> transformers, long maxAmount, TransactionContext transaction) {
        if (from == null || to == null) return 0;

        StoragePreconditions.notNegative(maxAmount);

        var allowed = maxAmount;
        for (var transformer : transformers) {
            allowed = Math.min(allowed, transformer.getCounter());
        }

        // Simulate extraction first.
        long maxExtracted;
        try (Transaction extractionTestTransaction = Transaction.openNested(transaction)) {
            maxExtracted = from.extract(allowed, extractionTestTransaction);
        }

        try (Transaction moveTransaction = Transaction.openNested(transaction)) {
            // Then insert what can be extracted.
            long accepted = to.insert(maxExtracted, moveTransaction);

            for (var transformer : transformers) {
                transformer.subtract(accepted, transaction);
            }

            // Extract for real.
            if (from.extract(accepted, moveTransaction) == accepted) {
                // Commit if the amounts match.
                moveTransaction.commit();
                return accepted;
            }
        }

        return 0;
    }
}
