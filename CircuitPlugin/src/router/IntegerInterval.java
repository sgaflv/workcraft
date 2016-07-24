package router;

public class IntegerInterval {
	final public int from;
	final public int to;
	
	public IntegerInterval(int from, int to) {
		this.from = Math.min(from, to);
		this.to = Math.max(from, to);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
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
		IntegerInterval other = (IntegerInterval) obj;
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}
	
	public boolean intersects(IntegerInterval other) {
		return this.from <= other.to && this.to >= other.from;
	}
}