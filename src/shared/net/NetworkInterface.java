package shared.net;

import shared.data.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkInterface {

    String host;
    int port;
    Socket socket;

    public NetworkInterface(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public NetworkObject GET(NetworkObject request) throws IOException {
        NetworkObject response = new NetworkObject(Command.DEFAULT_CODE);
        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
        out.println(request);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        response = NetworkObject.fromSerialisedString(in.readLine());
        out.close();
        in.close();
        return response;
    }

}
