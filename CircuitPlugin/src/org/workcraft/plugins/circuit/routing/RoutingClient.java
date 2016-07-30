package org.workcraft.plugins.circuit.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.graph.Viewport;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Direction;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.Port;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.impl.Obstacles;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RoutingCells;

/**
 * The class creates the routing task and
 */
public class RoutingClient {

	private Router router = new Router();

	public RoutingClient() {
	}

	public void registerObstacles(VisualCircuit circuit) {

		Obstacles newObstacles = new Obstacles();

		for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {

			Rectangle internalBoundingBox = getRectangle(component.getInternalBoundingBox());

			newObstacles.addRectangle(internalBoundingBox);

			for (VisualContact contact : component.getContacts()) {

				Point portPoint = new Point(contact.getX() + component.getX(), contact.getY() + component.getY());

				Port newPort = new Port(getDirection(contact), portPoint, false);
				newObstacles.addPort(newPort);

				Line portSegment = internalBoundingBox.getPortSegment(portPoint);
				newObstacles.addSegment(portSegment);
			}
		}

		for (VisualContact port : circuit.getVisualPorts()) {
			Rectangle2D internalBoundingBox = port.getInternalBoundingBox();
			newObstacles.addRectangle(getRectangle(internalBoundingBox));

			Port newPort = new Port(getDirection(port),
					new Point(internalBoundingBox.getCenterX(), internalBoundingBox.getCenterY()), true);

			newObstacles.addPort(newPort);
		}

		router.setObstacles(newObstacles);
	}

	private Direction getDirection(VisualContact contact) {

		org.workcraft.plugins.circuit.VisualContact.Direction direction = contact.getDirection();
		if (contact.isInput()) {
			direction = direction.flip();
		}

		switch (direction) {
		case EAST:
			return Direction.EAST;
		case WEST:
			return Direction.WEST;
		case NORTH:
			return Direction.NORTH;
		case SOUTH:
			return Direction.SOUTH;
		}

		return null;
	}

	private Rectangle getRectangle(Rectangle2D rect) {
		return new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public void draw(Graphics2D g, Viewport viewport) {

		drawCoordinates(g, viewport);
		drawBlocks(g);
		// drawSegments(g);
		drawCells(g);

	}

	private void drawCoordinates(Graphics2D g, Viewport viewport) {
		Path2D grid = new Path2D.Double();

		java.awt.Rectangle bounds = viewport.getShape();

		java.awt.Point screenTopLeft = new java.awt.Point(bounds.x, bounds.y);
		Point2D userTopLeft = viewport.screenToUser(screenTopLeft);
		java.awt.Point screenBottomRight = new java.awt.Point(bounds.x + bounds.width, bounds.y + bounds.height);
		Point2D userBottomRight = viewport.screenToUser(screenBottomRight);

		for (double x : router.getCoordinatesRegistry().getXCoordinates()) {
			grid.moveTo(x, userTopLeft.getY());
			grid.lineTo(x, userBottomRight.getY());
		}
		for (double y : router.getCoordinatesRegistry().getYCoordinates()) {
			grid.moveTo(userTopLeft.getX(), y);
			grid.lineTo(userBottomRight.getX(), y);
		}

		g.setColor(Color.GRAY.brighter());
		g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
		g.draw(grid);
	}

	private void drawCells(Graphics2D g) {
		RoutingCells rcells = router.getCoordinatesRegistry().getRoutingCells();

		int[][] cells = rcells.cells;

		int y = 0;
		for (double dy : router.getCoordinatesRegistry().getYCoordinates()) {
			int x = 0;
			for (double dx : router.getCoordinatesRegistry().getXCoordinates()) {
				boolean isBusy = (cells[x][y] & CellState.BUSY) > 0;
				boolean isVerticalPrivate = (cells[x][y] & CellState.VERTICAL_PUBLIC) == 0;
				boolean isHorizontalPrivate = (cells[x][y] & CellState.HORIZONTAL_PUBLIC) == 0;

				boolean isVerticalBlock = (cells[x][y] & CellState.VERTICAL_BLOCK) != 0;
				boolean isHorizontalBlock = (cells[x][y] & CellState.HORIZONTAL_BLOCK) != 0;

				Path2D shape = new Path2D.Double();

				if (isBusy) {
					g.setColor(Color.RED);
					shape.moveTo(dx - 0.1, dy - 0.1);
					shape.lineTo(dx + 0.1, dy + 0.1);
					shape.moveTo(dx + 0.1, dy - 0.1);
					shape.lineTo(dx - 0.1, dy + 0.1);
					g.draw(shape);
				} else {

					if (isVerticalPrivate) {
						shape = new Path2D.Double();
						g.setColor(Color.MAGENTA.darker());
						shape.moveTo(dx, dy - 0.1);
						shape.lineTo(dx, dy + 0.1);
						g.draw(shape);
					}

					if (isHorizontalPrivate) {
						shape = new Path2D.Double();
						g.setColor(Color.MAGENTA.darker());
						shape.moveTo(dx - 0.1, dy);
						shape.lineTo(dx + 0.1, dy);
						g.draw(shape);
					}

					if (isVerticalBlock) {
						shape = new Path2D.Double();
						g.setColor(Color.RED);
						shape.moveTo(dx, dy - 0.1);
						shape.lineTo(dx, dy + 0.1);
						g.draw(shape);
					}

					if (isHorizontalBlock) {
						shape = new Path2D.Double();
						g.setColor(Color.RED);
						shape.moveTo(dx - 0.1, dy);
						shape.lineTo(dx + 0.1, dy);
						g.draw(shape);
					}
				}

				x++;
			}
			y++;
		}
	}

	private void drawSegments(Graphics2D g) {
		g.setColor(Color.BLUE.darker());
		for (Line registeredSegment : router.getObstacles().getSegments()) {
			Path2D shape = new Path2D.Double();
			shape.moveTo(registeredSegment.x1, registeredSegment.y1);
			shape.lineTo(registeredSegment.x2, registeredSegment.y2);
			g.draw(shape);
		}
	}

	private void drawBlocks(Graphics2D g) {
		g.setColor(Color.BLUE.darker());
		for (Rectangle rec : router.getCoordinatesRegistry().blocked) {

			Rectangle2D drec = new Rectangle2D.Double(rec.x, rec.y, rec.width, rec.height);

			g.draw(drec);
		}
	}

}