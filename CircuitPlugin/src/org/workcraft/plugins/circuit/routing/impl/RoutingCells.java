package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.FieldState;
import org.workcraft.plugins.circuit.routing.basic.IntegerInterval;

public class RoutingCells {

	/** A rather low-level implementation for the cell states. */
	public final int cells[][];

	public RoutingCells(int width, int height) {
		cells = new int[width][height];
	}

	public void mark(int x1, int y1, int x2, int y2, int value) {

		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				cells[x][y] |= value;
			}
		}
	}

	public void unmark(int x1, int y1, int x2, int y2, int value) {

		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				cells[x][y] = cells[x][y] & (~value);
			}
		}
	}

	public void markBusy(IntegerInterval hInterval, IntegerInterval vInterval) {
		if (hInterval == null || vInterval == null) {
			return;
		}
		mark(hInterval.from, vInterval.from, hInterval.to, vInterval.to, FieldState.BUSY);
	}

	public void unmarkBusy(IntegerInterval hInterval, IntegerInterval vInterval) {
		if (hInterval == null || vInterval == null) {
			return;
		}
		unmark(hInterval.from, vInterval.from, hInterval.to, vInterval.to, FieldState.BUSY);
	}
}
