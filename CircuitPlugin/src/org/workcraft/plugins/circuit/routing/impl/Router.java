package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;

/**
 * Main router class.
 */
public class Router {

    private final CoordinatesRegistry coordinatesPhase1 = new CoordinatesRegistry();

    private final CoordinatesRegistry coordinatesPhase2 = new CoordinatesRegistry();

    private final AbstractRoutingAlgorithm algorithm = new DijkstraRouter();

    private RouterTask routerTask;

    private List<Route> routesFound;

    public void setObstacles(RouterTask routerTask) {

        if (routerTask == null || routerTask.equals(this.routerTask)) {
            return;
        }

        this.routerTask = routerTask;

        coordinatesPhase1.setRouterTask(routerTask);
        routeConnections();
    }

    public RouterTask getObstacles() {
        return coordinatesPhase1.getRouterTask();
    }

    public CoordinatesRegistry getCoordinatesRegistry() {
        return coordinatesPhase2;
    }

    public void routeConnections() {

        long start = System.currentTimeMillis();

        // 1st phase
        routesFound = algorithm.route(coordinatesPhase1.getRouterTask(), coordinatesPhase1.getRouterCells(), coordinatesPhase1);

        long stop = System.currentTimeMillis();

        // 2nd phase
        UsageCounter usageCounter = algorithm.getUsageCounter();
        coordinatesPhase2.buildFromUsageCounter(coordinatesPhase1.getRouterTask(), coordinatesPhase1, usageCounter);

        System.out.println("solved ms:" + (stop - start));

    }

    public List<Route> getRoutingResult() {
        return routesFound;
    }
}
