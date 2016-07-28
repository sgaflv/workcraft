package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.RoutingConstants;

public class SnapCalculator {

	/**
	 * Snap given value to a higher value. The snapSize must be positive.
	 * 
	 * @param value
	 *            value to snap
	 * @param snapSize
	 *            the size of the snap
	 * @return snapped value
	 */
	public static double snapToHigher(double value, double snapSize) {
		assert snapSize > 0 : "snapSize must be positive";

		double divided = (value - RoutingConstants.EPSILON) / snapSize;
		double ceil = Math.ceil(divided);
		return ceil * snapSize;
	}

	/**
	 * Snap given value to a lower value. The snapSize must be positive.
	 * 
	 * @param value
	 *            value to snap
	 * @param snapSize
	 *            the size of the snap
	 * @return snapped value
	 */
	public static double snapToLower(double value, double snapSize) {
		assert snapSize > 0 : "snapSize must be positive";

		double divided = (value + RoutingConstants.EPSILON) / snapSize;
		double floor = Math.floor(divided);
		return floor * snapSize;
	}

	/**
	 * Returns true if the given value is already a snapped value.
	 * 
	 * @param value
	 *            value to check for snapping
	 * @param snapSize
	 *            snap size
	 * @return true if the given value is a snapped value, false otherwise
	 */
	public static boolean isSnapped(double value, double snapSize) {
		double lower = snapToLower(value, snapSize);
		if (Math.abs(lower - value) <= RoutingConstants.EPSILON) {
			return true;
		}

		double higher = snapToHigher(value, snapSize);
		if (Math.abs(higher - value) <= RoutingConstants.EPSILON) {
			return true;
		}

		return false;
	}

}
