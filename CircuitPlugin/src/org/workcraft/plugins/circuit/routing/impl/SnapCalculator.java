package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

public class SnapCalculator {

    /**
     * Snap given value to a higher snapped value. The snapSize must be
     * positive.
     *
     * @param value
     *            value to snap
     * @param snapSize
     *            the size of the snap
     * @return snapped value
     */
    public static double snapToHigher(double value, double snapSize) {
        assert snapSize > 0 : "snapSize must be positive";

        final double divided = (value - RouterConstants.EPSILON) / snapSize;
        final double ceil = Math.ceil(divided);

        if (ceil == -0.0) {
            // special case, to avoid returning -0.0
            return 0.0;
        }

        return ceil * snapSize;
    }

    /**
     * Snap given value to a lower snapped value. The snapSize must be positive.
     *
     * @param value
     *            value to snap
     * @param snapSize
     *            the size of the snap
     * @return snapped value
     */
    public static double snapToLower(double value, double snapSize) {
        assert snapSize > 0 : "snapSize must be positive";

        final double divided = (value + RouterConstants.EPSILON) / snapSize;
        final double floor = Math.floor(divided);
        return floor * snapSize;
    }

    /**
     * Snap given value to a closest snapped value. The snapSize must be
     * positive.
     *
     * @param value
     *            value to snap
     * @param snapSize
     *            the size of the snap
     * @return snapped value
     */
    public static double snapToClosest(double value, double snapSize) {
        final double higher = snapToHigher(value, snapSize);
        final double lower = snapToLower(value, snapSize);

        final double distHigher = Math.abs(higher - value);
        final double distLower = Math.abs(lower - value);

        if (distHigher < distLower) {
            return higher;
        }

        return lower;
    }
}
