package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;

/**
 * Main router class.
 */
public class Router {

    private final CoordinatesRegistry coordinates = new CoordinatesRegistry();
    private final AbstractRoutingAlgorithm algorithm = new DijkstraRouter();

    private List<Route> routesFound;

    public void setObstacles(RouterTask newObstacles) {
        if (coordinates.setObstacles(newObstacles)) {
            routeConnections();
        }
    }

    public RouterTask getObstacles() {
        return coordinates.getRouterTask();
    }

    public CoordinatesRegistry getCoordinatesRegistry() {
        return coordinates;
    }

    public void routeConnections() {

        final long start = System.currentTimeMillis();

        // 1st phase
        routesFound = algorithm.route(coordinates.getRouterTask(), coordinates.getRouterCells(), coordinates);

        final long stop = System.currentTimeMillis();

        System.out.println("solved ms:" + (stop - start));

    }

    public List<Route> getRoutingResult() {
        return routesFound;
    }
}
