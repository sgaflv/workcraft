package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;

public class CellAnalyser {
    private final RouterCells cells;
    private final CoordinatesRegistry coordinates;

    int sizeX;
    int sizeY;

    private IndexedPoint source;
    private IndexedPoint destination;

    public CellAnalyser(RouterCells cells, CoordinatesRegistry coordinates) {
        this.cells = cells;
        this.coordinates = coordinates;

        sizeX = coordinates.getXCoordinates().size();
        sizeY = coordinates.getYCoordinates().size();
    }

    public void initRouting(IndexedPoint source, IndexedPoint destination) {
        this.source = source;
        this.destination = destination;
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

        if (dx != 0) {
            if (isBlockedHorizontally(x, y)) {
                return false;
            }
        }

        if (dy != 0) {
            if (isBlockedVertically(x, y)) {
                return false;
            }
        }

        return true;
    }

    private boolean isBlockedHorizontally(int x, int y) {
        final boolean isBlocked = cells.isMarked(x, y, CellState.HORIZONTAL_BLOCK);
        final boolean isPrivate = y != destination.y && y != source.y
                && !cells.isMarked(x, y, CellState.HORIZONTAL_PUBLIC);
        return isBlocked || isPrivate;
    }

    private boolean isBlockedVertically(int x, int y) {
        final boolean isBlocked = cells.isMarked(x, y, CellState.VERTICAL_BLOCK);
        final boolean isPrivate = x != destination.x && x != source.x
                && !cells.isMarked(x, y, CellState.VERTICAL_PUBLIC);
        return isBlocked || isPrivate;
    }

    public Double getMovementCost(int lastX, int lastY, int x, int y, int dx, int dy) {
        if (!isMovementPossible(x, y, dx, dy)) {
            return null;
        }

        if (!cells.isMarked(x, y, CellState.BUSY) && cells.isMarked(x + dx, y + dy, CellState.BUSY)) {
            return 1000.0;
        }

        final boolean hasTurned = (x - lastX) != dx || (y - lastY) != dy;

        if (hasTurned) {
            // return 10.0;
        }

        return 1.0;
    }

    public double getHeuristicsCost(int x, int y) {
        return 0;
    }
}
