package io.github.mattidragon.advancednetworking.graph.path;

final class Ordering {
    final Ordering.Marker before;
    final Ordering.Marker after;

    public Ordering() {
        this.before = new Ordering.Marker(true);
        this.after = new Ordering.Marker(false);
    }

    final class Marker {
        final boolean isBefore;

        private Marker(boolean isBefore) {
            this.isBefore = isBefore;
        }

        public Ordering getOwner() {
            return Ordering.this;
        }
    }
}
