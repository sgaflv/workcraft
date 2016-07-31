package org.workcraft.plugins.circuit.routing.impl;

/**
 * Main routing class.
 */
public class Router {

	private final CoordinatesRegistry coordinates = new CoordinatesRegistry();

	public void setObstacles(RouterTask newObstacles) {
		coordinates.setObstacles(newObstacles);
	}

	public RouterTask getObstacles() {
		return coordinates.getObstacles();
	}

	public CoordinatesRegistry getCoordinatesRegistry() {
		return coordinates;
	}
}
