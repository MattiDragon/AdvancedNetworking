package io.github.mattidragon.advancednetworking.graph.stream;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ResourceStreamEvaluator<T, C> {
    private final Set<Path<T>> paths = new LinkedHashSet<>();
    private final Map<ResourceStream.NotStart<T>, List<Path<T>>> inProgress = new LinkedHashMap<>();
    private final StreamApplier<T, C> applier;
    private C context;

    private ResourceStreamEvaluator(StreamApplier<T, C> applier, C context) {
        this.applier = applier;
        this.context = context;
    }

    public static <T, C> boolean evaluate(List<ResourceStream.Start<T>> starts, C initialContext, StreamApplier<T, C> applier) {
        return new ResourceStreamEvaluator<>(applier, initialContext).evaluate(starts);
    }

    private boolean evaluate(List<ResourceStream.Start<T>> starts) {
        starts.forEach(this::evaluateStart);

        while (!inProgress.isEmpty()) {
            var pair = inProgress.entrySet().iterator().next();
            inProgress.remove(pair.getKey());
            var node = pair.getKey();
            var paths = pair.getValue();

            if (node instanceof ResourceStreams.End<T> end)
                evaluateEnd(end, paths);
            else if (node instanceof ResourceStreams.Transform<T> transform)
                evaluateTransform(transform, paths);
            else if (node instanceof ResourceStreams.Split<T> split)
                evaluateSplit(split, paths);
            else if (node instanceof ResourceStreams.Order<T>.Ordered ordered)
                evaluateOrdered(ordered, paths);
            else if (node instanceof ResourceStreams.Dummy<T> dummy)
                evaluateDummy(dummy, paths);
        }

        var unsortedPhases = new HashMap<Set<Ordering.Marker>, Set<Path<T>>>();
        for (var path : paths) {
            unsortedPhases.computeIfAbsent(path.markers, __ -> new HashSet<>())
                    .add(path);
        }

        var sortedPaths = sortPaths(unsortedPhases);
        if (!unsortedPhases.isEmpty())
            return false; // Can't sort paths
        
        sortedPaths.forEach(this::runPath);
        return true;
    }

    private ArrayList<Path<T>> sortPaths(HashMap<Set<Ordering.Marker>, Set<Path<T>>> unsortedPhases) {
        var sortedPaths = new ArrayList<Path<T>>();
        var readyForSort = new ArrayList<Map.Entry<Set<Ordering.Marker>, Set<Path<T>>>>();

        for (var iterator = unsortedPhases.entrySet().iterator(); iterator.hasNext(); ) {
            var phase = iterator.next();
            if (phase.getKey().stream().allMatch(marker -> marker.isBefore)) {
                readyForSort.add(phase);
                iterator.remove();
            }
        }

        while (!readyForSort.isEmpty()) {
            var entry = readyForSort.remove(0);
            sortedPaths.addAll(entry.getValue());

            markers:
            for (var marker : entry.getKey()) {
                // Make sure no other phase has the same marker
                for (var markers : unsortedPhases.keySet()) {
                    if (markers.contains(marker))
                        continue markers; // Some other phase has the same marker; can't get rid of it.
                }

                // Remove the after marker as nothing has the before marker
                for (var markers : unsortedPhases.keySet()) {
                    markers.remove(marker.getOwner().after);
                }
            }

            for (var iterator = unsortedPhases.entrySet().iterator(); iterator.hasNext(); ) {
                var toCheck = iterator.next();
                if (toCheck.getKey().stream().allMatch(marker1 -> marker1.isBefore)) {
                    readyForSort.add(toCheck);
                    iterator.remove();
                }
            }
        }
        return sortedPaths;
    }

    private void runPath(Path<T> path) {
        var endStorage = path.end.getValue().storage;
        for (var transformer : path.transformers) {
            endStorage = transformer.transformer.apply(endStorage);
        }

        context = applier.apply(path.start.storage, endStorage, context);
    }

    private void addPaths(@NotNull ResourceStream.NotStart<T> node, List<Path<T>> paths) {
        inProgress.computeIfAbsent(node, __ -> new ArrayList<>())
                .addAll(paths);
    }

    private void evaluateStart(ResourceStream.Start<T> rawNode) {
        var node = (ResourceStreams.Start<T>) rawNode; // Only allowed subclass
        if (node.next == null) return;
        addPaths(node.next, List.of(new Path<>(node, new ArrayList<>(), new HashSet<>(), new MutableObject<>(null))));
    }

    private void evaluateDummy(ResourceStreams.Dummy<T> node, List<Path<T>> paths) {
        if (node.next != null)
            addPaths(node.next, paths);
    }

    private void evaluateSplit(ResourceStreams.Split<T> node, List<Path<T>> paths) {
        var secondPaths = paths.stream().map(Path::copy).toList();
        // Set up ordering
        paths.forEach(path -> path.markers.add(node.ordering.before));
        secondPaths.forEach(path -> path.markers.add(node.ordering.after));

        if (node.first.next != null)
            addPaths(node.first.next, paths);
        if (node.second.next != null)
            addPaths(node.second.next, secondPaths);
    }

    private void evaluateOrdered(ResourceStreams.Order<T>.Ordered node, List<Path<T>> paths) {
        // Set up ordering
        if (node.isFirst)
            paths.forEach(path -> path.markers.add(node.getOwner().ordering.before));
        else
            paths.forEach(path -> path.markers.add(node.getOwner().ordering.after));

        if (node.next != null)
            addPaths(node.next, paths);
    }

    private void evaluateTransform(ResourceStreams.Transform<T> node, List<Path<T>> paths) {
        paths.forEach(path -> path.transformers.add(node));

        if (node.next != null)
            addPaths(node.next, paths);
    }

    private void evaluateEnd(ResourceStreams.End<T> node, List<Path<T>> paths) {
        for (var path : paths) {
            path.end.setValue(node);
            this.paths.add(path);
        }
    }

    private record Path<T>(ResourceStreams.Start<T> start, ArrayList<ResourceStreams.Transform<T>> transformers, HashSet<Ordering.Marker> markers, MutableObject<ResourceStreams.End<T>> end) {
        public Path<T> copy() {
            if (end.getValue() != null)
                throw new IllegalStateException("can't split path after end");
            return new Path<>(start, new ArrayList<>(transformers), new HashSet<>(markers), new MutableObject<>(null));
        }
    }

    static final class Ordering {
        private final Marker before;
        private final Marker after;

        public Ordering() {
            this.before = new Marker(true);
            this.after = new Marker(false);
        }

        private class Marker {
            private final boolean isBefore;

            private Marker(boolean isBefore) {
                this.isBefore = isBefore;
            }

            public Ordering getOwner() {
                return Ordering.this;
            }
        }
    }
}
