package org.workcraft.plugins.circuit.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.graph.Viewport;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.routing.basic.FieldState;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;
import org.workcraft.plugins.circuit.routing.impl.Obstacles;
import org.workcraft.plugins.circuit.routing.impl.RoutingCells;

public class RoutingGrid {

	Obstacles obstacles = new Obstacles();

	public RoutingGrid(VisualCircuit circuit) {
		registerObstacles(circuit);
	}

	private void registerObstacles(VisualCircuit circuit) {

		for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
			obstacles.addRectangle(getRectangle(component.getInternalBoundingBox()));
		}
		for (VisualContact port : circuit.getVisualPorts()) {
			obstacles.addRectangle(getRectangle(port.getInternalBoundingBox()));
		}
	}

	private Rectangle getRectangle(Rectangle2D rect) {
		return new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public void draw(Graphics2D g, Viewport viewport) {
		Path2D grid = new Path2D.Double();

		java.awt.Rectangle bounds = viewport.getShape();

		Point screenTopLeft = new Point(bounds.x, bounds.y);
		Point2D userTopLeft = viewport.screenToUser(screenTopLeft);
		Point screenBottomRight = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
		Point2D userBottomRight = viewport.screenToUser(screenBottomRight);

		for (double x : obstacles.getXCoordinates()) {
			grid.moveTo(x, userTopLeft.getY());
			grid.lineTo(x, userBottomRight.getY());
		}
		for (double y : obstacles.getYCoordinates()) {
			grid.moveTo(userTopLeft.getX(), y);
			grid.lineTo(userBottomRight.getX(), y);
		}

		g.setColor(Color.GRAY);
		g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
		g.draw(grid);

		RoutingCells rcells = obstacles.getRoutingCells();

		int[][] cells = rcells.cells;

		int y = 0;
		for (double dy : obstacles.getYCoordinates()) {
			int x = 0;
			for (double dx : obstacles.getXCoordinates()) {
				boolean isBusy = (cells[x][y] & FieldState.BUSY) > 0;

				Path2D shape = new Path2D.Double();

				if (isBusy) {
					g.setColor(Color.RED);
					shape.moveTo(dx - 0.1, dy - 0.1);
					shape.lineTo(dx + 0.1, dy + 0.1);
					shape.moveTo(dx + 0.1, dy - 0.1);
					shape.lineTo(dx - 0.1, dy + 0.1);
				} else {
					g.setColor(Color.GREEN);
					shape.moveTo(dx - 0.1, dy - 0.1);
					shape.lineTo(dx + 0.1, dy + 0.1);
					shape.moveTo(dx + 0.1, dy - 0.1);
					shape.lineTo(dx - 0.1, dy + 0.1);
				}

				g.draw(shape);

				x++;
			}
			y++;
		}

	}

}