package org.workcraft.plugins.circuit.routing.impl;

/**
 * Main routing class.
 */
public class Router {

	private final CoordinatesRegistry coordinates = new CoordinatesRegistry();

	public void setObstacles(Obstacles newObstacles) {
		coordinates.setObstacles(newObstacles);
	}

	public Obstacles getObstacles() {
		return coordinates.getObstacles();
	}

	public CoordinatesRegistry getCoordinatesRegistry() {
		return coordinates;
	}
}
