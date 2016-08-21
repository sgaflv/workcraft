package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * Service class producing {@link RouterCells} from given
 * {@link CoordinatesRegistry} and {@link RouterTask}.
 */
public class RouterCellsBuilder {

    public RouterCells buildRouterCells(CoordinatesRegistry coordinatesRegistry, RouterTask routerTask) {
        RouterCells routerCells = new RouterCells(coordinatesRegistry.getXCoords().size(),
                coordinatesRegistry.getYCoords().size());

        markVerticalPublic(coordinatesRegistry, routerCells);
        markHorizontalPublic(coordinatesRegistry, routerCells);

        markBusy(coordinatesRegistry, routerCells, routerTask);
        markBlocked(coordinatesRegistry, routerCells, routerTask);

        return routerCells;
    }

    private void markBlocked(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells, RouterTask routerTask) {

        for (RouterPort port : routerTask.getPorts()) {
            IndexedPoint ip = coordinatesRegistry.getIndexedCoordinate(port.getLocation());
            if (ip != null) {
                if (port.getDirection().isVertical()) {
                    routerCells.mark(ip.getX(), ip.getY(), ip.getX(), ip.getY(), CellState.HORIZONTAL_BLOCK);
                } else {
                    routerCells.mark(ip.getX(), ip.getY(), ip.getX(), ip.getY(), CellState.VERTICAL_BLOCK);
                }
            }
        }

        for (Line segment : routerTask.getSegments()) {

            double x1 = Math.min(segment.getX1(), segment.getX2());
            double x2 = Math.max(segment.getX1(), segment.getX2());
            double y1 = Math.min(segment.getY1(), segment.getY2());
            double y2 = Math.max(segment.getY1(), segment.getY2());

            IndexedInterval xInt = coordinatesRegistry.getXCoords().getIndexedIntervalExclusive(
                    x1 - RouterConstants.SEGMENT_MARGIN, x2 + RouterConstants.SEGMENT_MARGIN);
            IndexedInterval yInt = coordinatesRegistry.getYCoords().getIndexedIntervalExclusive(
                    y1 - RouterConstants.SEGMENT_MARGIN, y2 + RouterConstants.SEGMENT_MARGIN);

            if (segment.isVertical()) {
                coordinatesRegistry.blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN,
                        y1 - RouterConstants.SEGMENT_MARGIN, x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN,
                        y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));

                routerCells.mark(xInt, yInt, CellState.VERTICAL_BLOCK);

                IndexedInterval xInclude = coordinatesRegistry.getXCoords().getIndexedInterval(x1, x1);
                IndexedInterval yIncludeMin = coordinatesRegistry.getYCoords()
                        .getIndexedInterval(y1 - RouterConstants.SEGMENT_MARGIN, y1);
                IndexedInterval yIncludeMax = coordinatesRegistry.getYCoords().getIndexedInterval(y2,
                        y2 + RouterConstants.SEGMENT_MARGIN);

                routerCells.unmark(xInclude, yIncludeMin, CellState.VERTICAL_BLOCK);
                routerCells.unmark(xInclude, yIncludeMax, CellState.VERTICAL_BLOCK);
            }

            if (segment.isHorizontal()) {

                coordinatesRegistry.blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN,
                        y1 - RouterConstants.SEGMENT_MARGIN, x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN,
                        y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));

                routerCells.mark(xInt, yInt, CellState.HORIZONTAL_BLOCK);

                IndexedInterval yInclude = coordinatesRegistry.getYCoords().getIndexedInterval(y1, y1);
                IndexedInterval xIncludeMin = coordinatesRegistry.getXCoords()
                        .getIndexedInterval(x1 - RouterConstants.SEGMENT_MARGIN, x1);
                IndexedInterval xIncludeMax = coordinatesRegistry.getXCoords().getIndexedInterval(x2,
                        x2 + RouterConstants.SEGMENT_MARGIN);

                routerCells.unmark(xIncludeMin, yInclude, CellState.HORIZONTAL_BLOCK);
                routerCells.unmark(xIncludeMax, yInclude, CellState.HORIZONTAL_BLOCK);
            }
        }
    }

    private void markBusy(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells, RouterTask routerTask) {
        for (Rectangle rectangle : routerTask.getRectangles()) {
            IndexedInterval xInt = coordinatesRegistry.getXCoords().getIndexedInterval(rectangle.getX(),
                    rectangle.getX() + rectangle.getWidth());
            IndexedInterval yInt = coordinatesRegistry.getYCoords().getIndexedInterval(rectangle.getY(),
                    rectangle.getY() + rectangle.getHeight());
            routerCells.markBusy(xInt, yInt);
        }
    }

    private void markVerticalPublic(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells) {

        if (routerCells.cells.length == 0) {
            return;
        }

        int ylen = routerCells.cells[0].length;
        int x = 0;
        for (Coordinate dx : coordinatesRegistry.getXCoords().getValues()) {
            if (dx.isPublic()) {
                routerCells.mark(x, 0, x, ylen - 1, CellState.VERTICAL_PUBLIC);
            }
            x++;
        }
    }

    private void markHorizontalPublic(CoordinatesRegistry coordinatesRegistry, RouterCells routerCells) {
        int xlen = routerCells.cells.length;
        int y = 0;
        for (Coordinate dy : coordinatesRegistry.getYCoords().getValues()) {
            if (dy.isPublic()) {
                routerCells.mark(0, y, xlen - 1, y, CellState.HORIZONTAL_PUBLIC);
            }
            y++;
        }
    }
}
