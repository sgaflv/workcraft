package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.primitive.RoutingConstants;

public class SnapCalculator {
	
	/**
	 * Snap given value to a higher value
	 * @param value value to snap
	 * @param snapSize the size of the snap
	 * @return snapped value
	 */
	public static double snapToHigher(double value, double snapSize) {
		assert snapSize > 0 : "snapSize must be positive";
		
		double divided = (value-RoutingConstants.EPSILON)/snapSize;
		double ceil = Math.ceil(divided);
		return ceil*snapSize;
	}	
	
}
