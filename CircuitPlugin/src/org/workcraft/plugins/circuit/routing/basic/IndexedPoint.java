package org.workcraft.plugins.circuit.routing.basic;

public final class IndexedPoint implements Comparable<IndexedPoint> {

    private static final int CACHE_SIZE = 50;

    private static final int LOW_BITS = 15;

    private static final IndexedPoint[][] pointsCache = new IndexedPoint[CACHE_SIZE][CACHE_SIZE];

    public final int x;
    public final int y;
    public final int hash;

    private IndexedPoint(int x, int y) {
        if (x < 0 || x >= (1 << LOW_BITS) || y < 0 || y >= (1 << LOW_BITS)) {
            throw new IllegalArgumentException("x or y are outside acceptable boundaries");
        }
        this.x = x;
        this.y = y;
        hash = (x << LOW_BITS) + y;
    }

    /**
     * Returns indexed point. For small x and y values, the cached version is
     * re-used.
     *
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return indexed point
     */
    public static IndexedPoint create(int x, int y) {
        if (x < 0 || x >= CACHE_SIZE || y < 0 || y >= CACHE_SIZE) {
            return new IndexedPoint(x, y);
        }

        if (pointsCache[x][y] == null) {
            pointsCache[x][y] = new IndexedPoint(x, y);
        }

        return pointsCache[x][y];
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedPoint other = (IndexedPoint) obj;
        return hash == other.hash;
    }

    @Override
    public String toString() {
        return "IndexedPoint [x=" + x + ", y=" + y + "]";
    }

    @Override
    public int compareTo(IndexedPoint other) {
        return hash - other.hash;
    }

}
