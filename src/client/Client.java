/*
package client;

import client.obj.DataStore;
import client.obj.Neighbour;
import client.obj.Neighbourhood;
import client.obj.State;
import client.ui.GameBoard;
import client.ui.UserInterface;
import shared.data.Command;
import shared.data.Direction;
import shared.data.StaticData;
import shared.net.NetworkInterface;
import shared.net.NetworkObject;
import shared.obj.Connection;
import shared.util.Alignment;
import shared.util.FileReader;
import shared.util.MatrixUtility;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("StatementWithEmptyBody")
public class Client {

    int[][] data;
    int generation = 0;
    int maxGenerations = 1000;
    int width;
    int height;
    int refreshRate = -1;
    int localPort = -1;
    long updateStartTime = 0;
    boolean running = true;
    boolean doUpdateContinuously = true;
    boolean doInfiniteWrap = true;
    boolean getDataLocally = true;
    String dataSource = "src/client/test_data_5.png";
    String uuid;

    Thread updateThread;
    State state = State.INITIALISING;
    NetworkInterface serverConnection;
    ServerSocket peerToPeerServer;
    Neighbourhood neighbourhood;
    DataStore dataStore;
    JFrame settingsGUI;
    GameBoard gameGUI;


    public Client() {
        serverConnection = new NetworkInterface(StaticData.serverHost, StaticData.serverPort);
        dataStore = new DataStore();

        settingsGUI = new JFrame();
        settingsGUI.setTitle("Client");
        settingsGUI.setSize(400, 80);

        JPanel panel = new JPanel();
        JButton button = new JButton("Start");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //setVisible(false);
                //dispose();
                start();
            }
        });
        panel.add(button);
        settingsGUI.getContentPane().add(panel);

        settingsGUI.setVisible(true);
        settingsGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //start();
        initialisePort();
        if(localPort == -1) return;
        initialiseLocalServer();
        initialiseData();
        initialiseUI();
        run();
    }

    public void start() {
        initialisePort();
        if(localPort == -1) return;
        initialiseLocalServer();
        initialiseData();
        initialiseUI();
        run();
    }

    private void initialisePort() {
        NetworkObject getPort = new NetworkObject(Command.GET_PORT);
        getPort.addVariable("test","val");
        NetworkObject getPortResponse = serverConnection.GET(getPort);
        if(getPortResponse.getCommand() == Command.SET_PORT) {
            System.out.println("received port " + getPortResponse.getVariable("port") + " from server");
            this.localPort = Integer.parseInt(getPortResponse.getVariable("port"));
        }
        else {
            System.out.println("error, unable to receive port from server");
        }
    }

    private void initialiseLocalServer() {
        try { peerToPeerServer = new ServerSocket(localPort); }
        catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("client peer to peer server running over localhost on port " + localPort);

        NetworkObject register = new NetworkObject(Command.REGISTER);
        register.addVariable("port", Integer.toString(localPort));
        NetworkObject registerResponse = serverConnection.GET(register);
        if(registerResponse.getCommand() == Command.ACKNOWLEDGE) {
            state = State.REST_API;
            uuid = registerResponse.getVariable("uuid");
            System.out.println("client registered with server");
            if(!"empty".equals(registerResponse.getVariable("neighbours"))) {
                System.out.println("getting neighbour data");
                neighbourhood = Neighbourhood.fromSerialisedString(registerResponse.getVariable("neighbours"));
                neighbourhood.print();
            }
            else {
                neighbourhood = new Neighbourhood();
            }
        }
        else {
            System.out.println("unable to register with server, shutting down");
            System.exit(0);
        }
    }

    private void initialiseData() {
        if(getDataLocally) {
            NetworkObject getDimensions = new NetworkObject(Command.GET_DIMENSIONS);
            NetworkObject getDimensionsResponse = serverConnection.GET(getDimensions);
            this.width = Integer.parseInt(getDimensionsResponse.getVariable("width"));
            this.height = Integer.parseInt(getDimensionsResponse.getVariable("height"));
            FileReader fileReader = new FileReader();
            this.data = fileReader.parseFile(dataSource);
            this.refreshRate = Integer.parseInt(getDimensionsResponse.getVariable("refreshRate"));
        } else {
            NetworkObject getGameData = new NetworkObject(Command.GET_GAME_DATA);
            NetworkObject getGameDataResponse = serverConnection.GET(getGameData);
            this.width = Integer.parseInt(getGameDataResponse.getVariable("width"));
            this.height = Integer.parseInt(getGameDataResponse.getVariable("height"));
            this.data = MatrixUtility.matrixFromString(getGameDataResponse.getVariable("data"));
            this.refreshRate = Integer.parseInt(getGameDataResponse.getVariable("refreshRate"));
        }
    }

    private void initialiseUI() {
        gameGUI = new GameBoard();
        gameGUI.setData(data);
    }

    public void run() {
        System.out.println("client running loop called");
        while(running) {
            printState();
            if(state == State.REST_API) updateServer();
            if(state == State.GAME) updateGame();
            if(state == State.RENDER) updateUI();
        }
    }

    private void updateServer() {
        try {
            Socket socket = peerToPeerServer.accept();
            System.out.println("\nincoming request from " + socket.getRemoteSocketAddress().toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(!in.ready()) { */
