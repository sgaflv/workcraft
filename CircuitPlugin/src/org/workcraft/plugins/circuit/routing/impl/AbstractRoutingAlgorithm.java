package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;

public abstract class AbstractRoutingAlgorithm {
    protected CellAnalyser analyser;
    protected RouterTask task;
    protected RouterCells cells;
    protected CoordinatesRegistry coordinates;

    public List<Route> route(RouterTask task, RouterCells cells, CoordinatesRegistry coordinates) {
        analyser = new CellAnalyser(cells, coordinates);
        this.task = task;
        this.cells = cells;
        this.coordinates = coordinates;

        final List<Route> routes = new ArrayList<>();

        for (final RouterConnection connection : task.getConnections()) {

            final Route routeProduced = produceRoute(connection);

            if (routeProduced != null) {
                routes.add(routeProduced);
            }
        }

        return routes;
    }

    /**
     * Augment route with path information.
     *
     * @param path
     *            the list of indexed points representing the path
     * @return route with path information added
     */
    protected Route augmentRouteSegments(Route route, List<IndexedPoint> path) {

        for (final IndexedPoint point : path) {
            route.add(coordinates.getPoint(point.x, point.y));
        }
        return route;
    }

    /**
     * Remove points not forming route corners or end-points.
     *
     * @param path
     *            list of indexed points forming the route segments
     * @return new list of points without points in the middle of route segments
     */
    protected List<IndexedPoint> clearStraightLines(List<IndexedPoint> path) {
        return path;
    }

    /**
     * from the given graph and the end-points, find the route path
     *
     * @return
     */
    protected List<IndexedPoint> buildPath(IndexedPoint source, IndexedPoint[][] sourceCells) {
        final List<IndexedPoint> path = new ArrayList<IndexedPoint>();
        path.add(source);

        IndexedPoint next = source;
        do {
            next = sourceCells[next.x][next.y];

            if (next != null) {
                path.add(next);
            }
        } while (next != null);

        return path;
    }

    abstract protected Route produceRoute(RouterConnection connection);
}
