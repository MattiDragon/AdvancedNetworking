package io.github.mattidragon.advancednetworking.graph.path;

import java.util.*;
import java.util.stream.Collectors;

public final class PathBundle<S, T> {
    private final List<Path<S, T>> paths = new ArrayList<>();

    PathBundle() {
    }

    void mark(Ordering.Marker marker) {
        for (var path : paths) {
            path.mark(marker);
        }
    }

    public static <S, T> PathBundle<S, T> begin(S start) {
        var bundle = new PathBundle<S, T>();
        bundle.paths.add(new Path<>(start));
        return bundle;
    }

    /**
     * Performs a deep copy of this bundle. Used by split nodes.
     * An ordering marker pair is added to this and the split bundle making this bundle execute first.
     * @return The copied bundle
     */
    public PathBundle<S, T> split() {
        var markerLookup = generateMarkerLookup();
        var split = new PathBundle<S, T>();
        for (var path : paths) {
            split.paths.add(path.copy(markerLookup));
        }
        var ordering = new Ordering();
        this.mark(ordering.before);
        split.mark(ordering.after);
        return split;
    }

    /**
     * Generates a map converting the order markers on current paths into new markers.
     * If only one half of an ordering is present in the current path it is allowed to stay.
     * This is used when copying paths to ensure that each split gets its own set of internal order markers.
     */
    private Map<Ordering.Marker, Ordering.Marker> generateMarkerLookup() {
        var lookup = new HashMap<Ordering.Marker, Ordering.Marker>();
        var markers = paths.stream().map(Path::getMarkers).flatMap(Set::stream).collect(Collectors.toCollection(HashSet::new));

        // Add markers where only one half exists to lookup as themselves
        for (var iterator = markers.iterator(); iterator.hasNext(); ) {
            var marker = iterator.next();
            if (!markers.contains(marker.getOther())) {
                lookup.put(marker, marker);
                iterator.remove();
            }
        }

        markers.stream().map(Ordering.Marker::getOwner).distinct().forEach(ordering -> {
            var newOrdering = new Ordering();
            lookup.put(ordering.before, newOrdering.before);
            lookup.put(ordering.after, newOrdering.after);
        });

        return lookup;
    }

    /**
     * Merges this bundle with {@code other}. An ordering is added making the paths of this bundle execute first.
     * Both this bundle and {@code other} should be considered invalid after this.
     * @param other The bundle to merge with.
     * @return The merged bundle
     */
    public PathBundle<S, T> merge(PathBundle<S, T> other) {
        var ordering = new Ordering();
        this.mark(ordering.before);
        other.mark(ordering.after);

        var result = new PathBundle<S, T>();
        result.paths.addAll(this.paths);
        result.paths.addAll(other.paths);
        return result;
    }

    public void end(S end, PathEnvironment<S, T> environment) {
        for (var path : paths) {
            path.end(end);
        }
        environment.addPaths(paths);
    }

    public void transform(T transformer) {
        for (var path : paths) {
            path.addTransformer(transformer);
        }
    }
}
