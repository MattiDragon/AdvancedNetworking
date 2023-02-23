package io.github.mattidragon.advancednetworking.graph.path;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathEnvironment<S, T> {
    private final List<Path<S, T>> paths = new ArrayList<>();
    private boolean sorted = true;

    public void clear() {
        paths.clear();
        sorted = true;
    }

    void addPaths(List<Path<S, T>> paths) {
        this.paths.addAll(paths);
        this.sorted = false;
    }

    public <C> boolean evaluate(C initialContext, Applicator<S, T, C> applicator) {
        if (!sorted) {
            var sortSucceeded = trySort();
            if (!sortSucceeded)
                return false;
        }

        var context = initialContext;

        for (var path : paths) {
            if (path.getEnd() == null) {
                AdvancedNetworking.LOGGER.warn("Path in environment doesn't have end storage, skipping.");
                continue;
            }

            context = applicator.apply(path.getStart(), path.getEnd(), path.getTransformers(), context);
        }

        return true;
    }
    
    private boolean trySort() {
        if (sorted)
            return true;

        var unsortedPhases = LinkedHashMultimap.<Set<Ordering.Marker>, Path<S, T>>create();
        for (var path : paths) {
            unsortedPhases.put(path.getMarkers(), path);
        }
        paths.clear();

        var readyForSort = findSortable(unsortedPhases);

        while (!readyForSort.isEmpty()) {
            var entry = readyForSort.remove(0);
            paths.add(entry.getValue());

            markers:
            for (var marker : entry.getKey()) {
                // Make sure no other phase has the same marker
                for (var markers : unsortedPhases.keySet()) {
                    if (markers.contains(marker))
                        continue markers; // Some other phase has the same marker; can't get rid of it.
                }

                // Remove the after marker as nothing has the before marker
                clearMarker(marker.getOwner().after, unsortedPhases);
            }

            readyForSort.addAll(findSortable(unsortedPhases));
        }

        if (!unsortedPhases.isEmpty())
            return false;

        sorted = true;
        return true;
    }

    // We can't mutate the keys of a hash based map without breaking it, so we need to remove and add back them.
    // This ends up changing the order in the map, but it should be fine as this behaviour is predictable.
    private static <S, T> void clearMarker(Ordering.Marker marker, Multimap<Set<Ordering.Marker>, Path<S, T>> unsortedPhases) {
        var matching = new ArrayList<Map.Entry<Set<Ordering.Marker>, Path<S, T>>>();

        for (var iterator = unsortedPhases.entries().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            if (entry.getKey().contains(marker)) {
                iterator.remove();
                matching.add(entry);
                entry.getKey().remove(marker);
            }
        }

        for (var entry : matching) {
            unsortedPhases.put(entry.getKey(), entry.getValue());
        }
    }

    @NotNull
    private static <S, T> List<Map.Entry<Set<Ordering.Marker>, Path<S, T>>> findSortable(Multimap<Set<Ordering.Marker>, Path<S, T>> unsortedPhases) {
        var list = new ArrayList<Map.Entry<Set<Ordering.Marker>, Path<S, T>>>();
        for (var iterator = unsortedPhases.entries().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            if (entry.getKey().stream().allMatch(marker -> marker.isBefore)) {
                list.add(entry);
                iterator.remove();
            }
        }
        return list;
    }

    @FunctionalInterface
    public interface Applicator<S, T, C> {
        C apply(S from, S to, List<T> transformers, C context);
    }
}
