package io.github.mattidragon.advancednetworking.graph.path;

import java.util.*;

final class Path<S, T> {
    private final S start;
    private final List<T> transformers;
    private final Set<Ordering.Marker> markers;
    private S end;

    Path(S start) {
        this.start = start;
        this.transformers = new ArrayList<>();
        this.markers = new HashSet<>();
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

    Path<S, T> copy(Map<Ordering.Marker, Ordering.Marker> markerLookup) {
        var path = new Path<S, T>(start);
        path.transformers.addAll(transformers);
        path.end = end;
        markers.stream().map(markerLookup::get).forEach(path.markers::add);
        return path;
    }

    List<T> getTransformers() {
        return transformers;
    }
}
