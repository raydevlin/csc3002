package shared.obj;

import java.util.ArrayList;

public class ConnectionContainer {

    ArrayList<Connection> connections;
    int maxConnections = 4;

    public ConnectionContainer() {
        connections = new ArrayList<>();
    }

    public boolean add(Connection connection) {
        return connections.add(connection);
    }

    public boolean remove(Connection connection) {
        return connections.remove(connection);
    }

    public Connection remove(int i) {
        return connections.remove(i);
    }

    public int size() {
        return connections.size();
    }

    public boolean atCapacity() { return connections.size() >= maxConnections; }

    public boolean contains(String ip, int port) {
        for (Connection conn : connections) {
            if(conn.ip.equals(ip) && conn.port == port) return true;
        }
        return false;
    }

    public boolean contains(String uuid) {
        for (Connection conn : connections) {
            if(conn.getUuid().toString().equals(uuid)) return true;
        }
        return false;
    }

    public boolean contains(Connection connection) {
        for (Connection conn : connections) {
            if(conn.uuid.equals(connection.uuid)) return true;
        }
        return false;
    }

    public ArrayList<Connection> toList() { return this.connections; }

}

