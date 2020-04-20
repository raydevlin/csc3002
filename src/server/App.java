package server;

import client.obj.Neighbourhood;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import server.ui.DirectConnectController;
import server.ui.ServerViewController;
import shared.data.Command;
import shared.data.Direction;
import shared.data.StaticData;
import shared.net.IpChecker;
import shared.net.NetworkObject;
import shared.obj.ClientNet;
import shared.obj.Connection;
import shared.obj.ConnectionContainer;
import shared.util.FileReader;
import shared.util.MatrixUtility;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

public class App extends Application {

    int width = 32;
    int height = 18;
    int port = 59712;
    int refreshRate = 500;
    boolean running = true;
    int clientPortStart = 61713;
    int clientPortOffset = 0;
    final int maxClientPortOffset = 200;
    int gameDeviceWidth = 2;
    int gameDeviceHeight = 1;
    int maxClients = gameDeviceHeight * gameDeviceWidth;
    boolean getDataFromServer = false;
    int[][] gameData = null;

    ServerViewController serverViewController;
    Task<Void> serverRunningLoop;
    ConnectionContainer connections;
    ClientNet clientNet;
    ServerSocket server;
    LinkedList<NetworkObject> broadcastRequestQueue;    //  TODO: broadcast request queue implementation
    HashMap<String, DirectConnectController> directConnectArchive;
    Stage stage;
    String defaultDataSource = "src/server/test_data_5.png";


    @Override
    public void start(Stage stage) throws Exception {
        initialiseUI(stage);
    }

    public void startServer() {
        try {
            server = new ServerSocket(port);
            serverViewController.serverInitialised();
            serverViewController.log("Server initialised");
        } catch (IOException e) {
            e.printStackTrace();
            serverViewController.serverNotInitialised();
        }

        connections = new ConnectionContainer();
        broadcastRequestQueue = new LinkedList<>();
        directConnectArchive = new HashMap<>();
        clientNet = new ClientNet(gameDeviceWidth,gameDeviceHeight);
        System.out.println("server initialised");
        run();
    }


    private void initialiseUI(Stage stage) {
        this.stage = stage;
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui/ServerView.fxml"));
            root = fxmlLoader.load();
            serverViewController = (ServerViewController)fxmlLoader.getController();
            serverViewController.setServer(this);
            //Platform.runLater(() -> serverViewController.setMaxConnectionsLabel(maxClients));
            stage.setTitle("Game of Life - Server");
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    System.out.println("CLOSING");
                    System.exit(0);
                }
            });
            stage.show();
        } catch (IOException e) {
            System.out.println("Error happened here");
            e.printStackTrace();
        }

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
            else if(request.getCommand() == Command.GET_DATA || request.getCommand() == Command.RECEIVE_DATA) {
                NetworkObject forwardedRequest = new NetworkObject(request);
                try {
                    Socket forwardSocket = new Socket(request.getVariable("recipient"), StaticData.clientPort);
                    PrintWriter forwardOutput = new PrintWriter(forwardSocket.getOutputStream(), true);
                    forwardOutput.println(forwardedRequest);
                    forwardOutput.close();
                    forwardSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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

    public void shutdownServer() {
        serverViewController.log("Shutting down server");

        // TODO  send message to all clients
        serverRunningLoop.cancel();

        try {
            this.server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  response methods
    private NetworkObject register(NetworkObject request) {
        System.out.println("attempting to registering device");
        NetworkObject response;

        if(connections.size() >= maxClients) return new NetworkObject(Command.SERVER_FULL);

        int registrationPort = Integer.parseInt(request.getVariable("port"));
        String pseudo = request.getVariable("pseudo");

        if(!connections.contains(request.getSourceHost(), registrationPort)) {
            Connection connection = new Connection(request.getSourceHost(), registrationPort, pseudo, -1);
            clientNet.set(connection, connections.size()%gameDeviceWidth, connections.size()/gameDeviceWidth);
            connections.add(connection);

            response = new NetworkObject(Command.ACKNOWLEDGE);
            response.addVariable("registered","true");
            response.addVariable("uuid", connection.getUuid().toString());
            System.out.println("device registered");

            if(directConnectArchive.containsKey(request.getSourceHost())) {
                directConnectArchive.get(request.getSourceHost()).successfulConnection();
                directConnectArchive.remove(request.getSourceHost());
            }

            System.out.println("getting neighbour data");
            Neighbourhood connectionNeighbours = clientNet.getNeighbours(connection);
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
            try {
                Platform.runLater(() -> {
                    serverViewController.setNoOfConnections(connections.size());
                    serverViewController.log(request.getSourceHost() + " connected to server");
                });
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
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
        if(this.gameData == null) loadGameData(defaultDataSource);
        response.addVariable("data", MatrixUtility.toString(this.gameData));
        return response;
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

    public void directConnect(String ipAddress, String message, DirectConnectController archivableController) throws IOException {

        if(directConnectArchive.containsKey(ipAddress)) return;
        directConnectArchive.put(ipAddress, archivableController);

        Socket directConnectionSocket = new Socket(ipAddress, StaticData.clientPort);
        NetworkObject directConnect = new NetworkObject(Command.DIRECT_CONNECT);
        directConnect.addVariable("ip", IpChecker.getLocalIP());
        directConnect.addVariable("message", message);

        PrintWriter directConnectionOutput = new PrintWriter(directConnectionSocket.getOutputStream(), true);
        directConnectionOutput.println(directConnect);

        directConnectionOutput.close();
        directConnectionSocket.close();

    }

    public void updateNeighbours() {
        for(Connection connection : connections.toList()) sendNeighbourRequest(connection);
    }

    private void sendNeighbourRequest(Connection connection) {
        Neighbourhood connectionNeighbours = clientNet.getNeighbours(connection);
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
    }

    public void loadGameData(String path) {
        loadGameData(new File(path));
    }

    public void loadGameData(File file) {
        FileReader fileReader = new FileReader();
        int[][] data = fileReader.parseFile(file);
        if(data != null) {
            this.gameData = data;
            if(this.height != data.length) {
                this.height = data.length;
                serverViewController.setHeight(data.length);
            }
            if(this.width != data[0].length) {
                this.width = data[0].length;
                serverViewController.setWidth(data[0].length);
            }
        }
    }

    //  update loop
    private void run() {
        System.out.println("server running");
        serverViewController.log("Server running");
        serverRunningLoop = new Task<Void>() {
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
        new Thread(serverRunningLoop).start();
    }

    public void test_sendRequest() {
        System.out.println("Sending test request");
        serverViewController.log("Sending Pre-Start request to connected clients");
        for(Connection client : connections.toList()) {
            try {
                Socket testSocket = new Socket(client.getIp(), client.getPort());
                NetworkObject testObject = new NetworkObject(Command.PRE_START);
                testObject.addVariable("source", "server.App.test_sendRequest()");

                PrintWriter testWriter = new PrintWriter(testSocket.getOutputStream(), true);
                testWriter.println(testObject);
                testWriter.close();
                testSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printClientNet() {
        clientNet.print();
    }

    public ClientNet getClientNet() {
        return this.clientNet;
    }

    public Stage getStage() {
        return this.stage;
    }

    //  start server
    public static void main(String[] args) {
        launch(args);
    }



}
