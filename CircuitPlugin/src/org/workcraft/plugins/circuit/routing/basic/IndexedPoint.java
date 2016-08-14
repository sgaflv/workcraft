package org.workcraft.plugins.circuit.routing.basic;

public class IndexedPoint implements Comparable<IndexedPoint> {

    private static final int CACHE_SIZE = 50;

    private static final IndexedPoint[][] pointsCache = new IndexedPoint[CACHE_SIZE][CACHE_SIZE];

    public final int x;
    public final int y;

    private IndexedPoint(int x, int y) {
        this.x = x;
        this.y = y;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
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
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IndexedPoint [x=" + x + ", y=" + y + "]";
    }

    @Override
    public int compareTo(IndexedPoint other) {
        int compare = Integer.compare(x, other.x);
        if (compare != 0) {
            return compare;
        }

        compare = Integer.compare(y, other.y);

        return compare;
    }

}
