package org.workcraft.plugins.circuit.routing.basic;

public enum PortDirection {
	NORTH, SOUTH, EAST, WEST;

	/**
	 * Returns true if the direction is vertical, returns false otherwise.
	 */
	public boolean isVertical() {
		return this == NORTH || this == SOUTH;
	}

	/**
	 * Returns true if the direction is horizontal, returns false otherwise.
	 */
	public boolean isHorizontal() {
		return this == EAST || this == WEST;
	}
}
