package org.workcraft.plugins.circuit.routing.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.TreeSet;

import org.junit.Test;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

public class SnapCalculatorTest {

    @Test
    public void testSnapToHigherSimple() {
        assertEquals(0.0, SnapCalculator.snapToHigher(0.0, 1), RouterConstants.EPSILON);
        assertEquals(1.0, SnapCalculator.snapToHigher(0.1, 1), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToHigherRemaining() {
        assertEquals(3.5, SnapCalculator.snapToHigher(3.5, 0.5), RouterConstants.EPSILON);
        assertEquals(3.5, SnapCalculator.snapToHigher(3.5 + RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
        assertEquals(3.5, SnapCalculator.snapToHigher(3.5 - RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToHigherBasic() {
        assertEquals(4, SnapCalculator.snapToHigher(3.501, 0.5), RouterConstants.EPSILON);
        assertEquals(3.5, SnapCalculator.snapToHigher(3.409, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToHigherLargerSnap() {
        assertEquals(10, SnapCalculator.snapToHigher(3.5, 10), RouterConstants.EPSILON);
        assertEquals(10, SnapCalculator.snapToHigher(2 * RouterConstants.EPSILON, 10), RouterConstants.EPSILON);
        assertEquals(20, SnapCalculator.snapToHigher(10.001, 10), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToHigherNegative() {
        assertEquals(-3.5, SnapCalculator.snapToHigher(-3.501, 0.5), RouterConstants.EPSILON);
        assertEquals(-3.0, SnapCalculator.snapToHigher(-3.409, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToHigherNegativeRemaining() {
        assertEquals(-3.5, SnapCalculator.snapToHigher(-3.5, 0.5), RouterConstants.EPSILON);
        assertEquals(-3.5, SnapCalculator.snapToHigher(-3.5 + RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
        assertEquals(-3.5, SnapCalculator.snapToHigher(-3.5 - RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToLowerSimple() {
        assertEquals(0.0, SnapCalculator.snapToLower(0.0, 1), RouterConstants.EPSILON);
        assertEquals(0.0, SnapCalculator.snapToLower(0.1, 1), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToLowerRemaining() {
        assertEquals(3.5, SnapCalculator.snapToLower(3.5, 0.5), RouterConstants.EPSILON);
        assertEquals(3.5, SnapCalculator.snapToLower(3.5 + RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
        assertEquals(3.5, SnapCalculator.snapToLower(3.5 - RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToLowerBasic() {
        assertEquals(3.5, SnapCalculator.snapToLower(3.501, 0.5), RouterConstants.EPSILON);
        assertEquals(3.0, SnapCalculator.snapToLower(3.409, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToLowerLargerSnap() {
        assertEquals(0, SnapCalculator.snapToLower(3.5, 10), RouterConstants.EPSILON);
        assertEquals(0, SnapCalculator.snapToLower(2 * RouterConstants.EPSILON, 10), RouterConstants.EPSILON);
        assertEquals(10, SnapCalculator.snapToLower(10.001, 10), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToLowerNegative() {
        assertEquals(-4.0, SnapCalculator.snapToLower(-3.501, 0.5), RouterConstants.EPSILON);
        assertEquals(-3.5, SnapCalculator.snapToLower(-3.409, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToLowerNegativeRemaining() {
        assertEquals(-3.5, SnapCalculator.snapToLower(-3.5, 0.5), RouterConstants.EPSILON);
        assertEquals(-3.5, SnapCalculator.snapToLower(-3.5 + RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
        assertEquals(-3.5, SnapCalculator.snapToLower(-3.5 - RouterConstants.EPSILON, 0.5), RouterConstants.EPSILON);
    }

    @Test
    public void testSnapToLowerVsSnapToHigher() {
        final TreeSet<Double> values = new TreeSet<Double>();

        for (int i = -3; i < 4; i++) {
            final double higher = SnapCalculator.snapToHigher(i - 0.1, 0.25);
            final double lower = SnapCalculator.snapToLower(i + 0.1, 0.25);

            values.add(higher);
            values.add(lower);

            if (higher != lower) {
                fail("SnapToLower from " + (i - 0.1) + " and SnapToHigher from " + (i + 0.1) + " are not compatible");
            }
        }

        assertEquals(7, values.size());

    }
}
