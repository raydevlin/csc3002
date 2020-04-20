package shared.obj;

import shared.net.NetworkInterface;

import java.util.UUID;

public class Connection {

    UUID uuid;
    String ip;
    String pseudo = "client-default-pseudo";
    int port = 80;
    NetworkInterface networkInterface;
    public boolean isDataGathered = false;

    public Connection(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.networkInterface = new NetworkInterface(ip, port);
        this.uuid = UUID.randomUUID();
    }

    public Connection(String ip, int port, String uuid) {
        this.ip = ip;
        this.port = port;
        this.networkInterface = new NetworkInterface(ip, port);
        this.uuid = UUID.fromString(uuid);
    }

    public Connection(String ip, int port, String pseudo, int nonUUIDflag) {
        this.ip = ip;
        this.port = port;
        this.networkInterface = new NetworkInterface(ip, port);
        this.uuid = UUID.randomUUID();
        this.pseudo = pseudo;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() { return port; }

    public UUID getUuid() {
        return uuid;
    }

    public NetworkInterface getNetworkInterface() { return networkInterface; }

    @Override
    public String toString() {
        if(ip==null) return null;
        return ip + ":" + port;
    }
}
