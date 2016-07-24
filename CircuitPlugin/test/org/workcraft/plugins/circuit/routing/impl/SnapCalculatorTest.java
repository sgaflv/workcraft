package org.workcraft.plugins.circuit.routing.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.workcraft.plugins.circuit.routing.basic.RoutingConstants;

public class SnapCalculatorTest {

	@Test
	public void testSnapToHigherSimple() {
		assertEquals(0.0, SnapCalculator.snapToHigher(0.0, 1), RoutingConstants.EPSILON);
		assertEquals(1.0, SnapCalculator.snapToHigher(0.1, 1), RoutingConstants.EPSILON);
	}

	@Test
	public void testSnapToHigherRemaining() {
		assertEquals(3.5, SnapCalculator.snapToHigher(3.5, 0.5), RoutingConstants.EPSILON);
		assertEquals(3.5, SnapCalculator.snapToHigher(3.5+RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
		assertEquals(3.5, SnapCalculator.snapToHigher(3.5-RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
	}
	
	@Test
	public void testSnapToHigherBasic() {
		assertEquals(4, SnapCalculator.snapToHigher(3.501, 0.5), RoutingConstants.EPSILON);
		assertEquals(3.5, SnapCalculator.snapToHigher(3.409, 0.5), RoutingConstants.EPSILON);
	}
	
	@Test
	public void testSnapToHigherLargerSnap() {
		assertEquals(10, SnapCalculator.snapToHigher(3.5, 10), RoutingConstants.EPSILON);
		assertEquals(10, SnapCalculator.snapToHigher(2*RoutingConstants.EPSILON, 10), RoutingConstants.EPSILON);
		assertEquals(20, SnapCalculator.snapToHigher(10.001, 10), RoutingConstants.EPSILON);
	}

	
	@Test
	public void testSnapToHigherNegative() {
		assertEquals(-3.5, SnapCalculator.snapToHigher(-3.501, 0.5), RoutingConstants.EPSILON);
		assertEquals(-3.0, SnapCalculator.snapToHigher(-3.409, 0.5), RoutingConstants.EPSILON);
	}	
	
	@Test
	public void testSnapToHigherNegativeRemaining() {
		assertEquals(-3.5, SnapCalculator.snapToHigher(-3.5, 0.5), RoutingConstants.EPSILON);
		assertEquals(-3.5, SnapCalculator.snapToHigher(-3.5+RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
		assertEquals(-3.5, SnapCalculator.snapToHigher(-3.5-RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
	}
	

	
	

	@Test
	public void testSnapToLowerSimple() {
		assertEquals(0.0, SnapCalculator.snapToLower(0.0, 1), RoutingConstants.EPSILON);
		assertEquals(0.0, SnapCalculator.snapToLower(0.1, 1), RoutingConstants.EPSILON);
	}

	@Test
	public void testSnapToLowerRemaining() {
		assertEquals(3.5, SnapCalculator.snapToLower(3.5, 0.5), RoutingConstants.EPSILON);
		assertEquals(3.5, SnapCalculator.snapToLower(3.5+RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
		assertEquals(3.5, SnapCalculator.snapToLower(3.5-RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
	}
	
	@Test
	public void testSnapToLowerBasic() {
		assertEquals(3.5, SnapCalculator.snapToLower(3.501, 0.5), RoutingConstants.EPSILON);
		assertEquals(3.0, SnapCalculator.snapToLower(3.409, 0.5), RoutingConstants.EPSILON);
	}
	
	@Test
	public void testSnapToLowerLargerSnap() {
		assertEquals(0, SnapCalculator.snapToLower(3.5, 10), RoutingConstants.EPSILON);
		assertEquals(0, SnapCalculator.snapToLower(2*RoutingConstants.EPSILON, 10), RoutingConstants.EPSILON);
		assertEquals(10, SnapCalculator.snapToLower(10.001, 10), RoutingConstants.EPSILON);
	}

	@Test
	public void testSnapToLowerNegative() {
		assertEquals(-4.0, SnapCalculator.snapToLower(-3.501, 0.5), RoutingConstants.EPSILON);
		assertEquals(-3.5, SnapCalculator.snapToLower(-3.409, 0.5), RoutingConstants.EPSILON);
	}	
	
	@Test
	public void testSnapToLowerNegativeRemaining() {
		assertEquals(-3.5, SnapCalculator.snapToLower(-3.5, 0.5), RoutingConstants.EPSILON);
		assertEquals(-3.5, SnapCalculator.snapToLower(-3.5+RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
		assertEquals(-3.5, SnapCalculator.snapToLower(-3.5-RoutingConstants.EPSILON, 0.5), RoutingConstants.EPSILON);
	}
	

}
