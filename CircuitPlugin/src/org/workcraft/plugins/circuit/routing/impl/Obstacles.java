package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.workcraft.plugins.circuit.routing.basic.Connection;
import org.workcraft.plugins.circuit.routing.basic.IntegerInterval;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Port;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RoutingConstants;

/**
 * The class representing the list of obstacles used in routing.
 */
public class Obstacles {
	private final IndexedValues xCoords = new IndexedValues();
	private final IndexedValues yCoords = new IndexedValues();

	private final List<Rectangle> rectangles = new ArrayList<Rectangle>();
	private final List<Line> hSegments = new ArrayList<>();
	private final List<Line> vSegments = new ArrayList<>();
	private final Set<Port> ports = new HashSet<>();
	private final Set<Connection> connections = new HashSet<>();

	private boolean isBuilt = false;

	private RoutingCells routingCells;

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
		rectangles.clear();
		vSegments.clear();
		hSegments.clear();
		ports.clear();
		isBuilt = false;
	}

	public void addRectangle(Rectangle rec) {
		rectangles.add(rec);

		isBuilt = false;
	}

	public void addSegment(Line line) {
		if (line.isVertical()) {
			vSegments.add(line);
			isBuilt = false;
		}

		if (line.isHorizontal()) {
			hSegments.add(line);
			isBuilt = false;
		}
	}

	public void addPort(Port port) {
		if (ports.add(port)) {
			isBuilt = false;
		}
	}

	public void addConnection(Connection connection) {
		addPort(connection.source);
		addPort(connection.destination);

		if (connections.add(connection)) {
			isBuilt = false;
		}
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

		for (Rectangle rectangle : rectangles) {
			IntegerInterval xInt = xCoords.getIndexedInterval(rectangle.x, rectangle.x + rectangle.width);
			IntegerInterval yInt = yCoords.getIndexedInterval(rectangle.y, rectangle.y + rectangle.height);
			routingCells.markBusy(xInt, yInt);
		}
	}

	private void rebuildCoordinates() {
		xCoords.clear();
		yCoords.clear();

		for (Rectangle rec : rectangles) {
			double minx = SnapCalculator.snapToLower(rec.x - RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);
			double maxx = SnapCalculator.snapToHigher(rec.x + rec.width + RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);
			double miny = SnapCalculator.snapToLower(rec.y - RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);
			double maxy = SnapCalculator.snapToHigher(rec.y + rec.height + RoutingConstants.OBSTACLE_MARGIN,
					RoutingConstants.MAJOR_SNAP);

			xCoords.add(minx, maxx);
			yCoords.add(miny, maxy);
		}

		for (Port port : ports) {
			// 1. out of the edge port
			if (!port.isOnEdge) {
				xCoords.add(port.location.x);
				yCoords.add(port.location.y);
				continue;
			}

			// 2. the port is on the edge
			IndexedValues parallel = yCoords;
			IndexedValues perpendecular = xCoords;
			double parallelCoord = port.location.y;
			double perpendecularCoord = port.location.x;
			if (port.direction.isVertical()) {
				parallel = xCoords;
				perpendecular = yCoords;
				parallelCoord = port.location.x;
				perpendecularCoord = port.location.y;
			}

			parallel.add(parallelCoord);

			double snappedCoordinate = perpendecularCoord;

			switch (port.direction) {
			case EAST:
				snappedCoordinate = SnapCalculator.snapToHigher(perpendecularCoord + RoutingConstants.OBSTACLE_MARGIN,
						RoutingConstants.MAJOR_SNAP);
				break;
			case WEST:
				snappedCoordinate = SnapCalculator.snapToLower(perpendecularCoord - RoutingConstants.OBSTACLE_MARGIN,
						RoutingConstants.MAJOR_SNAP);
				break;
			case NORTH:
				snappedCoordinate = SnapCalculator.snapToLower(perpendecularCoord - RoutingConstants.OBSTACLE_MARGIN,
						RoutingConstants.MAJOR_SNAP);
				break;
			case SOUTH:
				snappedCoordinate = SnapCalculator.snapToHigher(perpendecularCoord + RoutingConstants.OBSTACLE_MARGIN,
						RoutingConstants.MAJOR_SNAP);
				break;
			}

			perpendecular.add(snappedCoordinate);

		}
	}

}
