package shared.obj;

public class ClientNetEntity {

    private Connection connection;
    private Coordinate coordinate;

    public ClientNetEntity(Connection connection, Coordinate coordinate) {
        this.connection = connection;
        this.coordinate = coordinate;
    }

    public Connection getConnection() {
        return connection;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public String toString() {
        return "ClientNetEntity{" +
                "connection=" + connection +
                ", coordinate=" + coordinate +
                '}';
    }
}
