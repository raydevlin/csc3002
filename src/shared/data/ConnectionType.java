package shared.data;

import java.util.ArrayList;

public enum ConnectionType {

    PEER_TO_PEER(0, "Peer to Peer"), CLIENT_SERVER(1, "Client-Server");

    int value = -1;
    String desc = "null";

    private ConnectionType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int toValue() { return value; }

    public String toString() { return desc; }

    public static ArrayList<ConnectionType> getConnectionTypes() {
        ArrayList<ConnectionType> list = new ArrayList();
        list.add(ConnectionType.PEER_TO_PEER);
        list.add(ConnectionType.CLIENT_SERVER);
        return list;
    }

}
