package org.workcraft.plugins.circuit.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.gui.graph.Viewport;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;

public class RoutingGrid {
    private final Set<Double> xGrid = new HashSet<>();
    private final Set<Double> yGrid = new HashSet<>();

    public RoutingGrid(VisualCircuit circuit) {
        for (VisualFunctionComponent component: circuit.getVisualFunctionComponents()) {
            addBounds(component.getBoundingBox());
        }
        for (VisualContact port: circuit.getVisualPorts()) {
            addBounds(port.getBoundingBox());
        }
    }

    private void addBounds(Rectangle2D boundingBox) {
        xGrid.add(boundingBox.getMinX());
        xGrid.add(boundingBox.getMaxX());
        yGrid.add(boundingBox.getMinY());
        yGrid.add(boundingBox.getMaxY());
    }

    public void draw(Graphics2D g, Viewport viewport) {
        Path2D grid = new Path2D.Double();

        Rectangle b = viewport.getShape();
        Point screenTopLeft = new Point(b.x, b.y);
        Point2D userTopLeft = viewport.screenToUser(screenTopLeft);
        Point screenBottomRight = new Point(b.x + b.width, b.y + b.height);
        Point2D userBottomRight = viewport.screenToUser(screenBottomRight);
        for (Double x: xGrid) {
            grid.moveTo(x, userTopLeft.getY());
            grid.lineTo(x, userBottomRight.getY());
        }
        for (Double y: yGrid) {
            grid.moveTo(userTopLeft.getX(), y);
            grid.lineTo(userBottomRight.getX(), y);
        }
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(0.5f * (float) CircuitSettings.getBorderWidth()));
        g.draw(grid);
    }

}