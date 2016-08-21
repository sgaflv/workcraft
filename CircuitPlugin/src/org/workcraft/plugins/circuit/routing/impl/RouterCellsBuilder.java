package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

public class RouterCellsBuilder {

    public RouterCells buildCells(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        RouterCells routingCells = new RouterCells(baseRegistry.getXCoords().size(), baseRegistry.getYCoords().size());

        markVerticalPublic(baseRegistry, routingCells);
        markHorizontalPublic(baseRegistry, routingCells);

        markBusy(baseRegistry, routingCells, routerTask);
        markBlocked(baseRegistry, routingCells, routerTask);

        return routingCells;
    }

    private void markBlocked(CoordinatesRegistry baseRegistry, RouterCells routingCells, RouterTask routerTask) {

        for (Line segment : routerTask.getSegments()) {

            double x1 = Math.min(segment.getX1(), segment.getX2());
            double x2 = Math.max(segment.getX1(), segment.getX2());
            double y1 = Math.min(segment.getY1(), segment.getY2());
            double y2 = Math.max(segment.getY1(), segment.getY2());

            IndexedInterval xInt = baseRegistry.getXCoords().getIndexedIntervalExclusive(
                    x1 - RouterConstants.SEGMENT_MARGIN, x2 + RouterConstants.SEGMENT_MARGIN);
            IndexedInterval yInt = baseRegistry.getYCoords().getIndexedIntervalExclusive(
                    y1 - RouterConstants.SEGMENT_MARGIN, y2 + RouterConstants.SEGMENT_MARGIN);

            if (segment.isVertical()) {
                baseRegistry.blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN,
                        y1 - RouterConstants.SEGMENT_MARGIN, x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN,
                        y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));

                routingCells.mark(xInt, yInt, CellState.VERTICAL_BLOCK);

                IndexedInterval xInclude = baseRegistry.getXCoords().getIndexedInterval(x1, x1);
                IndexedInterval yIncludeMin = baseRegistry.getYCoords()
                        .getIndexedInterval(y1 - RouterConstants.SEGMENT_MARGIN, y1);
                IndexedInterval yIncludeMax = baseRegistry.getYCoords().getIndexedInterval(y2,
                        y2 + RouterConstants.SEGMENT_MARGIN);

                routingCells.unmark(xInclude, yIncludeMin, CellState.VERTICAL_BLOCK);
                routingCells.unmark(xInclude, yIncludeMax, CellState.VERTICAL_BLOCK);
            }

            if (segment.isHorizontal()) {

                baseRegistry.blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN,
                        y1 - RouterConstants.SEGMENT_MARGIN, x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN,
                        y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));

                routingCells.mark(xInt, yInt, CellState.HORIZONTAL_BLOCK);

                IndexedInterval yInclude = baseRegistry.getYCoords().getIndexedInterval(y1, y1);
                IndexedInterval xIncludeMin = baseRegistry.getXCoords()
                        .getIndexedInterval(x1 - RouterConstants.SEGMENT_MARGIN, x1);
                IndexedInterval xIncludeMax = baseRegistry.getXCoords().getIndexedInterval(x2,
                        x2 + RouterConstants.SEGMENT_MARGIN);

                routingCells.unmark(xIncludeMin, yInclude, CellState.HORIZONTAL_BLOCK);
                routingCells.unmark(xIncludeMax, yInclude, CellState.HORIZONTAL_BLOCK);
            }
        }
    }

    private void markBusy(CoordinatesRegistry baseRegistry, RouterCells routingCells, RouterTask routerTask) {
        for (Rectangle rectangle : routerTask.getRectangles()) {
            IndexedInterval xInt = baseRegistry.getXCoords().getIndexedInterval(rectangle.getX(),
                    rectangle.getX() + rectangle.getWidth());
            IndexedInterval yInt = baseRegistry.getYCoords().getIndexedInterval(rectangle.getY(),
                    rectangle.getY() + rectangle.getHeight());
            routingCells.markBusy(xInt, yInt);
        }
    }

    private void markVerticalPublic(CoordinatesRegistry baseRegistry, RouterCells routingCells) {

        if (routingCells.cells.length == 0) {
            return;
        }

        int ylen = routingCells.cells[0].length;
        int x = 0;
        for (Coordinate dx : baseRegistry.getXCoords().getValues()) {
            if (dx.isPublic()) {
                routingCells.mark(x, 0, x, ylen - 1, CellState.VERTICAL_PUBLIC);
            }
            x++;
        }
    }

    private void markHorizontalPublic(CoordinatesRegistry baseRegistry, RouterCells routingCells) {
        int xlen = routingCells.cells.length;
        int y = 0;
        for (Coordinate dy : baseRegistry.getYCoords().getValues()) {
            if (dy.isPublic()) {
                routingCells.mark(0, y, xlen - 1, y, CellState.HORIZONTAL_PUBLIC);
            }
            y++;
        }
    }
}
