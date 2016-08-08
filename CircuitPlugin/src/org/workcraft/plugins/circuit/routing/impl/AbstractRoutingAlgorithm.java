package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.List;

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
            analyser.routeConnection(connection);

            routes.add(produceRoute(connection));
        }

        return routes;
    }

    abstract protected Route produceRoute(RouterConnection connection);
}
