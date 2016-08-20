package org.workcraft.plugins.circuit.routing.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.workcraft.plugins.circuit.routing.basic.Coordinate;
import org.workcraft.plugins.circuit.routing.basic.CoordinateOrientation;
import org.workcraft.plugins.circuit.routing.basic.IndexedInterval;
import org.workcraft.plugins.circuit.routing.basic.RouterConstants;

public class IndexedValuesTest {

    IndexedCoordinates indexedValues = new IndexedCoordinates();

    @Test
    public void testIsBuilt() {
        assertFalse("The Initial isBuilt is not false", indexedValues.isBuilt());

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, 1.5);
        assertFalse("The isBuilt value must be false on adding", indexedValues.isBuilt());

        indexedValues.build();
        assertTrue("The isBuilt value must be true after build", indexedValues.isBuilt());

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, 1.5);
        assertTrue("The repeated insertion has reset isBuilt", indexedValues.isBuilt());

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, 2);
        assertFalse("The insertion of new value did not reset isBuilt", indexedValues.isBuilt());

        indexedValues.build();
        assertTrue("The isBuilt value must be true after build", indexedValues.isBuilt());

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, 1.5, 2);
        assertTrue("The repeated insertion has reset isBuilt", indexedValues.isBuilt());

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, 1.5, 3);
        assertFalse("The insertion of new value did not reset isBuilt", indexedValues.isBuilt());

        indexedValues.build();
        assertTrue("The isBuilt value is not true after build", indexedValues.isBuilt());

        indexedValues.clear();
        assertFalse("The isBuilt value is not false after clear", indexedValues.isBuilt());
    }

    @Test
    public void testValuesAreSorted() {

        final Double[] sortedArray = {-1.0, 0.5, 1.0, 3.0};

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, 0.5, 3.0, 1.0, -1.0);

        int i = 0;
        for (final Coordinate coordinate : indexedValues.getValues()) {
            assertEquals(sortedArray[i++], coordinate.getValue(), RouterConstants.EPSILON);
        }

    }

    @Test
    public void testGetIndexedIntervalBasic() {

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, -1.0, 0.5, 1.0, 3.0);

        assertEquals(new IndexedInterval(1, 2), indexedValues.getIndexedInterval(0.0, 2.0));
    }

    @Test
    public void testGetIndexedIntervalSinglePoint() {

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, -1.0, 0.5, 1.0, 3.0);

        assertEquals(new IndexedInterval(1, 1), indexedValues.getIndexedInterval(0.5, 0.5));
        assertEquals(new IndexedInterval(1, 1),
                indexedValues.getIndexedInterval(0.5 - RouterConstants.EPSILON, 0.5 - RouterConstants.EPSILON));
        assertEquals(new IndexedInterval(1, 1),
                indexedValues.getIndexedInterval(0.5 + RouterConstants.EPSILON, 0.5 + RouterConstants.EPSILON));
    }

    @Test
    public void testGetIndexedIntervalNonExistant() {

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, -1.0, 0.5, 1.0, 3.0);

        assertNull(indexedValues.getIndexedInterval(0.6, 0.7));
        assertNull(indexedValues.getIndexedInterval(5, 6));
        assertNull(indexedValues.getIndexedInterval(-4, -3));
    }

    @Test
    public void testPublicValues() {

        indexedValues.addPrivate(CoordinateOrientation.ORIENT_BOTH, 0, 1, 2);
        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, -1, 1, 3);

        assertTrue(indexedValues.isPublic(-1));
        assertTrue(indexedValues.isPublic(1));
        assertTrue(indexedValues.isPublic(3));

        assertFalse(indexedValues.isPublic(0));
        assertFalse(indexedValues.isPublic(2));

        indexedValues.clear();
        assertFalse(indexedValues.isPublic(-1));
        assertFalse(indexedValues.isPublic(1));
        assertFalse(indexedValues.isPublic(3));
    }

    @Test
    public void testGetValueByIndex() {

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, -1.0, 0.5, 1.0, 3.0);

        assertEquals(-1.0, indexedValues.getValueByIndex(0), RouterConstants.EPSILON);
        assertEquals(0.5, indexedValues.getValueByIndex(1), RouterConstants.EPSILON);
        assertEquals(1.0, indexedValues.getValueByIndex(2), RouterConstants.EPSILON);

        indexedValues.addPublic(CoordinateOrientation.ORIENT_BOTH, 0.0);

        assertEquals(0.5, indexedValues.getValueByIndex(2), RouterConstants.EPSILON);
    }
}
