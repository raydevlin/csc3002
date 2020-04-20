package server;

import client.obj.Neighbourhood;
import javafx.concurrent.Task;
import server.ui.UserInterface;
import shared.data.Command;
import shared.data.Direction;
import shared.net.NetworkInterface;
import shared.net.NetworkObject;
import shared.obj.ClientNet;
import shared.obj.Connection;
import shared.obj.ConnectionContainer;
import shared.util.FileReader;
import shared.util.MatrixUtility;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

@SuppressWarnings("StatementWithEmptyBody")
public class Server {

    int width = 32;
    int height = 18;
    int port = 59712;
    int refreshRate = 150;
    boolean running = true;
    int clientPortStart = 61713;
    int clientPortOffset = 0;
    final int maxClientPortOffset = 200;
    int gameDeviceWidth = 2;
    int gameDeviceHeight = 2;
    int maxClients = gameDeviceHeight * gameDeviceWidth;

    Task<Void> serverRunningLoop;
    ConnectionContainer connections;
    ClientNet clientNet;
    ServerSocket server;
    LinkedList<NetworkObject> broadcastRequestQueue;    //  TODO: broadcast request queue implementation
    String dataSource = "src/server/test_data_5.png";

    public Server() {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) { e.printStackTrace(); }
        connections = new ConnectionContainer();
        broadcastRequestQueue = new LinkedList<>();
        clientNet = new ClientNet(gameDeviceWidth,gameDeviceHeight);
        System.out.println("server initialised");
    }

    public void updateServer() {
        try {

            Socket socket = server.accept();
            System.out.println("\nincoming request from " + socket.getRemoteSocketAddress().toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(!in.ready()) { /* stall for connection */ }

            NetworkObject request = NetworkObject.fromSerialisedString(in.readLine());
            request.stripNetSource(socket);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


            //Route to method based on request
            NetworkObject response = null;


            if(request.getCommand() == Command.DEFAULT_CODE) {
                System.out.println("ignoring default command code");
            }

            else if(request.getCommand() == Command.GET_PORT) response = getPort(request);
            else if(request.getCommand() == Command.REGISTER) {
                response = register(request);
            }
            else if(request.getCommand() == Command.GET_DIMENSIONS) response = getDimensions(request);
            else if(request.getCommand() == Command.GET_GAME_DATA) response = getGameData(request);

            //  return network response
            if(response == null)
                response = new NetworkObject(Command.DEFAULT_CODE);
            out.println(response);

            while(!broadcastRequestQueue.isEmpty()) {
                NetworkObject broadcastRequest = broadcastRequestQueue.pop();
                for (Connection connection:connections.toList()) {
                    Socket broadcast = new Socket(connection.getIp(), connection.getPort());
                    PrintWriter broadcastOutput = new PrintWriter(broadcast.getOutputStream(), true);
                    broadcastOutput.println(broadcastRequest);
                    broadcastOutput.close();
                    broadcast.close();
                }
            }

            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    //  response methods
    private NetworkObject register(NetworkObject request) {
        System.out.println("attempting to registering device");
        NetworkObject response;

        if(connections.size() >= maxClients) return new NetworkObject(Command.SERVER_FULL);

        int registrationPort = Integer.parseInt(request.getVariable("port"));

        if(!connections.contains(request.getSourceHost(), registrationPort)) {
            Connection connection = new Connection(request.getSourceHost(), registrationPort);
            clientNet.set(connection, connections.size()%gameDeviceWidth, connections.size()/gameDeviceWidth);
            connections.add(connection);

            response = new NetworkObject(Command.ACKNOWLEDGE);
            response.addVariable("registered","true");
            response.addVariable("uuid", connection.getUuid().toString());
            System.out.println("device registered");

            //if(connections.size() > 1) {
                System.out.println("getting neighbour data");
                Neighbourhood connectionNeighbours = clientNet.getNeighbours(connection);
                connectionNeighbours.print();
                for(int key : connectionNeighbours.keySet()) {
                    new Thread(() -> {
                        try {
                            Socket sendConnectionSocket = new Socket(connectionNeighbours.getNeighbour(key).getConnection().getIp(), connectionNeighbours.getNeighbour(key).getConnection().getPort());
                            NetworkObject setConnection = new NetworkObject(Command.SET_CONNECTION);
                            setConnection.addVariable("ip", connection.getIp());
                            setConnection.addVariable("port", Integer.toString(connection.getPort()));
                            setConnection.addVariable("uuid", connection.getUuid().toString());
                            setConnection.addVariable("direction", Integer.toString(Direction.getComplementaryDirection(key)));

                            PrintWriter sendConnectionOutput = new PrintWriter(sendConnectionSocket.getOutputStream(), true);
                            sendConnectionOutput.println(setConnection);

                            sendConnectionOutput.close();
                            sendConnectionSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                response.addVariable("neighbours", connectionNeighbours.toSerialisedString());
            /*}
            else {
                System.out.println("insufficient connections to provide neighbour data, will update dynamically");
                response.addVariable("neighbours", "empty");
            }*/
        }
        else {
            response = new NetworkObject(Command.MULTIPLE_REGISTRATION);
            response.addVariable("error","connection already registered");
            System.out.println("unable to register device: multiple registration");
        }

        return response;
    }

    private NetworkObject getPort(NetworkObject request) {
        NetworkObject response;

        if(clientPortOffset < maxClientPortOffset) {
            int clientPort = clientPortStart + (clientPortOffset++);
            System.out.println("serving client port: " + clientPort);
            response = new NetworkObject(Command.SET_PORT);
            response.addVariable("port",Integer.toString(clientPort));
        }
        else {
            System.out.println("unable to serve client port! port overflow!");
            response = new NetworkObject(Command.PORT_OVERFLOW);
        }

        return response;
    }

    private NetworkObject getDimensions(NetworkObject request) {
        System.out.println("sending game dimensions to client");
        NetworkObject response = new NetworkObject(Command.SET_DIMENSIONS);
        response.addVariable("width",Integer.toString(width));
        response.addVariable("height",Integer.toString(height));
        response.addVariable("refreshRate", Integer.toString(refreshRate));
        return response;
    }

    private NetworkObject getGameData(NetworkObject request) {
        System.out.println("sending game data to client");
        NetworkObject response = getDimensions(request);
        FileReader fileReader = new FileReader();
        int[][] data = fileReader.parseFile(dataSource);
        response.addVariable("data", MatrixUtility.toString(data));
        return  response;
    }

    public void update() {
        if(connections.size() < maxClients) {
            System.out.println("client net not full, unable to update");
            return;
        }
        NetworkObject updateCall = new NetworkObject(Command.UPDATE);
        for(Connection connection : connections.toList()) {
            Socket updateSocket = null;
            try {
                updateSocket = new Socket(connection.getIp(), connection.getPort());
                PrintWriter updateOutput = new PrintWriter(updateSocket.getOutputStream(), true);
                updateOutput.println(updateCall);
                updateOutput.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //  utility methods
    private boolean isRegistered(NetworkObject request) {
        return request.getVariable("uuid") != null && connections.contains(request.getVariable("uuid"));
    }


    //  update loop
    private void run() {
        System.out.println("server running");
        while(running) {
            System.out.println("running");
            updateServer();
        }
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*serverRunningLoop = new Task<>() {
            @Override public Void call() {
                while (running) {
                    if (isCancelled()) {
                        System.out.println("thread cancelled");
                        break;
                    }
                    updateServer();
                }
                return null;
            }
        };
        new Thread(serverRunningLoop).start();*/
    }


    //  start server
    public static void main(String[] args) {
        Server server = new Server();
        UserInterface ui = new UserInterface(server);
        server.run();
    }
}
