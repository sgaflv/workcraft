package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;

/**
 * Main router class.
 */
public class Router {

    private final CoordinatesRegistryBuilder registryBuilder = new CoordinatesRegistryBuilder();
    private final RouterCellsBuilder cellsBuilder = new RouterCellsBuilder();

    private CoordinatesRegistry coordinatesPhase1;

    private CoordinatesRegistry coordinatesPhase2;

    private RouterCells routerCells;

    private final AbstractRoutingAlgorithm algorithm = new DijkstraRouter();

    private RouterTask routerTask;

    private List<Route> routesFound;

    public void setRouterTask(RouterTask routerTask) {

        if (routerTask == null || routerTask.equals(this.routerTask)) {
            return;
        }

        this.routerTask = routerTask;

        coordinatesPhase1 = registryBuilder.buildPhase1Coordinates(routerTask);

        routerCells = cellsBuilder.buildRouterCells(coordinatesPhase1, routerTask);

        routeConnections();
    }

    public RouterTask getObstacles() {
        return routerTask;
    }

    public CoordinatesRegistry getCoordinatesRegistry() {

        return coordinatesPhase1;
    }

    public void routeConnections() {

        long start = System.currentTimeMillis();

        // 1st phase
        routesFound = algorithm.route(routerTask, routerCells, coordinatesPhase1);

        long stop = System.currentTimeMillis();

        // 2nd phase
        UsageCounter usageCounter = algorithm.getUsageCounter();

        coordinatesPhase2 = registryBuilder.buildPhase2Coordinates(routerTask, coordinatesPhase1, usageCounter);

        System.out.println("solved ms:" + (stop - start));

    }

    public List<Route> getRoutingResult() {
        return routesFound;
    }

    public RouterCells getRouterCells() {
        return routerCells;
    }
}
