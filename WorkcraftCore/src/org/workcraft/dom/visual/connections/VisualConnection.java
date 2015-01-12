/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.ObservableHierarchyImpl;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualConnection extends VisualNode implements Node, Drawable, DependentNode,
		Connection, VisualConnectionProperties, ObservableHierarchy {

	public enum ConnectionType {
		POLYLINE("Polyline"),
		BEZIER("Bezier");

		private final String name;

		private ConnectionType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	public enum ScaleMode {
		NONE("Lock anchors"),
		LOCK_RELATIVELY("Bind to components"),
		SCALE("Proportional"),
		STRETCH("Stretch"),
		ADAPTIVE("Adaptive");

		private final String name;

		private ScaleMode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();

	private MathConnection refConnection = null;
	private VisualComponent first = null;
	private VisualComponent second = null;

	private ConnectionType connectionType = ConnectionType.POLYLINE;
	private ScaleMode scaleMode = ScaleMode.NONE;

	private ConnectionGraphic graphic = null;

	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	private static double defaultBubbleSize = 0.2;
	public static double HIT_THRESHOLD = 0.2;
	private static Color defaultColor = Color.BLACK;

	private Color color = defaultColor;
	private double lineWidth = defaultLineWidth;

	private boolean hasArrow = true;
	private double arrowWidth = defaultArrowWidth;
	private double arrowLength = defaultArrowLength;

	private boolean hasBubble = false;
	private double bubbleSize = defaultBubbleSize;

	private boolean isTokenColorPropagator = false;
	private Point2D splitPoint = null;

	private LinkedHashSet<Node> children = new LinkedHashSet<Node>();
	private ComponentsTransformObserver componentsTransformObserver = null;

	public VisualConnection() {
		this(null, null, null);
	}

	public VisualConnection(MathConnection refConnection) {
		this(refConnection, null, null);
	}

	public VisualConnection(MathConnection refConnection, VisualComponent first, VisualComponent second) {
		this.refConnection = refConnection;
		if ((first != null) && (second != null)) {
			this.first = first;
			this.second = second;
			this.graphic = new Polyline(this);
		}
		initialise();
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Double>(
				this, "Line width", Double.class) {
			@Override
			public void setter(VisualConnection object, Double value) {
				object.setLineWidth(value);
			}
			@Override
			public Double getter(VisualConnection object) {
				return object.getLineWidth();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Double>(
				this, "Arrow width", Double.class) {
			@Override
			public void setter(VisualConnection object, Double value) {
				object.setArrowWidth(value);
			}
			@Override
			public Double getter(VisualConnection object) {
				return object.getArrowWidth();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Double>(
				this, "Arrow length", Double.class) {
			@Override
			public void setter(VisualConnection object, Double value) {
				object.setArrowLength(value);
			}
			@Override
			public Double getter(VisualConnection object) {
				return object.getArrowLength();
			}
			@Override
			public Map<Double, String> getChoice() {
				LinkedHashMap<Double, String> result = new LinkedHashMap<Double, String>();
				result.put(0.0, "none");
				result.put(0.2, "short");
				result.put(0.4, "medium");
				result.put(0.8, "long");
				return result;
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualConnection, ConnectionType>(
				this, "Connection type", ConnectionType.class) {
			protected void setter(VisualConnection object, ConnectionType value) {
				object.setConnectionType(value);
			}
			protected ConnectionType getter(VisualConnection object) {
				return object.getConnectionType();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualConnection, ScaleMode>(
				this, "Scale mode", ScaleMode.class) {
			protected void setter(VisualConnection object, ScaleMode value) {
				object.setScaleMode(value);
			}
			protected ScaleMode getter(VisualConnection object) {
				return object.getScaleMode();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualConnection, Color>(
				this, "Color", Color.class) {
			protected void setter(VisualConnection object, Color value) {
				object.setColor(value);
			}
			protected Color getter(VisualConnection object) {
				return object.getColor();
			}
		});

	}

	protected void initialise() {
		children.clear();
		componentsTransformObserver = new ComponentsTransformObserver(this);
		children.add(componentsTransformObserver);
		if (graphic != null) {
			children.add(graphic);
		}
		if (refConnection instanceof ObservableState) {
			((ObservableState)refConnection).addObserver(new StateObserver() {
				public void notify(StateEvent e) {
					observableStateImpl.sendNotification(e);
				}
			});
		}
	}

	public void setVisualConnectionDependencies(VisualComponent first,	VisualComponent second,
			ConnectionGraphic graphic, MathConnection refConnection) {
		if (first == null)
			throw new NullPointerException("first");
		if (second == null)
			throw new NullPointerException("second");
		if (graphic == null)
			throw new NullPointerException("graphic");

		this.first = first;
		this.second = second;
		this.refConnection = refConnection;
		this.graphic = graphic;

		if (graphic instanceof Polyline) {
			connectionType = ConnectionType.POLYLINE;
		} else if (graphic instanceof Bezier) {
			connectionType = ConnectionType.BEZIER;
		}
		initialise();
	}

	@NoAutoSerialisation
	public ConnectionType getConnectionType() {
		return connectionType;
	}

	@NoAutoSerialisation
	public void setConnectionType(ConnectionType t) {
		if (connectionType != t) {
			this.connectionType = t;
			this.observableHierarchyImpl.sendNotification(new NodesDeletingEvent(this, getGraphic()));
			this.children.remove(getGraphic());
			this.observableHierarchyImpl.sendNotification(new NodesDeletedEvent(this, getGraphic()));

			if (connectionType == ConnectionType.POLYLINE) {
				graphic = new Polyline(this);
			} else if (connectionType == ConnectionType.BEZIER) {
				graphic = new Bezier(this);
			}
			graphic.setDefaultControlPoints();

			this.children.add(graphic);
			this.observableHierarchyImpl.sendNotification(new NodesAddedEvent(this,	getGraphic()));
			this.graphic.invalidate();
			this.observableStateImpl.sendNotification(new PropertyChangedEvent(this, "connectionType"));
		}
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public Color getDrawColor() {
		return getColor();
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		if (lineWidth < 0.01)
			lineWidth = 0.01;
		if (lineWidth > 0.5)
			lineWidth = 0.5;
		this.lineWidth = lineWidth;

		invalidate();
	}

	@Override
	public Stroke getStroke() {
		return new BasicStroke((float) getLineWidth());
	}

	@Override
	public boolean hasArrow() {
		return hasArrow;
	}

	public void setArrow(boolean value) {
		hasArrow = value;
	}

	@Override
	public double getArrowWidth() {
		return arrowWidth;
	}

	public void setArrowWidth(double value) {
		if (value > 1)	value = 1;
		if (value < 0.0) value = 0.0;
		this.arrowWidth = value;
		invalidate();
	}

	@Override
	public double getArrowLength() {
		if (!hasArrow()) return 0.0;
		return arrowLength;
	}

	public void setArrowLength(double value) {
		if (value > 1) value = 1;
		if (value < 0.0) value = 0.0;
		this.arrowLength = value;
		invalidate();
	}

	public void invalidate() {
		if (graphic != null) {
			graphic.invalidate();
		}
	}

	@Override
	public boolean hasBubble() {
		return hasBubble;
	}

	public void setBubble(boolean value) {
		hasBubble = value;
	}

	@Override
	public double getBubbleSize() {
		if (!hasArrow()) return 0.0;
		return bubbleSize;
	}

	public void setBubbleSize(double value) {
		if (value > 1)value = 1;
		if (value < 0.1) value = 0.1;
		this.bubbleSize = value;
		invalidate();
	}

	@Override
	public boolean isTokenColorPropagator() {
		return isTokenColorPropagator;
	}

	public void setTokenColorPropagator(boolean value) {
		isTokenColorPropagator = value;
	}

	@NoAutoSerialisation
	public void setSplitPoint(Point2D point) {
		splitPoint = point;
	}

	@NoAutoSerialisation
	public Point2D getSplitPoint() {
		return (splitPoint == null) ? getPointOnConnection(0.5) : splitPoint;
	}

	public Point2D getPointOnConnection(double t) {
		return graphic.getPointOnCurve(t);
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		return graphic.getNearestPointOnCurve(pt);
	}

	@Override
	public void setParent(Node parent) {
		super.setParent(parent);
		invalidate();
	};

	@Override
	public void draw(DrawRequest r) {

	}

	public MathConnection getReferencedConnection() {
		return refConnection;
	}

	public boolean hitTest(Point2D pointInParentSpace) {
		return graphic.hitTest(pointInParentSpace);
	}

	@Override
	public Rectangle2D getBoundingBox() {
		return graphic.getBoundingBox();
	}

	public VisualComponent getFirst() {
		return first;
	}

	public VisualComponent getSecond() {
		return second;
	}

	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedConnection());
		return ret;
	}

	public ConnectionGraphic getGraphic() {
		return graphic;
	}

	@Override
	public Collection<Node> getChildren() {
		return children;
	}

	public void addObserver(HierarchyObserver obs) {
		observableHierarchyImpl.addObserver(obs);
	}

	public void removeObserver(HierarchyObserver obs) {
		observableHierarchyImpl.removeObserver(obs);
	}

	public void removeAllObservers() {
		observableHierarchyImpl.removeAllObservers();
	}

	@Override
	public Point2D getFirstCenter() {
		return componentsTransformObserver.getFirstCenter();
	}

	@Override
	public Touchable getFirstShape() {
		return componentsTransformObserver.getFirstShape();
	}

	@Override
	public Point2D getSecondCenter() {
		return componentsTransformObserver.getSecondCenter();
	}

	@Override
	public Touchable getSecondShape() {
		return componentsTransformObserver.getSecondShape();
	}

	@Override
	public ScaleMode getScaleMode() {
		return scaleMode;
	}

	@Override
	public Point2D getCenter() {
		return graphic.getCenter();
	}

	public void setScaleMode(ScaleMode scaleMode) {
		this.scaleMode = scaleMode;
	}

	@Override
	public void copyStyle(Stylable src) {
		super.copyStyle(src);
		if (src instanceof VisualConnection) {
			VisualConnection srcConnection = (VisualConnection)src;
			setConnectionType(srcConnection.getConnectionType());
			ConnectionGraphic srcGraphics = srcConnection.getGraphic();
			setColor(srcConnection.getColor());
			setLineWidth(srcConnection.getLineWidth());
			setArrowLength(srcConnection.getArrowLength());
			setArrowWidth(srcConnection.getArrowWidth());
			setBubbleSize(srcConnection.getBubbleSize());
			setScaleMode(srcConnection.getScaleMode());

			if (srcGraphics instanceof Polyline) {
				Polyline polyline = (Polyline)getGraphic();
				polyline.resetControlPoints();
				for (Node srcNode: srcGraphics.getChildren()) {
					if (srcNode instanceof ControlPoint) {
						ControlPoint srcCp = (ControlPoint)srcNode;
						polyline.addControlPoint(srcCp.getPosition());
					}
				}
			} else if (srcGraphics instanceof Bezier) {
				BezierControlPoint[] p = ((Bezier)srcGraphics).getControlPoints();
				BezierControlPoint cp1 = new BezierControlPoint();
				cp1.setPosition(p[0].getPosition());
				BezierControlPoint cp2 = new BezierControlPoint();
				cp2.setPosition(p[1].getPosition());
				Bezier bezier = (Bezier)getGraphic();
				bezier.initControlPoints(cp1, cp2);
			}
		}
	}

}
