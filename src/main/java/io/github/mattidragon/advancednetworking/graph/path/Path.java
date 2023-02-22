package io.github.mattidragon.advancednetworking.graph.path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Path<S, T> {
    private final S start;
    private final List<T> transformers;
    private final Set<Ordering.Marker> markers;
    private S end;

    Path(S start) {
        this.start = start;
        this.transformers = new ArrayList<>();
        this.markers = new HashSet<>();
    }

    Path(Path<S, T> other) {
        this.start = other.start;
        this.transformers = new ArrayList<>(other.transformers);
        this.end = other.end;
        this.markers = new HashSet<>(other.markers);
    }

    Set<Ordering.Marker> getMarkers() {
        return markers;
    }

    void addTransformer(T transformer) {
        transformers.add(transformer);
    }

    void end(S end) {
        this.end = end;
    }

    void mark(Ordering.Marker marker) {
        this.markers.add(marker);
    }

    S getEnd() {
        return end;
    }

    S getStart() {
        return start;
    }

    public List<T> getTransformers() {
        return transformers;
    }
}
