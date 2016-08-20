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

        for (Line segment : lastObstaclesUsed.getSegments()) {

            double x1 = Math.min(segment.getX1(), segment.getX2());
            double x2 = Math.max(segment.getX1(), segment.getX2());
            double y1 = Math.min(segment.getY1(), segment.getY2());
            double y2 = Math.max(segment.getY1(), segment.getY2());

            IndexedInterval xInt = xCoords.getIndexedIntervalExclusive(x1 - RouterConstants.SEGMENT_MARGIN,
                    x2 + RouterConstants.SEGMENT_MARGIN);
            IndexedInterval yInt = yCoords.getIndexedIntervalExclusive(y1 - RouterConstants.SEGMENT_MARGIN,
                    y2 + RouterConstants.SEGMENT_MARGIN);

            if (segment.isVertical()) {
                blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN, y1 - RouterConstants.SEGMENT_MARGIN,
                        x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN, y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));
                routingCells.mark(xInt, yInt, CellState.VERTICAL_BLOCK);

                IndexedInterval xInclude = xCoords.getIndexedInterval(x1, x1);
                IndexedInterval yIncludeMin = yCoords.getIndexedInterval(y1 - RouterConstants.SEGMENT_MARGIN, y1);
                IndexedInterval yIncludeMax = yCoords.getIndexedInterval(y2, y2 + RouterConstants.SEGMENT_MARGIN);

                routingCells.unmark(xInclude, yIncludeMin, CellState.VERTICAL_BLOCK);
                routingCells.unmark(xInclude, yIncludeMax, CellState.VERTICAL_BLOCK);
            }

            if (segment.isHorizontal()) {

                blocked.add(new Rectangle(x1 - RouterConstants.SEGMENT_MARGIN, y1 - RouterConstants.SEGMENT_MARGIN,
                        x2 - x1 + 2 * RouterConstants.SEGMENT_MARGIN, y2 - y1 + 2 * RouterConstants.SEGMENT_MARGIN));

                routingCells.mark(xInt, yInt, CellState.HORIZONTAL_BLOCK);

                IndexedInterval yInclude = yCoords.getIndexedInterval(y1, y1);
                IndexedInterval xIncludeMin = xCoords.getIndexedInterval(x1 - RouterConstants.SEGMENT_MARGIN, x1);
                IndexedInterval xIncludeMax = xCoords.getIndexedInterval(x2, x2 + RouterConstants.SEGMENT_MARGIN);

                routingCells.unmark(xIncludeMin, yInclude, CellState.HORIZONTAL_BLOCK);
                routingCells.unmark(xIncludeMax, yInclude, CellState.HORIZONTAL_BLOCK);
            }
        }
    }

    private void markBusy() {
        for (Rectangle rectangle : lastObstaclesUsed.getRectangles()) {
            IndexedInterval xInt = xCoords.getIndexedInterval(rectangle.getX(),
                    rectangle.getX() + rectangle.getWidth());
            IndexedInterval yInt = yCoords.getIndexedInterval(rectangle.getY(),
                    rectangle.getY() + rectangle.getHeight());
            routingCells.markBusy(xInt, yInt);
        }
    }

    private void markVerticalPublic() {

        if (routingCells.cells.length == 0) {
            return;
        }

        int ylen = routingCells.cells[0].length;
        int x = 0;
        for (Coordinate dx : xCoords.getValues()) {
            if (dx.isPublic()) {
                routingCells.mark(x, 0, x, ylen - 1, CellState.VERTICAL_PUBLIC);
            }
            x++;
        }
    }

    private void markHorizontalPublic() {
        int xlen = routingCells.cells.length;
        int y = 0;
        for (Coordinate dy : yCoords.getValues()) {
            if (dy.isPublic()) {

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

        System.out.println(
                "cells: " + xCoords.size() * yCoords.size() + " (" + xCoords.size() + "x" + yCoords.size() + ")"
                        + " rectangles:" + lastObstaclesUsed.getRectangles().size() + " connections:"
                        + lastObstaclesUsed.getConnections().size());
    }

    private void registerAdditionalCoordinates() {

        for (Rectangle rec : lastObstaclesUsed.getRectangles()) {

            boolean foundHorizontal = xCoords.isIntervalOccupied(rec.getX(), rec.getX() + rec.getWidth());
            boolean foundVertical = yCoords.isIntervalOccupied(rec.getY(), rec.getY() + rec.getHeight());

            if (!foundHorizontal) {
                xCoords.addPrivate(CoordinateOrientation.ORIENT_NONE, rec.getX() + rec.getWidth() / 2);
            }

            if (!foundVertical) {
                yCoords.addPrivate(CoordinateOrientation.ORIENT_NONE, rec.getY() + rec.getHeight() / 2);
            }

        }
    }

    private void registerPorts() {
        for (RouterConnection connection : lastObstaclesUsed.getConnections()) {
            registerPort(connection.getSource());
            registerPort(connection.getDestination());
        }
    }

    private void registerPort(RouterPort port) {
        xCoords.addPrivate(port.getDirection().getHorizontalOrientation(), port.getLocation().getX());
        yCoords.addPrivate(port.getDirection().getVerticalOrientation(), port.getLocation().getY());
        return;
    }

    private void registerRectangles() {
        for (Rectangle rec : lastObstaclesUsed.getRectangles()) {
            double minx = SnapCalculator.snapToLower(rec.getX() - RouterConstants.OBSTACLE_MARGIN,
                    RouterConstants.MAJOR_SNAP);
            double maxx = SnapCalculator.snapToHigher(rec.getX() + rec.getWidth() + RouterConstants.OBSTACLE_MARGIN,
                    RouterConstants.MAJOR_SNAP);
            double miny = SnapCalculator.snapToLower(rec.getY() - RouterConstants.OBSTACLE_MARGIN,
                    RouterConstants.MAJOR_SNAP);
            double maxy = SnapCalculator.snapToHigher(rec.getY() + rec.getHeight() + RouterConstants.OBSTACLE_MARGIN,
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

        IndexedInterval indexedIntervalH = xCoords.getIndexedInterval(point.getX(), point.getX());
        IndexedInterval indexedIntervalV = yCoords.getIndexedInterval(point.getY(), point.getY());

        int x = indexedIntervalH.getFrom();
        int y = indexedIntervalV.getFrom();

        return IndexedPoint.create(x, y);
    }

}
