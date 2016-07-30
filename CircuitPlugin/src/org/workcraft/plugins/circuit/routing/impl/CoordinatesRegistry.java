package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IntegerInterval;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Port;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RoutingConstants;

/**
 * Class generates coorginates
 */
public class CoordinatesRegistry {

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

	public Obstacles getObstacles() {
		return _lastObstaclesUsed;
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
		markBlocked();
	}

	public List<Rectangle> blocked = new ArrayList<Rectangle>();

	private void markBlocked() {
		blocked.clear();

		for (Line segment : _lastObstaclesUsed.getSegments()) {

			double x1 = Math.min(segment.x1, segment.x2);
			double x2 = Math.max(segment.x1, segment.x2);
			double y1 = Math.min(segment.y1, segment.y2);
			double y2 = Math.max(segment.y1, segment.y2);

			IntegerInterval xInt = xCoords.getIndexedIntervalExclusive(x1 - RoutingConstants.SEGMENT_MARGIN,
					x2 + RoutingConstants.SEGMENT_MARGIN);
			IntegerInterval yInt = yCoords.getIndexedIntervalExclusive(y1 - RoutingConstants.SEGMENT_MARGIN,
					y2 + RoutingConstants.SEGMENT_MARGIN);

			if (segment.isVertical()) {
				blocked.add(new Rectangle(x1 - RoutingConstants.SEGMENT_MARGIN, y1 - RoutingConstants.SEGMENT_MARGIN,
						x2 - x1 + 2 * RoutingConstants.SEGMENT_MARGIN, y2 - y1 + 2 * RoutingConstants.SEGMENT_MARGIN));
				routingCells.mark(xInt, yInt, CellState.VERTICAL_BLOCK);

				IntegerInterval xInclude = xCoords.getIndexedInterval(x1, x1);
				IntegerInterval yIncludeMin = yCoords.getIndexedInterval(y1 - RoutingConstants.SEGMENT_MARGIN, y1);
				IntegerInterval yIncludeMax = yCoords.getIndexedInterval(y2, y2 + RoutingConstants.SEGMENT_MARGIN);

				routingCells.unmark(xInclude, yIncludeMin, CellState.VERTICAL_BLOCK);
				routingCells.unmark(xInclude, yIncludeMax, CellState.VERTICAL_BLOCK);
			}

			if (segment.isHorizontal()) {

				blocked.add(new Rectangle(x1 - RoutingConstants.SEGMENT_MARGIN, y1 - RoutingConstants.SEGMENT_MARGIN,
						x2 - x1 + 2 * RoutingConstants.SEGMENT_MARGIN, y2 - y1 + 2 * RoutingConstants.SEGMENT_MARGIN));

				routingCells.mark(xInt, yInt, CellState.HORIZONTAL_BLOCK);

				IntegerInterval yInclude = yCoords.getIndexedInterval(y1, y1);
				IntegerInterval xIncludeMin = xCoords.getIndexedInterval(x1 - RoutingConstants.SEGMENT_MARGIN, x1);
				IntegerInterval xIncludeMax = xCoords.getIndexedInterval(x2, x2 + RoutingConstants.SEGMENT_MARGIN);

				routingCells.unmark(xIncludeMin, yInclude, CellState.HORIZONTAL_BLOCK);
				routingCells.unmark(xIncludeMax, yInclude, CellState.HORIZONTAL_BLOCK);
			}
		}
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

		registerRectangles();
		registerPorts();

		registerAdditionalCoordinates();
	}

	private void registerAdditionalCoordinates() {

		for (Rectangle rec : _lastObstaclesUsed.getRectangles()) {

			xCoords.addPrivate(rec.x + rec.width / 2);

			yCoords.addPrivate(rec.y + rec.height / 2);
		}
	}

	private void registerPorts() {
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

	private void registerRectangles() {
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
	}

}
