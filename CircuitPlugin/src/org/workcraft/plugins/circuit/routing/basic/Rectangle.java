package org.workcraft.plugins.circuit.routing.basic;

public final class Rectangle {
	public final double x;
	public final double y;
	public final double width;
	public final double height;

	public Rectangle(double x, double y, double width, double height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(width);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
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
		Rectangle other = (Rectangle) obj;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (Double.doubleToLongBits(width) != Double.doubleToLongBits(other.width))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Rectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
	}

	public Line getPortSegment(Point location) {
		double dx = 0;
		double dy = 0;

		if (location.x < x) {
			dx = x - location.x;
		}

		if (location.x > x + width) {
			dx = x + width - location.x;
		}

		if (location.y < y) {
			dy = y - location.y;
		}

		if (location.y > y + height) {
			dy = y + height - location.y;
		}

		if (dx == 0 && dy == 0) {
			return null;
		}

		return new Line(location.x, location.y, location.x + dx, location.y + dy);
	}

	public Rectangle merge(Rectangle other) {

		double x1, x2, y1, y2;
		x1 = Math.min(x, other.x);
		x2 = Math.max(x + width, other.x + other.width);
		y1 = Math.min(y, other.y);
		y2 = Math.max(y + height, other.y + other.height);

		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}

	public boolean intersects(Rectangle other) {
		boolean intersectsH = (x <= other.x + other.width) && (other.x <= x + width);
		boolean intersectsV = (y <= other.y + other.height) && (other.y <= y + height);
		return intersectsH && intersectsV;
	}

	public double middleH() {
		return x + width / 2;
	}

	public double middleV() {
		return y + height / 2;
	}
}
