package shared.net;

import java.io.PrintWriter;
import java.net.ServerSocket;

public class ServerInterface {

    ServerSocket server;

    public ServerInterface(ServerSocket server) {
        this.server = server;
    }

}
