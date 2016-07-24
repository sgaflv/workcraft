package org.workcraft.plugins.circuit.routing.basic;

public enum FieldState {
	/** Field is occupied by an obstacle. */
	BUSY,
	/** Field is vertically blocked by a route segment. */
	VERTICAL_BLOCK,
	/** Field is horizontally blocked by a route segment. */
	HORIZONTAL_BLOCK
}