/* stall for connection *//*
 }

            NetworkObject request = NetworkObject.fromSerialisedString(in.readLine());
            request.stripNetSource(socket);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //Route to method based on request
            NetworkObject response = new NetworkObject(Command.ACKNOWLEDGE);
            boolean doGenericResponse = true;   //  allow individual methods to control whether to write to custom source or not
            if(request.getCommand() == Command.SET_CONNECTION) {
                System.out.println("setting connection");

                String ip = request.getVariable("ip");
                int port = Integer.parseInt(request.getVariable("port"));
                String uuid = request.getVariable("uuid");
                int direction = Integer.parseInt(request.getVariable("direction"));

                Connection connection = new Connection(ip, port, uuid);
                Neighbour neighbour = new Neighbour(connection, direction);
                neighbourhood.setNeighbour(neighbour);
                neighbourhood.print();
            }
            if(request.getCommand() == Command.UPDATE) {
                this.state = State.GAME;
            }
            if(request.getCommand() == Command.GET_DATA) {
                returnData(request);
                doGenericResponse = false;
            }
            if(request.getCommand() == Command.RECEIVE_DATA) {
                receiveData(request);
            }
            if(request.getCommand() == Command.OUT_OF_SYNC) {
                int direction = Integer.parseInt(request.getVariable("direction"));
                new Thread(() -> {
                    try {
                        Thread.sleep(20);
                        NetworkObject retryGetData = new NetworkObject(Command.GET_DATA);
                        retryGetData.addVariable("generation", Integer.toString(generation));
                        retryGetData.addVariable("direction", Integer.toString(Direction.getComplementaryDirection(direction)));
                        Connection connection = neighbourhood.getNeighbour(direction).getConnection();
                        Socket retryGetDataSocket = new Socket(connection.getIp(), connection.getPort());
                        PrintWriter retryGetDataOutput = new PrintWriter(retryGetDataSocket.getOutputStream(), true);
                        retryGetDataOutput.println(retryGetData);
                        retryGetDataOutput.close();
                        retryGetDataSocket.close();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            if(doGenericResponse)out.println(response);

            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //  check if ready to update game after every server update
        //  if ready switch state to GAME
    }

    private void updateGame() {
        updateStartTime = System.currentTimeMillis();
        for(Neighbour neighbour : neighbourhood.toList()) {
            new Thread(() -> {
                NetworkObject dataGET = new NetworkObject(Command.GET_DATA);
                dataGET.addVariable("generation", Integer.toString(generation));
                dataGET.addVariable("direction", Integer.toString(Direction.getComplementaryDirection(neighbour.getDirection())));
                NetworkInterface clientInterface = new NetworkInterface(neighbour.getConnection().getIp(), neighbour.getConnection().getPort());
                NetworkObject dataGETResponse = clientInterface.GET(dataGET);
            }).start();
        }
        this.state = State.REST_API;
    }

    public void returnData(NetworkObject request) {
        NetworkObject response = new NetworkObject(Command.DEFAULT_CODE);
        int direction = Integer.parseInt(request.getVariable("direction"));
        int requestedGeneration = Integer.parseInt(request.getVariable("generation"));
        response.addVariable("direction", Integer.toString(Direction.getComplementaryDirection(direction)));
        if(requestedGeneration > this.generation) {
            response.setCommand(Command.OUT_OF_SYNC);
        }
        else {
            response.setCommand(Command.RECEIVE_DATA);
            int[][] workingData;
            if(requestedGeneration < this.generation) workingData = dataStore.getGeneration(requestedGeneration);
            else workingData = this.data;

            if(direction == Direction.NORTH) {
                int[] row = MatrixUtility.getFirstRow(workingData);
                response.addVariable("data", MatrixUtility.toString(row));
            }
            else if(direction == Direction.SOUTH) {
                int[] row = MatrixUtility.getLastRow(workingData);
                response.addVariable("data", MatrixUtility.toString(row));
            }
            else if(direction == Direction.WEST) {
                int[] col = MatrixUtility.getFirstCol(workingData);
                response.addVariable("data", MatrixUtility.toString(col));
            }
            else if(direction == Direction.EAST) {
                int[] col = MatrixUtility.getLastCol(workingData);
                response.addVariable("data", MatrixUtility.toString(col));
            }
            else if(direction == Direction.NORTH_WEST) {
                int pixel = MatrixUtility.getFirstRow(workingData)[0];
                response.addVariable("data", Integer.toString(pixel));
            }
            else if(direction == Direction.NORTH_EAST) {
                int pixel = MatrixUtility.getFirstRow(workingData)[width-1];
                response.addVariable("data", Integer.toString(pixel));
            }
            else if(direction == Direction.SOUTH_EAST) {
                int pixel = MatrixUtility.getLastRow(workingData)[width-1];
                response.addVariable("data", Integer.toString(pixel));
            }
            else if(direction == Direction.SOUTH_WEST) {
                int pixel = MatrixUtility.getLastRow(workingData)[0];
                response.addVariable("data", Integer.toString(pixel));
            }
        }
        Neighbour recipient = neighbourhood.getNeighbour(direction);
        System.out.println("sending data to " + recipient.getConnection());
        try {
            Socket dataSocket = new Socket(recipient.getConnection().getIp(), recipient.getConnection().getPort());
            PrintWriter dataOutput = new PrintWriter(dataSocket.getOutputStream(), true);
            dataOutput.println(response);
            dataOutput.close();
            dataSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveData(NetworkObject request) {
        System.out.println("receiving data");
        int direction = Integer.parseInt(request.getVariable("direction"));
        if(direction % 2 == 0) {    //  CARDINAL DIRECTION
            int[] transmittedData = MatrixUtility.arrayFromString(request.getVariable("data"));
            dataStore.setData(direction, transmittedData);
        }
        else {                      //  INTER-CARDINAL DIRECTION
            int transmittedData = Integer.parseInt(request.getVariable("data"));
            dataStore.setData(direction, transmittedData);
        }
        int[] transmittedData = MatrixUtility.arrayFromString(request.getVariable("data"));
        dataStore.setData(direction, transmittedData);
        neighbourhood.getNeighbour(direction).getConnection().isDataGathered = true;
        tryUpdateData();
    }

    public void tryUpdateData() {
        int isDataCollected = 0;
        for(Neighbour neighbour : neighbourhood.toList()) {
            if(neighbour.getConnection().isDataGathered) isDataCollected ++;
        }
        if(isDataCollected == neighbourhood.keySet().size()) {
            for(Neighbour neighbour : neighbourhood.toList()) {
                neighbour.getConnection().isDataGathered = false;
            }
            updateData();
        }
    }

    private void updateData() {
        System.out.println("updating data for generation " + generation);
        int[][] workingGeneration = MatrixUtility.pad2dArray(data, 1, 0);
        int workingWidth = workingGeneration[0].length;
        int workingHeight =workingGeneration.length;
        int[][] nextData = new int[height][width];
        for(Integer key : neighbourhood.keySet()) {          //  CARDINAL DIRECTION
            int[] receivedData = dataStore.getData(key);
            if(key == Direction.NORTH) workingGeneration = MatrixUtility.setRow(workingGeneration, 0, receivedData, Alignment.CENTER);
            else if(key == Direction.EAST) workingGeneration = MatrixUtility.setCol(workingGeneration, workingWidth-1, receivedData, Alignment.CENTER);
            else if(key == Direction.SOUTH) workingGeneration = MatrixUtility.setRow(workingGeneration, workingHeight-1, receivedData, Alignment.CENTER);
            else if(key == Direction.WEST) workingGeneration = MatrixUtility.setCol(workingGeneration, 0, receivedData, Alignment.CENTER);
        }
        for(Integer key : neighbourhood.keySet()) {                      //  INTER-CARDINAL DIRECTION
            int receivedData = dataStore.getIntData(key);
            if(key == Direction.NORTH_EAST) workingGeneration[0][workingWidth-1] = receivedData;
            else if(key == Direction.SOUTH_EAST) workingGeneration[workingHeight-1][workingWidth-1] = receivedData;
            else if(key == Direction.SOUTH_WEST) workingGeneration[workingHeight-1][0] = receivedData;
            else if(key == Direction.NORTH_WEST) workingGeneration[0][0] = receivedData;
        }
        int DETECTION_RADIUS = 1;
        int UNDERPOPULATION_THRESHOLD = 2;
        int OVERPOPULATION_THRESHOLD = 3;
        int LAZARUS_VALUE = 3;
        for(int y = DETECTION_RADIUS; y < workingGeneration.length - DETECTION_RADIUS; y ++) {
            for(int x = DETECTION_RADIUS; x < workingGeneration[y].length - DETECTION_RADIUS; x++){
                int sum = MatrixUtility.sum2dArray(MatrixUtility.getNeighbours(workingGeneration, DETECTION_RADIUS, y, x)) - workingGeneration[y][x];
                if((workingGeneration[y][x] == 1 && (sum >= UNDERPOPULATION_THRESHOLD && sum <= OVERPOPULATION_THRESHOLD))
                        || (workingGeneration[y][x] == 0 && sum == LAZARUS_VALUE))
                    nextData[y-DETECTION_RADIUS][x-DETECTION_RADIUS] = 1;
            }
        }
        dataStore.saveGeneration(generation, data);
        generation++;
        data = nextData;
        this.state = State.RENDER;
        if(doUpdateContinuously && generation <= maxGenerations) {
            long updateSleepTime = (refreshRate - (System.currentTimeMillis() - updateStartTime));
            if(updateSleepTime < 0) {
                System.out.println("\nWARNING: client unable to keep up with update speed!\n");
                updateUI();
                this.state = State.GAME;
            }
            else {
                updateThread = new Thread(() -> {
                    try {
                        System.out.println("sleeping for: " + updateSleepTime);
                        Thread.sleep(updateSleepTime);
                        System.out.println("waking for update");
                        updateGame();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                updateThread.start();
            }
        }
    }

    public void printState() {
        System.out.println("State: " + state);
    }

    private void updateUI() {
        gameGUI.setData(data);
        this.state = State.REST_API;
    }

    public static void main(String[] args) {
        Client client = new Client();
        UserInterface clientUI = new UserInterface(client);
    }


}
*/
