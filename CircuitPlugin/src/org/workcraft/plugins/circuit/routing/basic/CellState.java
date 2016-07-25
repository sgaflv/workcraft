package org.workcraft.plugins.circuit.routing.basic;

public class CellState {
	/** Field is occupied by an obstacle. */
	public static int BUSY = 1;
	/** Field is vertically blocked by a route segment. */
	public static int VERTICAL_BLOCK = 2;
	/** Field is horizontally blocked by a route segment. */
	public static int HORIZONTAL_BLOCK = 4;
}
