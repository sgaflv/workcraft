package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

/**
 * Service object building {@link CoordinatesRegistry} for phase1 and phase2
 * router.
 */
public class CoordinatesRegistryBuilder {

    public CoordinatesRegistry buildFromUsageCounter(RouterTask routerTask,
            CoordinatesRegistry otherRegistry, UsageCounter usageCounter) {

        CoordinatesRegistry baseRegistry = new CoordinatesRegistry();

        for (int x = 0; x < usageCounter.getWidth(); x++) {
            if (usageCounter.getXCoordUsage(x) > 0) {
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

    public CoordinatesRegistry buildCoordinates(RouterTask routerTask) {

        CoordinatesRegistry baseRegistry = new CoordinatesRegistry();

        rebuildCoordinates(baseRegistry, routerTask);

        System.out.println("cells: " + baseRegistry.getXCoords().size() * baseRegistry.getYCoords().size() + " ("
                + baseRegistry.getXCoords().size() + "x" + baseRegistry.getYCoords().size() + ")" + " rectangles:"
                + routerTask.getRectangles().size() + " connections:" + routerTask.getConnections().size());

        return baseRegistry;
    }

    private void rebuildCoordinates(CoordinatesRegistry baseRegistry, RouterTask routerTask) {

        registerRectangles(baseRegistry, routerTask);
        registerPorts(baseRegistry, routerTask);

        registerAdditionalCoordinates(baseRegistry, routerTask);
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
        for (RouterConnection connection : routerTask.getConnections()) {
            baseRegistry.registerPort(connection.getSource());
            baseRegistry.registerPort(connection.getDestination());
        }
    }

    private void registerRectangles(CoordinatesRegistry baseRegistry, RouterTask routerTask) {
        for (Rectangle rec : routerTask.getRectangles()) {
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

        baseRegistry.getXCoords().mergeCoordinates();
        baseRegistry.getYCoords().mergeCoordinates();
    }

}
