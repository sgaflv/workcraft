package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.workcraft.plugins.circuit.routing.basic.Connection;
import org.workcraft.plugins.circuit.routing.basic.Line;
import org.workcraft.plugins.circuit.routing.basic.Port;
import org.workcraft.plugins.circuit.routing.basic.Rectangle;

/**
 * The class completely defines the routing task. It's equality function can be
 * used to determine if two routing tasks would produce the same routing result.
 */
public class Obstacles {

	private final List<Rectangle> rectangles = new ArrayList<Rectangle>();
	private final List<Line> segments = new ArrayList<>();
	private final Set<Port> ports = new HashSet<>();
	private final Set<Connection> connections = new HashSet<>();

	public void addRectangle(Rectangle rec) {
		rectangles.add(rec);
	}

	public void addSegment(Line line) {
		if (line == null) {
			return;
		}

		segments.add(line);
	}

	public void addPort(Port port) {
		if (port == null) {
			return;
		}

		ports.add(port);
	}

	public void addConnection(Connection connection) {
		addPort(connection.source);
		addPort(connection.destination);

		connections.add(connection);
	}

	public List<Line> getSegments() {
		return segments;
	}

	public List<Rectangle> getRectangles() {
		return rectangles;
	}

	public Set<Port> getPorts() {
		return ports;
	}

	public Set<Connection> getConnections() {
		return connections;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connections == null) ? 0 : connections.hashCode());
		result = prime * result + ((ports == null) ? 0 : ports.hashCode());
		result = prime * result + ((rectangles == null) ? 0 : rectangles.hashCode());
		result = prime * result + ((segments == null) ? 0 : segments.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Obstacles other = (Obstacles) obj;
		if (connections == null) {
			if (other.connections != null)
				return false;
		} else if (!connections.equals(other.connections))
			return false;
		if (ports == null) {
			if (other.ports != null)
				return false;
		} else if (!ports.equals(other.ports))
			return false;
		if (rectangles == null) {
			if (other.rectangles != null)
				return false;
		} else if (!rectangles.equals(other.rectangles))
			return false;
		if (segments == null) {
			if (other.segments != null)
				return false;
		} else if (!segments.equals(other.segments))
			return false;
		return true;
	}

}
