package org.workcraft.plugins.circuit.routing.basic;

public class CellState {

	/** Field is occupied by an obstacle. */
	public static int BUSY = 1;

	/** Vertical propagation is blocked for routing. */
	public static int VERTICAL_BLOCK = 2;

	/** Horizontal propagation is blocked for routing. */
	public static int HORIZONTAL_BLOCK = 4;

	/**
	 * Cells not marked as public only allow propagation for entering or exiting
	 * ports.
	 */
	public static int HORIZONTAL_PUBLIC = 8;

	/**
	 * Cells not marked as public only allow propagation for entering or exiting
	 * ports.
	 */
	public static int VERTICAL_PUBLIC = 16;

}
