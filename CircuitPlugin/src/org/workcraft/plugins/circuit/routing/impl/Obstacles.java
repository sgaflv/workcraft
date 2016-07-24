package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RoutingConstants;

/**
 * The class representing the list of obstacles used in routing. It also
 * generates and represents the indexed field of routing coordinates.
 */
public class Obstacles {
	private final IndexedValues xCoords = new IndexedValues();
	private final IndexedValues yCoords = new IndexedValues();

	private final List<Rectangle> rectangles = new ArrayList<Rectangle>();
	private final List<Line> horizontals = new ArrayList<>();
	private final List<Line> verticals = new ArrayList<>();

	private boolean isBuilt = false;

	public Collection<Double> getXCoordinates() {
		buildCoordinates();

		return xCoords.getValues();
	}

	public Collection<Double> getYCoordinates() {
		buildCoordinates();

		return yCoords.getValues();
	}

	public void clear() {
		rectangles.clear();
		verticals.clear();
		horizontals.clear();
		isBuilt = false;
	}

	public void addRectangle(Rectangle rec) {
		rectangles.add(rec);

		isBuilt = false;
	}

	public void addSegment(Line line) {
		if (line.isVertical()) {
			verticals.add(line);
			isBuilt = false;
		}

		if (line.isHorizontal()) {
			horizontals.add(line);
			isBuilt = false;
		}
	}

	public void buildCoordinates() {

		if (isBuilt) {
			return;
		}

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

		isBuilt = true;
	}

}
