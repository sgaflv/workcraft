package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.workcraft.plugins.circuit.routing.primitive.IntegerInterval;

/**
 * This class maps values to integer indexes.
 */
public final class IndexedValues {
	
	private final TreeSet<Double> values = new TreeSet<>();
	
	private final TreeMap<Double, Integer> toIndex = new TreeMap<>();
	private final TreeMap<Integer, Double> toValue = new TreeMap<>();
	
	private boolean isBuilt = false;
	
	/**
	 * Remove all coordinates.
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
	 * Add a single coordinate to the mapping.
	 *
	 * @param coordinate
	 *            a single coordinate to be added to the mapping
	 */
	public void add(double coordinate) {
		if (isBuilt) {
			clearMaps();
		}
		
		values.add(coordinate);
	}
	
	/**
	 * Add a list of new coordinates
	 *
	 * @param coordinates
	 *            the list of coordinates to be added
	 */
	public void add(List<Double> coordinates) {
		if (isBuilt) {
			clearMaps();
		}
		
		values.addAll(coordinates);
	}
	
	public int size() {
		return values.size();
	}
	
	/**
	 * Create the coordinate mapping after all the coordinates were added.
	 */
	public void build() {
		if (isBuilt) {
			return;
		}
		
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
	 * @return the interval in indexed coordinates. Return null if the interval
	 *         is not covering any indexed coordinates.
	 */
	public IntegerInterval getIndexedInterval(double from, double to) {
		assert from <= to : "the first interval border must not be greater than the second";
		
		Double minBorder = values.floor(from);
		Double maxBorder = values.ceiling(to);
		
		if (minBorder==null || maxBorder==null) {
			return null;
		}
		
		return new IntegerInterval(toIndex.get(minBorder), toIndex.get(maxBorder));
	}
}
