package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;
import java.util.PriorityQueue;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;

public class DijkstraRouter extends AbstractRoutingAlgorithm {

    private double[][] scores;
    private boolean[][] visited;

    private IndexedPoint[][] sourceCells;

    @Override
    protected List<IndexedPoint> findRoute() {

        visited = new boolean[width][height];
        scores = new double[width][height];
        sourceCells = new IndexedPoint[width][height];

        solve();

        final List<IndexedPoint> path = buildPath(source, sourceCells);

        return path;
    }


    private void solve() {

        final PriorityQueue<PointToVisit> visitQueue = new PriorityQueue<PointToVisit>();
        visitQueue.add(new PointToVisit(1.0, destination));

        while (!visitQueue.isEmpty()) {

            final PointToVisit visitPoint = visitQueue.poll();

            visited[visitPoint.location.getX()][visitPoint.location.getY()] = true;
            if (visitPoint.location.equals(source)) {
                return;
            }

            IndexedPoint lastPoint = sourceCells[visitPoint.location.getX()][visitPoint.location.getY()];

            if (lastPoint == null) {
                lastPoint = visitPoint.location;
            }

            checkDirection(visitQueue, visitPoint.score, lastPoint, visitPoint.location, 1, 0);
            checkDirection(visitQueue, visitPoint.score, lastPoint, visitPoint.location, -1, 0);
            checkDirection(visitQueue, visitPoint.score, lastPoint, visitPoint.location, 0, 1);
            checkDirection(visitQueue, visitPoint.score, lastPoint, visitPoint.location, 0, -1);

        }
    }

    private void checkDirection(PriorityQueue<PointToVisit> visitQueue, double score, IndexedPoint lastPoint,
            IndexedPoint point, int dx, int dy) {

        final int newX = point.getX() + dx;
        final int newY = point.getY() + dy;

        Double newScore = analyser.getMovementCost(lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(), dx, dy);

        if (newScore != null) {

            if (visited[newX][newY]) {
                return;
            }

            newScore += score;
            if (scores[newX][newY] == 0 || newScore < scores[newX][newY]) {
                scores[newX][newY] = newScore;
                sourceCells[newX][newY] = point;
                visitQueue.add(new PointToVisit(newScore, IndexedPoint.create(newX, newY)));
            }
        }

    }

}
