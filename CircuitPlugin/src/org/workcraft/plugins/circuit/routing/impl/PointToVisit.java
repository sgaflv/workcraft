package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;

public class PointToVisit implements Comparable<PointToVisit> {
    public final double score;
    public final IndexedPoint location;

    public PointToVisit(double score, IndexedPoint location) {
        this.score = score;
        this.location = location;
    }

    @Override
    public int compareTo(PointToVisit other) {
        assert this != other;

        final int compare = Double.compare(score, other.score);
        if (compare != 0) {
            return compare;
        }

        // compare coordinates to minimize randomness when the score is the same
        return location.compareTo(other.location);

    }

    @Override
    public String toString() {
        return "PointToVisit [score=" + score + ", location=" + location + "]";
    }

}
