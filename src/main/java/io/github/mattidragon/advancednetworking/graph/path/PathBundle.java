package io.github.mattidragon.advancednetworking.graph.path;

import java.util.ArrayList;
import java.util.List;

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
        var split = new PathBundle<S, T>();
        for (var path : paths) {
            split.paths.add(new Path<>(path));
        }
        var ordering = new Ordering();
        this.mark(ordering.before);
        split.mark(ordering.after);
        return split;
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
