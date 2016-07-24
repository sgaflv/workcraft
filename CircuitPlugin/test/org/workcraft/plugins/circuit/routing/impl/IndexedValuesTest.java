package org.workcraft.plugins.circuit.routing.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.workcraft.plugins.circuit.routing.primitive.IntegerInterval;
import org.workcraft.plugins.circuit.routing.primitive.RoutingConstants;

public class IndexedValuesTest {

	IndexedValues indexedValues = new IndexedValues();

	@Test
	public void testIsBuilt() {
		assertFalse("The Initial isBuilt is not false", indexedValues.isBuilt());

		indexedValues.add(1.5);
		assertFalse("The isBuilt value must be false on adding", indexedValues.isBuilt());

		indexedValues.build();
		assertTrue("The isBuilt value must be true after build", indexedValues.isBuilt());

		indexedValues.add(1.5);
		assertTrue("The repeated insertion has reset isBuilt", indexedValues.isBuilt());

		indexedValues.add(2);
		assertFalse("The insertion of new value did not reset isBuilt", indexedValues.isBuilt());

		indexedValues.build();
		assertTrue("The isBuilt value must be true after build", indexedValues.isBuilt());

		indexedValues.add(1.5, 2);
		assertTrue("The repeated insertion has reset isBuilt", indexedValues.isBuilt());

		indexedValues.add(1.5, 3);
		assertFalse("The insertion of new value did not reset isBuilt", indexedValues.isBuilt());

		indexedValues.build();
		assertTrue("The isBuilt value is not true after build", indexedValues.isBuilt());

		indexedValues.clear();
		assertFalse("The isBuilt value is not false after clear", indexedValues.isBuilt());
	}

	@Test
	public void testValuesAreSorted() {

		Double sortedArray[] = { -1.0, 0.5, 1.0, 3.0 };

		indexedValues.add(0.5, 3.0, 1.0, -1.0);

		assertArrayEquals(sortedArray, indexedValues.getValues().toArray());
	}

	@Test
	public void testGetIndexedIntervalBasic() {

		indexedValues.add(-1.0, 0.5, 1.0, 3.0);

		assertEquals(new IntegerInterval(1, 2), indexedValues.getIndexedInterval(0.0, 2.0));
	}

	@Test
	public void testGetIndexedIntervalSinglePoint() {

		indexedValues.add(-1.0, 0.5, 1.0, 3.0);

		assertEquals(new IntegerInterval(1, 1), indexedValues.getIndexedInterval(0.5, 0.5));
		assertEquals(new IntegerInterval(1, 1),
				indexedValues.getIndexedInterval(0.5 - RoutingConstants.EPSILON, 0.5 - RoutingConstants.EPSILON));
		assertEquals(new IntegerInterval(1, 1),
				indexedValues.getIndexedInterval(0.5 + RoutingConstants.EPSILON, 0.5 + RoutingConstants.EPSILON));
	}

	@Test
	public void testGetIndexedIntervalNonExistant() {

		indexedValues.add(-1.0, 0.5, 1.0, 3.0);

		assertNull(indexedValues.getIndexedInterval(0.6, 0.7));
		assertNull(indexedValues.getIndexedInterval(5, 6));
		assertNull(indexedValues.getIndexedInterval(-4, -3));
	}

	@Test
	public void testGetValueByIndex() {

		indexedValues.add(-1.0, 0.5, 1.0, 3.0);

		assertEquals(-1.0, indexedValues.getValueByIndex(0), RoutingConstants.EPSILON);
		assertEquals(0.5, indexedValues.getValueByIndex(1), RoutingConstants.EPSILON);
		assertEquals(1.0, indexedValues.getValueByIndex(2), RoutingConstants.EPSILON);

		indexedValues.add(0.0);

		assertEquals(0.5, indexedValues.getValueByIndex(2), RoutingConstants.EPSILON);
	}
}
