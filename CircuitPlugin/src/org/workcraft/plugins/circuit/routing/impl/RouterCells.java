package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IntegerInterval;

public class RouterCells {

    /** A rather low-level implementation for the cell states. */
    public final int[][] cells;

    public RouterCells(int width, int height) {
        cells = new int[width][height];
    }

    public void mark(int x1, int y1, int x2, int y2, int value) {
        if (value == 0) {
            return;
        }

        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                cells[x][y] |= value;
            }
        }
    }

    public void mark(IntegerInterval hInterval, IntegerInterval vInterval, int value) {
        if (hInterval == null || vInterval == null) {
            return;
        }
        mark(hInterval.from, vInterval.from, hInterval.to, vInterval.to, value);
    }

    public void unmark(int x1, int y1, int x2, int y2, int value) {
        if (value == 0) {
            return;
        }

        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                cells[x][y] = cells[x][y] & (~value);
            }
        }
    }

    public void unmark(IntegerInterval hInterval, IntegerInterval vInterval, int value) {
        if (hInterval == null || vInterval == null) {
            return;
        }
        unmark(hInterval.from, vInterval.from, hInterval.to, vInterval.to, value);
    }

    public void markBusy(IntegerInterval hInterval, IntegerInterval vInterval) {
        mark(hInterval, vInterval, CellState.BUSY);
    }

    public void unmarkBusy(IntegerInterval hInterval, IntegerInterval vInterval) {
        unmark(hInterval, vInterval, CellState.BUSY);
    }
}
