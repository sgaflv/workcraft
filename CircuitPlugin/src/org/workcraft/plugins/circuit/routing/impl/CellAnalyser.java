package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.RouterConnection;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

public class CellAnalyser {
    private final RouterCells cells;
    private final CoordinatesRegistry coordinates;

    int sizeX;
    int sizeY;

    private RouterPort source;
    private RouterPort destination;

    public CellAnalyser(RouterCells cells, CoordinatesRegistry coordinates) {
        this.cells = cells;
        this.coordinates = coordinates;

        sizeX = coordinates.getXCoordinates().size();
        sizeY = coordinates.getYCoordinates().size();
    }

    public void routeConnection(RouterConnection connection) {
        source = connection.source;
        destination = connection.destination;
    }

    public boolean isMovementPossible(int x, int y, int dx, int dy) {
        assert source != null && destination != null : "source and destination must be known";
        assert dx != 0 || dy != 0 : "Has to move in x or y direction";
        assert dx == 0 || dy == 0 : "Diagonal movement is not allowed";
        assert Math.abs(dx) < 2 && Math.abs(dy) < 2 : "The movement length over more than 1 cell is not allowed";

        final int targetX = x + dx;
        final int targetY = y + dy;

        final boolean isOutsideBoundaries = targetX < 0 || targetX >= sizeX || targetY < 0 || targetY >= sizeY;

        if (isOutsideBoundaries) {
            return false;
        }

        return false;
    }

    public double getMovementCost(int x, int y, int dx, int dy) {
        return 0;
    }

    public double getHeuristicsCost(int x, int y) {
        return 0;
    }
}
