package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * Class represents router coordinates.
 */
public class CoordinatesRegistry {

    private final IndexedCoordinates xCoords = new IndexedCoordinates();
    private final IndexedCoordinates yCoords = new IndexedCoordinates();

    private RouterTask lastObstaclesUsed;

    private boolean isBuilt = false;

    private RouterCells routingCells;

    public boolean setObstacles(RouterTask newObstacles) {
        if (newObstacles == null || newObstacles.equals(lastObstaclesUsed)) {
            return false;
        }

        lastObstaclesUsed = newObstacles;
        isBuilt = false;
        return true;
    }

    public RouterTask getRouterTask() {
        return lastObstaclesUsed;
    }

    public Collection<Coordinate> getXCoordinates() {
        buildCoordinates();

        return xCoords.getValues();
    }

    public Collection<Coordinate> getYCoordinates() {
        buildCoordinates();

        return yCoords.getValues();
    }

    public RouterCells getRouterCells() {
        buildCoordinates();

        return routingCells;
    }

    public void clear() {
        isBuilt = false;
    }

    public void buildCoordinates() {

        if (isBuilt) {
            return;
        }

        rebuildCoordinates();

        markCells();

        isBuilt = true;
    }

    private void markCells() {
        routingCells = new RouterCells(xCoords.size(), yCoords.size());

        markVerticalPublic();
        markHorizontalPublic();

        markBusy();
        markBlocked();
    }

    public List<Rectangle> blocked = new ArrayList<Rectangle>();

    private void markBlocked() {
        blocked.clear();

        for (final Line segment : lastObstaclesUsed.getSegments()) {

            final double x1 = Math.min(segment.x1, segment.x2);
            final double x2 = Math.max(segment.x1, segment.x2);
            final double y1 = Math.min(segment.y1, segment.y2);
            final double y2 = Math.max(segment.y1, segment.y2);

            final IndexedInterval xInt = xCoords.getIndexedIntervalExclusive(x1 - RouterConstants.SEGMENT_MARGIN,
                    x2 + RouterConstants.SEGMENT_MARGIN);
            final IndexedInterval yInt = yCoords.getIndexedIntervalExclusive(y1 - RouterConstants.SEGMENT_MARGIN,
                    y2 + RouterConstants.SEGMENT_MARGIN);

            if (segment.isVertical()) {
                blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN, y1 - RouterConstants.SEGMENT_MARGIN,
                        x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN, y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));
                routingCells.mark(xInt, yInt, CellState.VERTICAL_BLOCK);

                final IndexedInterval xInclude = xCoords.getIndexedInterval(x1, x1);
                final IndexedInterval yIncludeMin = yCoords.getIndexedInterval(y1 - RouterConstants.SEGMENT_MARGIN, y1);
                final IndexedInterval yIncludeMax = yCoords.getIndexedInterval(y2, y2 + RouterConstants.SEGMENT_MARGIN);

                routingCells.unmark(xInclude, yIncludeMin, CellState.VERTICAL_BLOCK);
                routingCells.unmark(xInclude, yIncludeMax, CellState.VERTICAL_BLOCK);
            }

