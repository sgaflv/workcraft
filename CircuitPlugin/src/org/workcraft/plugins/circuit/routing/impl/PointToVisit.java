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
        return Double.compare(score, other.score);
    }

}
