package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;
import java.util.PriorityQueue;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;

public class DijkstraRouter extends AbstractRoutingAlgorithm {

    private double[][] scores;
    private boolean[][] visited;

    private IndexedPoint[][] sourceCells;

    @Override
    protected Route produceRoute(RouterConnection connection) {

        visited = new boolean[coordinates.getXCoordinates().size()][coordinates.getYCoordinates().size()];
        scores = new double[coordinates.getXCoordinates().size()][coordinates.getYCoordinates().size()];
        sourceCells = new IndexedPoint[coordinates.getXCoordinates().size()][coordinates.getYCoordinates().size()];

        final IndexedPoint source = coordinates.getIndexedCoordinate(connection.source.location);
        final IndexedPoint destination = coordinates.getIndexedCoordinate(connection.destination.location);

        solve(source, destination);

        List<IndexedPoint> path = buildPath(source, sourceCells);
        path = clearStraightLines(path);

        final Route route = new Route(connection.source, connection.destination);

        return augmentRouteSegments(route, path);
    }

    private void solve(IndexedPoint source, IndexedPoint destination) {

        analyser.initRouting(source, destination);

        final PriorityQueue<PointToVisit> visitQueue = new PriorityQueue<PointToVisit>();
        visitQueue.add(new PointToVisit(1.0, destination));

        while (!visitQueue.isEmpty()) {

            final PointToVisit visitPoint = visitQueue.poll();

            visited[visitPoint.location.x][visitPoint.location.y] = true;
            if (visitPoint.location.equals(source)) {
                return;
            }

            IndexedPoint lastPoint = sourceCells[visitPoint.location.x][visitPoint.location.y];

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

        final int newX = point.x + dx;
        final int newY = point.y + dy;

        Double newScore = analyser.getMovementCost(lastPoint.x, lastPoint.y, point.x, point.y, dx, dy);

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
