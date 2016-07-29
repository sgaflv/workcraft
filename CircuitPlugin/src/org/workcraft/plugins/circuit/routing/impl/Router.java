package org.workcraft.plugins.circuit.routing.impl;

import java.util.Collection;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IntegerInterval;
import org.workcraft.plugins.circuit.routing.basic.Port;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RoutingConstants;

/**
 * For now this class computes indexed coordinates and generates routing cell
 * information.
 */
public class Router {

	private final IndexedValues xCoords = new IndexedValues();
	private final IndexedValues yCoords = new IndexedValues();

	private Obstacles _lastObstaclesUsed;

	private boolean isBuilt = false;

	private RoutingCells routingCells;

	public boolean setObstacles(Obstacles newObstacles) {
		if (newObstacles == null || newObstacles.equals(_lastObstaclesUsed)) {
			return false;
		}

		_lastObstaclesUsed = newObstacles;
		isBuilt = false;
		return true;
	}

	public Collection<Double> getXCoordinates() {
		buildCoordinates();

		return xCoords.getValues();
	}

	public Collection<Double> getYCoordinates() {
		buildCoordinates();

		return yCoords.getValues();
	}

	public RoutingCells getRoutingCells() {
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
		routingCells = new RoutingCells(xCoords.size(), yCoords.size());

		markVerticalPublic();
		markHorizontalPublic();

		markBusy();
	}

	private void markBusy() {
		for (Rectangle rectangle : _lastObstaclesUsed.getRectangles()) {
			IntegerInterval xInt = xCoords.getIndexedInterval(rectangle.x, rectangle.x + rectangle.width);
			IntegerInterval yInt = yCoords.getIndexedInterval(rectangle.y, rectangle.y + rectangle.height);
			routingCells.markBusy(xInt, yInt);
		}
	}

	private void markVerticalPublic() {
		int ylen = routingCells.cells[0].length;
		int x = 0;
		for (double dx : xCoords.getValues()) {
			if (xCoords.isPublic(dx)) {
				routingCells.mark(x, 0, x, ylen - 1, CellState.VERTICAL_PUBLIC);
			}
			x++;
		}
	}

	private void markHorizontalPublic() {
		int xlen = routingCells.cells.length;
		int y = 0;
		for (double dy : yCoords.getValues()) {
			if (yCoords.isPublic(dy)) {

				routingCells.mark(0, y, xlen - 1, y, CellState.HORIZONTAL_PUBLIC);
			}
			y++;
		}
	}

	private void rebuildCoordinates() {
		xCoords.clear();
		yCoords.clear();

		for (Rectangle rec : _lastObstaclesUsed.getRectangles()) {
			double minx = SnapCalculator.snapToLower(rec.x - RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);
			double maxx = SnapCalculator.snapToHigher(rec.x + rec.width + RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);
			double miny = SnapCalculator.snapToLower(rec.y - RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);
			double maxy = SnapCalculator.snapToHigher(rec.y + rec.height + RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);

			xCoords.addPublic(minx, maxx);
			yCoords.addPublic(miny, maxy);
		}

		for (Port port : _lastObstaclesUsed.getPorts()) {
			// 1. out of the edge port
			if (!port.isOnEdge) {
				xCoords.addPrivate(port.location.x);
				yCoords.addPrivate(port.location.y);
				continue;
			}

			// 2. the port is on the edge
			IndexedValues parallel = yCoords;
			double parallelCoord = port.location.y;

			if (port.direction.isVertical()) {
				parallel = xCoords;
				parallelCoord = port.location.x;
			}

			parallel.addPrivate(parallelCoord);
		}
	}

}