            if (segment.isHorizontal()) {

                blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN, y1 - RouterConstants.SEGMENT_MARGIN,
                        x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN, y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));

                routingCells.mark(xInt, yInt, CellState.HORIZONTAL_BLOCK);

                final IndexedInterval yInclude = yCoords.getIndexedInterval(y1, y1);
                final IndexedInterval xIncludeMin = xCoords.getIndexedInterval(x1 - RouterConstants.SEGMENT_MARGIN, x1);
                final IndexedInterval xIncludeMax = xCoords.getIndexedInterval(x2, x2 + RouterConstants.SEGMENT_MARGIN);

                routingCells.unmark(xIncludeMin, yInclude, CellState.HORIZONTAL_BLOCK);
                routingCells.unmark(xIncludeMax, yInclude, CellState.HORIZONTAL_BLOCK);
            }
        }
    }

    private void markBusy() {
        for (final Rectangle rectangle : lastObstaclesUsed.getRectangles()) {
            final IndexedInterval xInt = xCoords.getIndexedInterval(rectangle.x, rectangle.x + rectangle.width);
            final IndexedInterval yInt = yCoords.getIndexedInterval(rectangle.y, rectangle.y + rectangle.height);
            routingCells.markBusy(xInt, yInt);
        }
    }

    private void markVerticalPublic() {

        if (routingCells.cells.length == 0) {
            return;
        }

        final int ylen = routingCells.cells[0].length;
        int x = 0;
        for (final Coordinate dx : xCoords.getValues()) {
            if (dx.isPublic) {
                routingCells.mark(x, 0, x, ylen - 1, CellState.VERTICAL_PUBLIC);
            }
            x++;
        }
    }

    private void markHorizontalPublic() {
        final int xlen = routingCells.cells.length;
        int y = 0;
        for (final Coordinate dy : yCoords.getValues()) {
            if (dy.isPublic) {

                routingCells.mark(0, y, xlen - 1, y, CellState.HORIZONTAL_PUBLIC);
            }
            y++;
        }
    }

    private void rebuildCoordinates() {
        xCoords.clear();
        yCoords.clear();

        registerRectangles();
        registerPorts();

        registerAdditionalCoordinates();

        System.out.println("cells: " + xCoords.size() * yCoords.size());
    }

    private void registerAdditionalCoordinates() {

        for (final Rectangle rec : lastObstaclesUsed.getRectangles()) {

            if (!xCoords.isIntervalOccupied(rec.x, rec.x + rec.width)) {
                xCoords.addPrivate(CoordinateOrientation.ORIENT_NONE, rec.x + rec.width / 2);
            }

            if (!yCoords.isIntervalOccupied(rec.y, rec.y + rec.height)) {
                yCoords.addPrivate(CoordinateOrientation.ORIENT_NONE, rec.y + rec.height / 2);
            }

        }
    }

    private void registerPorts() {
        for (final RouterConnection connection : lastObstaclesUsed.getConnections()) {
            registerPort(connection.source);
            registerPort(connection.destination);
        }
    }

    private void registerPort(RouterPort port) {
        // 1. out of the edge port
        if (!port.isOnEdge) {
            xCoords.addPrivate(port.direction.getHorizontalOrientation(), port.location.x);
            yCoords.addPrivate(port.direction.getVerticalOrientation(), port.location.y);
            return;
        }

        // 2. the port is on the edge
        IndexedCoordinates parallel = yCoords;
        double parallelCoord = port.location.y;

        if (port.direction.isVertical()) {
            parallel = xCoords;
            parallelCoord = port.location.x;
        }

        parallel.addPrivate(CoordinateOrientation.ORIENT_BOTH, parallelCoord);
    }

    private void registerRectangles() {
        for (final Rectangle rec : lastObstaclesUsed.getRectangles()) {
            final double minx = SnapCalculator.snapToLower(rec.x - RouterConstants.OBSTACLE_MARGIN,
                    RouterConstants.MAJOR_SNAP);
            final double maxx = SnapCalculator.snapToHigher(rec.x + rec.width + RouterConstants.OBSTACLE_MARGIN,
                    RouterConstants.MAJOR_SNAP);
            final double miny = SnapCalculator.snapToLower(rec.y - RouterConstants.OBSTACLE_MARGIN,
                    RouterConstants.MAJOR_SNAP);
            final double maxy = SnapCalculator.snapToHigher(rec.y + rec.height + RouterConstants.OBSTACLE_MARGIN,
                    RouterConstants.MAJOR_SNAP);

            xCoords.addPublic(CoordinateOrientation.ORIENT_LOWER, minx);
            xCoords.addPublic(CoordinateOrientation.ORIENT_HIGHER, maxx);
            yCoords.addPublic(CoordinateOrientation.ORIENT_LOWER, miny);
            yCoords.addPublic(CoordinateOrientation.ORIENT_HIGHER, maxy);
        }

        xCoords.mergeCoordinates();
        yCoords.mergeCoordinates();
    }

    public Point getPoint(int x, int y) {
        assert x >= 0 && y >= 0 && x < xCoords.size() && y < yCoords.size();

        return new Point(xCoords.getValueByIndex(x), yCoords.getValueByIndex(y));
    }

    public IndexedPoint getIndexedCoordinate(Point point) {

        final IndexedInterval indexedIntervalH = xCoords.getIndexedInterval(point.x, point.x);
        final IndexedInterval indexedIntervalV = yCoords.getIndexedInterval(point.y, point.y);

        final int x = indexedIntervalH.from;
        final int y = indexedIntervalV.from;

        return new IndexedPoint(x, y);
    }

}
