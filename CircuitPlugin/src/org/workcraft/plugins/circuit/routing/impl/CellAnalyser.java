package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.PortDirection;

public class CellAnalyser {
    private final RouterCells cells;

    int sizeX;
    int sizeY;

    private IndexedPoint source;
    private PortDirection exitDirection;
    private IndexedPoint destination;
    private PortDirection entranceDirection;

    public CellAnalyser(RouterCells cells) {
        this.cells = cells;

        sizeX = cells.cells.length;
        sizeY = cells.cells[0].length;
    }

    public void initRouting(IndexedPoint source, IndexedPoint destination, PortDirection exitDirection,
            PortDirection entranceDirection) {
        this.source = source;
        this.destination = destination;
        this.exitDirection = exitDirection;
        this.entranceDirection = entranceDirection;
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

        if (x + dx == source.x && y + dy == source.y) {
            if (exitDirection != null) {
                if (dx != -exitDirection.getDx() || dy != -exitDirection.getDy()) {
                    return false;
                }
            }
        }

        if (x == destination.x && y == destination.y) {
            if (entranceDirection != null) {
                if (dx != entranceDirection.getDx() || dy != entranceDirection.getDy()) {
                    return false;
                }
            }
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
            return 10.0;
        }

        return 1.0;
    }

    public double getHeuristicsCost(int x, int y) {
        return 0;
    }
}
