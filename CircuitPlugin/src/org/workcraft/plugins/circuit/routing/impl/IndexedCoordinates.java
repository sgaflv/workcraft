package org.workcraft.plugins.circuit.routing.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.IntegerInterval;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

/**
 * This class maps coordinates to integer indexes.
 */
public final class IndexedCoordinates {

    private final TreeMap<Double, Coordinate> values = new TreeMap<>();
    private final Collection<Coordinate> readOnlyValues = Collections.unmodifiableCollection(values.values());

    private final SortedMap<Coordinate, Integer> toIndex = new TreeMap<>();
    private Coordinate[] toValue;

    private final Set<Double> publicValues = new HashSet<Double>();

    private boolean isBuilt = false;

    /**
     * Returns the accumulated list of values.
     *
     * @return the accumulated list of values
     */
    public Collection<Coordinate> getValues() {
        return readOnlyValues;
    }

    /**
     * Shows whether value indices are built.
     *
     * @return true if indices are built, false otherwise
     */
    public boolean isBuilt() {
        return isBuilt;
    }

    /**
     * Remove all values.
     */
    public void clear() {
        values.clear();
        publicValues.clear();
        clearMaps();
    }

    /**
     * Remove a single value.
     */
    public void remove(double value) {
        values.remove(value);
        publicValues.remove(value);
        clearMaps();
    }

    private void clearMaps() {
        toIndex.clear();
        toValue = null;
        isBuilt = false;
    }

    /**
     * Add a single value to the mapping.
     *
     * @param value
     *            a single value to be added to the mapping
     */
    private boolean add(Coordinate coordinate) {

        final Coordinate oldValue = values.put(coordinate.value, coordinate);

        if (coordinate.isPublic) {
            publicValues.add(coordinate.value);
        }

        return oldValue == null;
    }

    private void addValue(boolean isPublic, CoordinateOrientation orientation, double... values) {
        boolean changed = false;

        for (final double value : values) {
            CoordinateOrientation newOrientation = orientation;

            final Coordinate oldCoordinate = this.values.get(value);

            if (oldCoordinate != null) {
                newOrientation = orientation.merge(oldCoordinate.orientation);
            }

            final Coordinate newCoordinate = new Coordinate(newOrientation, isPublic || isPublic(value), value);

            changed |= add(newCoordinate);
        }

        if (changed && isBuilt) {
            clearMaps();
        }
    }

    /**
     * Add public values to the mapping.
     *
     * @param values
     *            a values to be added to the mapping
     */
    public void addPublic(CoordinateOrientation orientation, double... values) {
        addValue(true, orientation, values);
    }

    /**
     * Add private values to the mapping.
     *
     * @param values
     *            a values to be added to the mapping
     */
    public void addPrivate(CoordinateOrientation orientation, double... values) {
        addValue(false, orientation, values);
    }

    public int size() {
        return values.size();
    }

    /**
     * Create the value-to-index mapping after all the values were added.
     */
    public void build() {
        if (isBuilt) {
            return;
        }

        clearMaps();

        toValue = new Coordinate[values.size()];
        int idx = 0;
        for (final Coordinate coordinate : values.values()) {
            toIndex.put(coordinate, idx);
            toValue[idx] = coordinate;
            idx++;
        }

        isBuilt = true;
    }

    /**
     * Return the indices covered by the given interval.
     *
     * @param from
     *            lowest border interval
     * @param to
     *            highest border interval
     * @return the interval in indexed values. Returns null if the interval is
     *         not covering any indexed values.
     */
    public IntegerInterval getIndexedInterval(double from, double to) {
        assert from <= to : "the interval borders must be provided from lower to higher";

        build();

        final Double minBorder = values.ceilingKey(from - RouterConstants.EPSILON);
        final Double maxBorder = values.floorKey(to + RouterConstants.EPSILON);

        if (minBorder == null || maxBorder == null) {
            return null;
        }

        if (minBorder > maxBorder) {
            return null;
        }

        return new IntegerInterval(toIndex.get(values.get(minBorder)), toIndex.get(values.get(maxBorder)));
    }

    /**
     * Return the indices covered by the given interval. It does not include the
     * interval boundaries.
     *
     * @param from
     *            lowest border interval
     * @param to
     *            highest border interval
     * @return the interval in indexed values. Returns null if the interval is
     *         not covering any indexed values.
     */
    public IntegerInterval getIndexedIntervalExclusive(double from, double to) {
        assert from <= to : "the interval borders must be provided from lower to higher";

        build();

        final Double minBorder = values.ceilingKey(from + 2 * RouterConstants.EPSILON);
        final Double maxBorder = values.floorKey(to - 2 * RouterConstants.EPSILON);

        if (minBorder == null || maxBorder == null) {
            return null;
        }

        if (minBorder > maxBorder) {
            return null;
        }

        return new IntegerInterval(toIndex.get(values.get(minBorder)), toIndex.get(values.get(maxBorder)));
    }

    /**
     * Return registered value by specified index.
     *
     * @param index
     *            the index of value to be returned
     * @return the value registered for specified index
     */
    public double getValueByIndex(int index) {

        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException();
        }

        build();

        return toValue[index].value;
    }

    public boolean isPublic(double value) {
        return publicValues.contains(value);
    }

    public void mergeCoordinates() {
        Coordinate last = null;
        final List<Coordinate> toAdd = new ArrayList<Coordinate>();
        final List<Coordinate> toDelete = new ArrayList<Coordinate>();

        for (final Coordinate coordinate : getValues()) {
            if (!(coordinate.isPublic)) {
                continue;
            }

            if (coordinate.orientation == CoordinateOrientation.ORIENT_HIGHER) {

                if (last != null && last.orientation == CoordinateOrientation.ORIENT_HIGHER) {
                    toDelete.add(last);
                }

                last = coordinate;
                continue;
            }

            if (last != null && coordinate.orientation == CoordinateOrientation.ORIENT_LOWER) {

                switch (last.orientation) {
                case ORIENT_HIGHER:

                    final double middle = SnapCalculator.snapToClosest((last.value + coordinate.value) / 2,
                            RouterConstants.SEGMENT_MARGIN);

                    toAdd.add(new Coordinate(CoordinateOrientation.ORIENT_BOTH, true, middle));
                    toDelete.add(last);
                    toDelete.add(coordinate);
                    break;

                case ORIENT_LOWER:

                    toDelete.add(coordinate);
                    break;

                default:
                    break;
                }

            }

            last = coordinate;
        }

        for (final Coordinate coordinate : toDelete) {
            remove(coordinate.value);
        }

        for (final Coordinate coordinate : toAdd) {
            add(coordinate);
        }
    }
}
