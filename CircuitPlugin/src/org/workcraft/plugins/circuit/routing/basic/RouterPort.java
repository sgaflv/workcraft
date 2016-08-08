package org.workcraft.plugins.circuit.routing.basic;

/**
 * Class representing a separate port used as a start or end of any route.
 */
public final class RouterPort {
    /** Direction of the port. */
    public final PortDirection direction;
    /** Location of the port. */
    public final Point location;
    /**
     * if true, the router till add route segment in the defined direction
     * before making any turns. If false, the router can choose any neighboring
     * direction on the first route segment.
     */
    public final boolean isOnEdge;

    public RouterPort(PortDirection direction, Point location, boolean isOnEdge) {
        if (direction == null || location == null) {
            throw new IllegalArgumentException();
        }
        this.direction = direction;
        this.location = location;
        this.isOnEdge = isOnEdge;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + (isOnEdge ? 1231 : 1237);
        result = prime * result + ((location == null) ? 0 : location.hashCode());
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
        final RouterPort other = (RouterPort) obj;
        if (direction != other.direction) {
            return false;
        }
        if (isOnEdge != other.isOnEdge) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Port [direction=" + direction + ", location=" + location + ", isOnEdge=" + isOnEdge + "]";
    }

}
