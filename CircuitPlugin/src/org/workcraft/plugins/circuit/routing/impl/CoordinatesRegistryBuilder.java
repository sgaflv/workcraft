package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;

/**
 * Service object building {@link CoordinatesRegistry} for phase1 and for phase2
 * router.
 */
public class CoordinatesRegistryBuilder {

    public CoordinatesRegistry buildPhase2Coordinates(RouterTask routerTask,
            CoordinatesRegistry otherRegistry, UsageCounter usageCounter) {

        CoordinatesRegistry baseRegistry = new CoordinatesRegistry();

        for (int x = 0; x < usageCounter.getWidth(); x++) {

            int xUsage = usageCounter.getXCoordUsage(x);

            for (int i = 0; i < xUsage; i++) {

                Coordinate xCoord = otherRegistry.getXCoords().getCoordinateByIndex(x);
                baseRegistry.getXCoords().addCoordinate(xCoord);
            }
        }

        for (int y = 0; y < usageCounter.getHeight(); y++) {
            if (usageCounter.getYCoordUsage(y) > 0) {
                Coordinate yCoord = otherRegistry.getYCoords().getCoordinateByIndex(y);
                baseRegistry.getYCoords().addCoordinate(yCoord);
            }
        }

        registerPorts(baseRegistry, routerTask);

        registerAdditionalCoordinates(baseRegistry, routerTask);

        System.out.println("cells: " + baseRegistry.getXCoords().size() * baseRegistry.getYCoords().size() + " ("
                + baseRegistry.getXCoords().size() + "x" + baseRegistry.getYCoords().size() + ")" + " rectangles:"
                + routerTask.getRectangles().size() + " connections:" + routerTask.getConnections().size());

        return baseRegistry;
    }

    public CoordinatesRegistry buildPhase1Coordinates(RouterTask routerTask) {

        CoordinatesRegistry baseRegistry = new CoordinatesRegistry();

        rebuildCoordinates(baseRegistry, routerTask);

        System.out.println("cells: " + baseRegistry.getXCoords().size() * baseRegistry.getYCoords().size() + " ("
                + baseRegistry.getXCoords().size() + "x" + baseRegistry.getYCoords().size() + ")" + " rectangles:"
                + routerTask.getRectangles().size() + " connections:" + routerTask.getConnections().size());

        return baseRegistry;
    }

    private void rebuildCoordinates(CoordinatesRegistry baseRegistry, RouterTask routerTask) {

        registerRectangles(baseRegistry, routerTask);

        registerBoundaries(baseRegistry, routerTask);

        registerPorts(baseRegistry, routerTask);


        registerAdditionalCoordinates(baseRegistry, routerTask);
    }

    private void registerBoundaries(CoordinatesRegistry baseRegistry, RouterTask routerTask) {

        Rectangle first = routerTask.getRectangles().iterator().next();

        double minx = first.getX();
        double maxx = first.getX() + first.getWidth();
        double miny = first.getY();
        double maxy = first.getY() + first.getHeight();

        for (Rectangle rec : routerTask.getRectangles()) {
            minx = Math.min(minx, rec.getX());
            miny = Math.min(miny, rec.getY());
            maxx = Math.max(maxx, rec.getX() + rec.getWidth());
            maxy = Math.max(maxy, rec.getY() + rec.getHeight());
        }

        for (RouterPort port : routerTask.getPorts()) {
            minx = Math.min(minx, port.getLocation().getX());
            miny = Math.min(miny, port.getLocation().getY());
            maxx = Math.max(maxx, port.getLocation().getX());
            maxy = Math.max(maxy, port.getLocation().getY());
        }

        registerSnappedRectangle(baseRegistry, new Rectangle(minx, miny, maxx - minx, maxy - miny));


    }

    private void registerAdditionalCoordinates(CoordinatesRegistry baseRegistry, RouterTask routerTask) {

        for (Rectangle rec : routerTask.getRectangles()) {

            boolean foundHorizontal = baseRegistry.getXCoords().isIntervalOccupied(rec.getX(),
                    rec.getX() + rec.getWidth());
            boolean foundVertical = baseRegistry.getYCoords().isIntervalOccupied(rec.getY(),
                    rec.getY() + rec.getHeight());

            if (!foundHorizontal) {
                baseRegistry.getXCoords().addPrivate(CoordinateOrientation.ORIENT_NONE,
                        rec.getX() + rec.getWidth() / 2);
            }

            if (!foundVertical) {
                baseRegistry.getYCoords().addPrivate(CoordinateOrientation.ORIENT_NONE,
                        rec.getY() + rec.getHeight() / 2);
            }

        }
    }

    private void registerPorts(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        for (RouterPort port : routerTask.getPorts()) {
            baseRegistry.registerPort(port);
        }
    }

    private void registerRectangles(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        for (Rectangle rec : routerTask.getRectangles()) {
            registerSnappedRectangle(baseRegistry, rec);
        }
    }

    private void registerSnappedRectangle(CoordinatesRegistry baseRegistry, Rectangle rec) {
        double minx = SnapCalculator.snapToLower(rec.getX() - RouterConstants.OBSTACLE_MARGIN,
                RouterConstants.MAJOR_SNAP);
        double maxx = SnapCalculator.snapToHigher(rec.getX() + rec.getWidth() + RouterConstants.OBSTACLE_MARGIN,
                RouterConstants.MAJOR_SNAP);
        double miny = SnapCalculator.snapToLower(rec.getY() - RouterConstants.OBSTACLE_MARGIN,
                RouterConstants.MAJOR_SNAP);
        double maxy = SnapCalculator.snapToHigher(rec.getY() + rec.getHeight() + RouterConstants.OBSTACLE_MARGIN,
                RouterConstants.MAJOR_SNAP);

        baseRegistry.getXCoords().addPublic(CoordinateOrientation.ORIENT_LOWER, minx);
        baseRegistry.getXCoords().addPublic(CoordinateOrientation.ORIENT_HIGHER, maxx);
        baseRegistry.getYCoords().addPublic(CoordinateOrientation.ORIENT_LOWER, miny);
        baseRegistry.getYCoords().addPublic(CoordinateOrientation.ORIENT_HIGHER, maxy);
    }

}
