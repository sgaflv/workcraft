package org.workcraft.plugins.circuit.routing.basic;

public class Coordinate implements Comparable<Coordinate> {
	public final CoordinateOrientation orientation;
	public final boolean isPublic;
	public final double value;

	public Coordinate(CoordinateOrientation orientation, boolean isPublic, double value) {
		this.orientation = orientation;
		this.isPublic = isPublic;
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isPublic ? 1231 : 1237);
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (isPublic != other.isPublic)
			return false;
		if (orientation != other.orientation)
			return false;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}

	@Override
	public int compareTo(Coordinate other) {
		return Double.compare(this.value, other.value);
	}
}
