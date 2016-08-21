package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * Class represents router coordinates.
 */
public class CoordinatesRegistry {

    // TODO: remove this variable
    public final List<Rectangle> blocked = new ArrayList<Rectangle>();

    private final IndexedCoordinates xCoords = new IndexedCoordinates();
    private final IndexedCoordinates yCoords = new IndexedCoordinates();

    public IndexedCoordinates getXCoords() {
        return xCoords;
    }

    public IndexedCoordinates getYCoords() {
        return yCoords;
    }

    public Collection<Coordinate> getXCoordinates() {

        return xCoords.getValues();
    }

    public Collection<Coordinate> getYCoordinates() {

        return yCoords.getValues();
    }

    public void clear() {
        blocked.clear();
        xCoords.clear();
        yCoords.clear();
    }

    public void registerPort(RouterPort port) {
        xCoords.addPrivate(port.getDirection().getHorizontalOrientation(), port.getLocation().getX());
        yCoords.addPrivate(port.getDirection().getVerticalOrientation(), port.getLocation().getY());
    }

    public Point getPoint(int x, int y) {
        assert x >= 0 && y >= 0 && x < xCoords.size() && y < yCoords.size();

        return new Point(xCoords.getValueByIndex(x), yCoords.getValueByIndex(y));
    }

    public IndexedPoint getIndexedCoordinate(Point point) {

        IndexedInterval indexedIntervalH = xCoords.getIndexedInterval(point.getX(), point.getX());
        IndexedInterval indexedIntervalV = yCoords.getIndexedInterval(point.getY(), point.getY());

        if (indexedIntervalH == null || indexedIntervalV == null) {
            return null;
        }

        int x = indexedIntervalH.getFrom();
        int y = indexedIntervalV.getFrom();

        return IndexedPoint.create(x, y);
    }

}
