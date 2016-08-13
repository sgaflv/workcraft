package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.CellState;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;

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

    public void mark(IndexedInterval hInterval, IndexedInterval vInterval, int value) {
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

    public void unmark(IndexedInterval hInterval, IndexedInterval vInterval, int value) {
        if (hInterval == null || vInterval == null) {
            return;
        }
        unmark(hInterval.from, vInterval.from, hInterval.to, vInterval.to, value);
    }

    public void markBusy(IndexedInterval hInterval, IndexedInterval vInterval) {
        mark(hInterval, vInterval, CellState.BUSY);
    }

    public void unmarkBusy(IndexedInterval hInterval, IndexedInterval vInterval) {
        unmark(hInterval, vInterval, CellState.BUSY);
    }
}
