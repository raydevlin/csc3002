package client.obj;

import shared.data.Direction;
import shared.obj.Connection;

public class Neighbour {

    Connection connection;
    int direction;

    public Neighbour(Connection connection, int direction) {
        this.connection = connection;
        this.direction = direction;
    }

    public Connection getConnection() {
        return connection;
    }

    public int getDirection() {
        return direction;
    }
}
