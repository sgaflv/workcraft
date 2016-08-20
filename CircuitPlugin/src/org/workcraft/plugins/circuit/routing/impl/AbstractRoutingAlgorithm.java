package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.PortDirection;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;

public abstract class AbstractRoutingAlgorithm {
    protected CellAnalyser analyser;
    protected RouterTask task;
    protected RouterCells cells;
    protected CoordinatesRegistry coordinatesPhase1;

    protected UsageCounter usageCounter;

    protected int width;
    protected int height;
    protected IndexedPoint source;
    protected IndexedPoint destination;

    public List<Route> route(RouterTask task, RouterCells cells, CoordinatesRegistry coordinates) {
        analyser = new CellAnalyser(cells);

        width = coordinates.getXCoordinates().size();
        height = coordinates.getYCoordinates().size();

        this.task = task;
        this.cells = cells;
        coordinatesPhase1 = coordinates;

        List<Route> routes = new ArrayList<>();
        List<List<IndexedPoint>> paths = new ArrayList<List<IndexedPoint>>();

        for (RouterConnection connection : task.getConnections()) {

            initialise(connection);

            List<IndexedPoint> path = findRoute();
            path = getCleanPath(path);
            paths.add(path);

            Route route = new Route(connection.getSource(), connection.getDestination());
            augmentRouteSegments(route, path);

            if (route != null) {
                routes.add(route);
            }
        }

        usageCounter = new UsageCounter(width, height);

        for (List<IndexedPoint> path : paths) {
            for (int i = 1; i < path.size(); i++) {
                IndexedPoint p1 = path.get(i - 1);
                IndexedPoint p2 = path.get(i);

                usageCounter.markUsage(p1.getX(), p1.getY(), p2.getX(), p2.getY());

            }
        }


        // for (int x = 0; x < width; x++) {
        // System.out.print(usageCounter.getXCoordUsage(x) + " ");
        // }
        // System.out.println();

        return routes;
    }

    private void initialise(RouterConnection connection) {
        source = coordinatesPhase1.getIndexedCoordinate(connection.getSource().getLocation());
        destination = coordinatesPhase1.getIndexedCoordinate(connection.getDestination().getLocation());

        initialiseAnalyser(connection);
    }

    private void initialiseAnalyser(RouterConnection connection) {

        PortDirection sourceDirection = null;
        PortDirection destinationDirection = null;

        if (connection.getSource().isFixedDirection) {
            sourceDirection = connection.getSource().getDirection();
        }

        if (connection.getDestination().isFixedDirection) {
            destinationDirection = connection.getDestination().getDirection();
        }

        analyser.initRouting(source, destination, sourceDirection, destinationDirection);
    }

    /**
     * Augment route with path information.
     *
     * @param path
     *            the list of indexed points representing the path
     * @return route with path information added
     */
    protected Route augmentRouteSegments(Route route, List<IndexedPoint> path) {

        for (IndexedPoint point : path) {
            route.add(coordinatesPhase1.getPoint(point.getX(), point.getY()));
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
    protected List<IndexedPoint> getCleanPath(List<IndexedPoint> path) {

        assert path.size() >= 2;

        List<IndexedPoint> cleanPath = new ArrayList<>();

        cleanPath.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            if (!isLineFormed(path.get(i - 1), path.get(i), path.get(i + 1))) {
                cleanPath.add(path.get(i));
            }
        }

        cleanPath.add(path.get(path.size() - 1));

        return cleanPath;
    }

    private boolean isLineFormed(IndexedPoint p1, IndexedPoint p2, IndexedPoint p3) {
        assert !p1.equals(p2) && !p2.equals(p3) && !p3.equals(p1);

        if (p1.getX() == p2.getX() && p2.getX() == p3.getX()) {
            return true;
        }

        if (p1.getY() == p2.getY() && p2.getY() == p3.getY()) {
            return true;
        }

        return false;
    }

    /**
     * from the given graph and the end-points, find the route path.
     *
     * @return
     */
    protected List<IndexedPoint> buildPath(IndexedPoint source, IndexedPoint[][] sourceCells) {
        List<IndexedPoint> path = new ArrayList<IndexedPoint>();
        path.add(source);

        IndexedPoint next = source;
        do {
            next = sourceCells[next.getX()][next.getY()];

            if (next != null) {
                path.add(next);
            }
        } while (next != null);

        return path;
    }

    abstract protected List<IndexedPoint> findRoute();
}
