package org.workcraft.plugins.circuit.routing.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.workcraft.plugins.circuit.routing.basic.IntegerInterval;
import org.workcraft.plugins.circuit.routing.basic.RoutingConstants;

/**
 * This class maps values to integer indexes.
 */
public final class IndexedValues {

	private final TreeSet<Double> values = new TreeSet<>();
	private final Set<Double> readOnlyValues = Collections.unmodifiableSet(values);

	private final SortedMap<Double, Integer> toIndex = new TreeMap<>();
	private final SortedMap<Integer, Double> toValue = new TreeMap<>();

	private final Set<Double> publicValues = new HashSet<Double>();

	private boolean isBuilt = false;

	/**
	 * Returns the accumulated list of values.
	 * 
	 * @return the accumulated list of values
	 */
	public Set<Double> getValues() {
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
		clearMaps();
	}

	private void clearMaps() {
		toIndex.clear();
		toValue.clear();
		isBuilt = false;
	}

	/**
	 * Add a single value to the mapping.
	 *
	 * @param value
	 *            a single value to be added to the mapping
	 */
	private boolean add(boolean isPublic, double value) {

		boolean changed = values.add(value);

		if (isPublic) {
			publicValues.add(value);
		}

		return changed;
	}

	/**
	 * Add a single value to the mapping.
	 *
	 * @param values
	 *            a values to be added to the mapping
	 */
	public void add(boolean isPublic, double... values) {
		boolean changed = false;

		for (double value : values) {
			changed |= add(isPublic, value);
		}

		if (changed && isBuilt) {
			clearMaps();
		}
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

		int idx = 0;
		for (Double value : values) {
			toIndex.put(value, idx);
			toValue.put(idx, value);
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
	 * @return the interval in indexed values. Return null if the interval is
	 *         not covering any indexed values.
	 */
	public IntegerInterval getIndexedInterval(double from, double to) {
		assert from <= to : "the interval borders must be provided from lower to higher";

		// build indices if necessary
		build();

		Double minBorder = values.ceiling(from - RoutingConstants.EPSILON);
		Double maxBorder = values.floor(to + RoutingConstants.EPSILON);

		if (minBorder == null || maxBorder == null) {
			return null;
		}

		if (minBorder > maxBorder) {
			return null;
		}

		assert isBuilt : "the value indices must be built before calling the getIndexedInterval";

		return new IntegerInterval(toIndex.get(minBorder), toIndex.get(maxBorder));
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

		return toValue.get(index);
	}

	public boolean isPublic(double value) {
		return publicValues.contains(value);
	}
}
