package org.workcraft.plugins.circuit.routing.impl;

import org.workcraft.plugins.circuit.routing.basic.Point;
import org.workcraft.plugins.circuit.routing.basic.RouterConnection;

public class DijkstraRouter extends AbstractRoutingAlgorithm {

    @Override
    protected Route produceRoute(RouterConnection connection) {
        final Route dummyOutput = new Route(connection.source, connection.destination);

        dummyOutput.add(dummyOutput.source.location);

        final Point dummyPoint = new Point(dummyOutput.source.location.x + 2, dummyOutput.source.location.y + 2);
        final Point dummyPoint2 = new Point(dummyOutput.destination.location.x - 2,
                dummyOutput.destination.location.y - 2);

        dummyOutput.add(dummyPoint);
        dummyOutput.add(dummyPoint2);

        dummyOutput.add(dummyOutput.destination.location);

        return dummyOutput;
    }

}
