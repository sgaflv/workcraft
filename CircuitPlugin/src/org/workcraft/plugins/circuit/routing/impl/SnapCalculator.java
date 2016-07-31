package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

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

		double divided = (value - RouterConstants.EPSILON) / snapSize;
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

		double divided = (value + RouterConstants.EPSILON) / snapSize;
		double floor = Math.floor(divided);
		return floor * snapSize;
	}

}
