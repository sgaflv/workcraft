package org.workcraft.plugins.circuit.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.dom.Node;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.PortDirection;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;
import org.workcraft.plugins.circuit.routing.basic.RouterPort;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterCells;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;

/**
 * The class creates the routing task and launches the router.
 */
public class RouterClient {

	private final Router router = new Router();

	public RouterClient() {
	}

	private Map<Node, RouterPort> portMap = new HashMap<Node, RouterPort>();

	public void registerObstacles(VisualCircuit circuit) {

		RouterTask newTask = new RouterTask();

		for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {

			Rectangle internalBoundingBox = getRectangle(component.getInternalBoundingBox());

			newTask.addRectangle(internalBoundingBox);

			for (VisualContact contact : component.getContacts()) {

				Point portPoint = new Point(contact.getX() + component.getX(), contact.getY() + component.getY());

				RouterPort newPort = new RouterPort(getDirection(contact), portPoint, false);
				portMap.put(contact, newPort);
				newTask.addPort(newPort);

				Line portSegment = internalBoundingBox.getPortSegment(portPoint);
				newTask.addSegment(portSegment);
			}
		}

		for (VisualContact port : circuit.getVisualPorts()) {
			Rectangle2D internalBoundingBox = port.getInternalBoundingBox();
			newTask.addRectangle(getRectangle(internalBoundingBox));

			RouterPort newPort = new RouterPort(getDirection(port),
					new Point(internalBoundingBox.getCenterX(), internalBoundingBox.getCenterY()), true);

			portMap.put(port, newPort);

			newTask.addPort(newPort);
		}

		// TODO: use the math model instead
		for (Entry<Node, RouterPort> entry : portMap.entrySet()) {
			Node node = entry.getKey();
			RouterPort source = entry.getValue();
			for (Node nodeDest : circuit.getPostset(node)) {
				RouterPort destination = portMap.get(nodeDest);

				if (destination != null) {
					newTask.addConnection(new RouterConnection(source, destination));
				}
			}
		}

		router.setObstacles(newTask);
	}

	private PortDirection getDirection(VisualContact contact) {

		org.workcraft.plugins.circuit.VisualContact.Direction direction = contact.getDirection();

		switch (direction) {
		case EAST:
			return PortDirection.EAST;
		case WEST:
			return PortDirection.WEST;
		case NORTH:
			return PortDirection.NORTH;
		case SOUTH:
			return PortDirection.SOUTH;
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
		drawConnections(g);
	}

	private void drawConnections(Graphics2D g) {
		for (RouterConnection connection : router.getObstacles().getConnections()) {
			RouterPort src = connection.source;
			RouterPort dest = connection.destination;

			Line2D line = new Line2D.Double(src.location.x, src.location.y, dest.location.x, dest.location.y);

			g.setColor(Color.RED);
			g.draw(line);

		}
	}

	private void drawCoordinates(Graphics2D g, Viewport viewport) {

		java.awt.Rectangle bounds = viewport.getShape();

		java.awt.Point screenTopLeft = new java.awt.Point(bounds.x, bounds.y);
		Point2D userTopLeft = viewport.screenToUser(screenTopLeft);
		java.awt.Point screenBottomRight = new java.awt.Point(bounds.x + bounds.width, bounds.y + bounds.height);
		Point2D userBottomRight = viewport.screenToUser(screenBottomRight);

		for (Coordinate x : router.getCoordinatesRegistry().getXCoordinates()) {

			if (!x.isPublic) {
				continue;
			}

			Path2D grid = new Path2D.Double();
			grid.moveTo(x.value, userTopLeft.getY());
			grid.lineTo(x.value, userBottomRight.getY());
			g.setColor(Color.GRAY.brighter());

			if (x.orientation == CoordinateOrientation.ORIENT_LOWER) {
				g.setColor(Color.BLUE);
			}
			if (x.orientation == CoordinateOrientation.ORIENT_HIGHER) {
				g.setColor(Color.RED);
			}

			g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
			g.draw(grid);
		}
		for (Coordinate y : router.getCoordinatesRegistry().getYCoordinates()) {

			if (!y.isPublic) {
				continue;
			}

			Path2D grid = new Path2D.Double();
			grid.moveTo(userTopLeft.getX(), y.value);
			grid.lineTo(userBottomRight.getX(), y.value);
			g.setColor(Color.GRAY.brighter());

			if (y.orientation == CoordinateOrientation.ORIENT_LOWER) {
				g.setColor(Color.BLUE);
			}
			if (y.orientation == CoordinateOrientation.ORIENT_HIGHER) {
				g.setColor(Color.RED);
			}
			g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
			g.draw(grid);
		}

	}

	private void drawCells(Graphics2D g) {
		RouterCells rcells = router.getCoordinatesRegistry().getRoutingCells();

		int[][] cells = rcells.cells;

		int y = 0;
		for (Coordinate dy : router.getCoordinatesRegistry().getYCoordinates()) {
			int x = 0;
			for (Coordinate dx : router.getCoordinatesRegistry().getXCoordinates()) {
				boolean isBusy = (cells[x][y] & CellState.BUSY) > 0;
				boolean isVerticalPrivate = (cells[x][y] & CellState.VERTICAL_PUBLIC) == 0;
				boolean isHorizontalPrivate = (cells[x][y] & CellState.HORIZONTAL_PUBLIC) == 0;

				boolean isVerticalBlock = (cells[x][y] & CellState.VERTICAL_BLOCK) != 0;
				boolean isHorizontalBlock = (cells[x][y] & CellState.HORIZONTAL_BLOCK) != 0;

				Path2D shape = new Path2D.Double();

				if (isBusy) {
					g.setColor(Color.RED);
					shape.moveTo(dx.value - 0.1, dy.value - 0.1);
					shape.lineTo(dx.value + 0.1, dy.value + 0.1);
					shape.moveTo(dx.value + 0.1, dy.value - 0.1);
					shape.lineTo(dx.value - 0.1, dy.value + 0.1);
					g.draw(shape);
				} else {

					if (isVerticalPrivate) {
						shape = new Path2D.Double();
						g.setColor(Color.MAGENTA.darker());
						shape.moveTo(dx.value, dy.value - 0.1);
						shape.lineTo(dx.value, dy.value + 0.1);
						g.draw(shape);
					}

					if (isHorizontalPrivate) {
						shape = new Path2D.Double();
						g.setColor(Color.MAGENTA.darker());
						shape.moveTo(dx.value - 0.1, dy.value);
						shape.lineTo(dx.value + 0.1, dy.value);
						g.draw(shape);
					}

					if (isVerticalBlock) {
						shape = new Path2D.Double();
						g.setColor(Color.RED);
						shape.moveTo(dx.value, dy.value - 0.1);
						shape.lineTo(dx.value, dy.value + 0.1);
						g.draw(shape);
					}

					if (isHorizontalBlock) {
						shape = new Path2D.Double();
						g.setColor(Color.RED);
						shape.moveTo(dx.value - 0.1, dy.value);
						shape.lineTo(dx.value + 0.1, dy.value);
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