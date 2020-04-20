package client;

import client.obj.DataStore;
import client.obj.Neighbour;
import client.obj.Neighbourhood;
import client.obj.State;
import client.ui.ClientViewController;
import client.ui.DirectConnectConfirmationViewController;
import client.ui.ResizableCanvas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import shared.data.*;
import shared.net.IpChecker;
import shared.net.NetworkInterface;
import shared.net.NetworkObject;
import shared.obj.Connection;
import shared.util.Alignment;
import shared.util.FileReader;
import shared.util.MatrixUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class App extends Application {

    int[][] data;
    int generation = 0;
    int maxGenerations = 40;
    int width;
    int height;
    int refreshRate = -1;
    int localPort = StaticData.clientPort;
    long updateStartTime = 0;
    boolean isRunning = true;
    boolean isConnected = false;
    boolean doUpdateContinuously = true;
    boolean doInfiniteWrap = true;
    boolean getDataLocally = false;
    boolean readyToRun = false;
    boolean preStartPassed = false;
    String dataSource = "src/client/test_data_5.png";
    String localIP = "0.0.0.0";
    String uuid;

    ClientViewController clientViewController;
    Thread updateThread;
    Task<Void> clientRunningTask;
    Thread clientRunningThread;
    NetworkInterface serverConnection;
    ServerSocket peerToPeerServer;
    Neighbourhood neighbourhood;
    DataStore dataStore;
    ResizableCanvas gameGUI;
    Stage mainStage;
    LogContainer logs;
    State state = State.INITIALISING;
    ConnectionType connectionType = ConnectionType.PEER_TO_PEER;
    EdgeLogic edgeLogic = EdgeLogic.WRAP_AROUND;

    /**
     * JavaFX start method, required to run any javafx app.
     * The jfx equivalent of main(String[] args). calls relevant setup methods.
     *
     * @param stage         the default JavaFX autogenerated Stage object.
     * @throws Exception    throws and error somewhere...
     * TODO - find where error is thrown, may be default by jfx
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.mainStage = stage;
        initialiseUI(stage);
        dataStore = new DataStore();
        this.state = State.REST_API;
        initialiseLocalServer();
        run();
    }

    /**
     * Loads the Client User Interface from the relevant FXML file
     *
     * @param stage     the default JavaFX autogenerated Stage object.
     * TODO - more robust error checking?
     */
    private void initialiseUI(Stage stage) {
        Parent root;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui/ClientView.fxml"));
            root = fxmlLoader.load();
            clientViewController = fxmlLoader.getController();
            clientViewController.setClient(this);
            Platform.runLater(() -> {
                try {
                    localIP = IpChecker.getLocalIP();
                    clientViewController.setLocalAddress(localIP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    System.out.println("CLOSING");
                    System.exit(0);
                }
            });
            stage.setTitle("Game of Life - Client");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Error happened here");
            e.printStackTrace();
        }
    }

    private void openDirectConnectConfirmation(String serverIP, String message) {
        System.out.println("opening manual connect");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui/DirectConnectConfirmationView.fxml"));
            Parent directConnectRoot = fxmlLoader.load();
            ((DirectConnectConfirmationViewController)(fxmlLoader.getController())).setClient(this);
            ((DirectConnectConfirmationViewController)(fxmlLoader.getController())).setDirectConnect_IpAddress(serverIP);
            ((DirectConnectConfirmationViewController)(fxmlLoader.getController())).setDirectConnect_Message(message);
            Stage directConnectStage = new Stage();
            directConnectStage.setTitle("Direct Connect");
            directConnectStage.setScene(new Scene(directConnectRoot));
            directConnectStage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the Client Game Interface that displays the actual game grid and cells
     *
     * @param stage     the default JavaFX autogenerated Stage object.
     * TODO - more robust testing
     */
    private void initialiseGameUI(Stage stage) {
        gameGUI = new ResizableCanvas();
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(gameGUI);
        gameGUI.widthProperty().bind(stackPane.widthProperty());
        gameGUI.heightProperty().bind(stackPane.heightProperty());
        gameGUI.setData(data);
        gameGUI.draw();
        stage.setScene(new Scene(stackPane, 480, 320));
        gameGUI.draw();
        stage.setTitle("Game of Life - Client");
        stage.show();
    }

    /**
     * Old method required when testing multiple Clients on the one local machine.
     * Each client required a dedicated port to differentiate from others, as such
     * the server had to provide each client with a port.
     * Required knowledge of the server's IP to function, not good.
     */
    @Deprecated
    private void initialisePort() throws IOException {
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

    /**
     * Initialises the local API server
     */
    private void initialiseLocalServer() {
        try {
            peerToPeerServer = new ServerSocket(localPort);
            System.out.println("client peer to peer server running over localhost on port " + localPort);
            clientViewController.setLocalServerFlags(StaticData.SERVER_STATUS_RUNNING);
            clientViewController.setLocalStatus("Running");
        }
        catch(Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Client Running Error");
            alert.setHeaderText("Another instance of Game of Life Client is already running on this machine!");
            alert.setContentText("Only one active instance allowed per IP address.");

            clientViewController.setLocalServerFlags(StaticData.SERVER_STATUS_CLOSED);
            clientViewController.setLocalStatus("Already in use");
            clientViewController.lockConnectionUI();

            alert.showAndWait();
        }
    }

    /**
     * Sends register request to server. Initialises relevant connection-related variables
     * on successful connection to server.
     */
    private void register() throws IOException {

        NetworkObject register = new NetworkObject(Command.REGISTER);
        register.addVariable("port", Integer.toString(localPort));
        register.addVariable("pseudo", clientViewController.getPseudo());
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

            isConnected = true;
            Platform.runLater(() -> {
                //clientViewController.checkConnectToServerButtonValidity();
                clientViewController.setServerStatusFlags(StaticData.SERVER_STATUS_RUNNING);
                clientViewController.setServerStatus("Connected");
                clientViewController.lockConnectionUI();
            });

        }
        else {
            System.out.println("unable to register with server");
            clientViewController.setServerStatusFlags(StaticData.SERVER_STATUS_CLOSED);
            clientViewController.setServerStatus("Unavailable");
            clientViewController.unlockConnectionUI();
        }
    }

    /**
     * Populates the local game grid's cells with the relevant data. If local data
     * is in use it loads the data from a file, if non-local data is required it
     * requests the data from the server and then populates.
     */
    private void initialiseData() throws IOException {
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
        this.logs = new LogContainer(this.refreshRate);
        this.readyToRun = true;
    }

    /**
     * Creates and starts the Client running loop. Controls when the client is in API mode
     * and Game mode.
     *
     * TODO - This Task may be what is causing the program to persist running even after the
 *            window is closed. More investigation is needed.
     */
    public void run() {
        System.out.println("client running loop called");
        clientRunningTask = new Task<Void>() {
            @Override public Void call() {
                int count = 0;
                while (isRunning) {
                    if (isCancelled()) {
                        System.out.println("thread cancelled");
                        break;
                    }
                    System.out.println("loop #" + ++count);
                    printState();
                    if(state == client.obj.State.REST_API) updateServer();
                    if(state == client.obj.State.GAME) updateGame();
                    if(state == client.obj.State.RENDER) updateUI();
                }
                return null;
            }
        };
        clientRunningThread = new Thread(clientRunningTask);
        clientRunningThread.start();
    }

    /**
     * Connects to a Server instance of Game of Life via a given IP address.
     */
    private void connect() throws IOException {
        register();
        initialiseData();
        Platform.runLater(()->{
            clientViewController.disableConnectToServerButton();
            clientViewController.setLocalStatus("Running");
        });
        /*catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Network Error");
            alert.setHeaderText("Unable To Reach Server");
            alert.setContentText("The program encountered an error when attempting to connect to the server.");

            alert.showAndWait();
        }*/
    }

    public void connectViaDirectConnect(String serverIP) throws IOException {
        System.out.println("Direct connect request validated!");
        serverConnection = new NetworkInterface(serverIP, StaticData.serverPort);
        connect();
    }

    public void connectViaManualEntry(String serverIP) throws IOException {
        serverConnection = new NetworkInterface(serverIP, StaticData.serverPort);
        connect();
    }

    private void updateServer() {
        try {
            Socket socket = peerToPeerServer.accept();
            System.out.println("\nincoming request from " + socket.getRemoteSocketAddress().toString());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(!in.ready()) { /* stall for connection */ }

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

                if(neighbourhood != null) {
                    Connection connection = new Connection(ip, port, uuid);
                    Neighbour neighbour = new Neighbour(connection, direction);
                    neighbourhood.setNeighbour(neighbour);
                    neighbourhood.print();
                }
            }
            if(request.getCommand() == Command.PRE_START) {
                if(preStartPassed) {
                    response.setCommand(Command.READY);
                }
                else {
                    System.out.println("attempting pre-start");
                    if(!readyToRun) {
                        response.setCommand(Command.NOT_READY_TO_START);
                    }
                    else {
                        //  switch to Game UI
                        Platform.runLater(() -> {
                            initialiseGameUI(mainStage);
                            gameGUI.draw();
                            preStartPassed = true;
                        });
                    }
                }
            }
            if(request.getCommand() == Command.DIRECT_CONNECT) {
                Platform.runLater(() -> {
                    openDirectConnectConfirmation(request.getSourceHost(), request.getVariable("message"));
                });
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
            if (request.getCommand() == Command.TEST_COMMAND) {
                System.out.println("Received test response");
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
                try {
                    if(connectionType == ConnectionType.CLIENT_SERVER) {
                        NetworkInterface clientInterface = new NetworkInterface(neighbour.getConnection().getIp(), neighbour.getConnection().getPort());
                        /*NetworkObject dataGETResponse = */clientInterface.GET(dataGET);
                    }
                    else if(connectionType == ConnectionType.PEER_TO_PEER) {
                        dataGET.addVariable("recipient",neighbour.getConnection().getIp());
                        /*NetworkObject dataGETResponse = */serverConnection.GET(dataGET);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        if(connectionType == ConnectionType.PEER_TO_PEER) {
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
        } else if(connectionType == ConnectionType.CLIENT_SERVER) {
            System.out.println("sending data to " + recipient.getConnection() + " via Server");
            response.addVariable("recipient",recipient.getConnection().getIp());
            try {
                serverConnection.GET(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        data = nextData;
        this.state = State.RENDER;
        long updateEndTime = System.currentTimeMillis();
        logs.add(new Log(generation, updateStartTime, updateEndTime));
        generation++;
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
        } else if(generation > maxGenerations) {
            System.out.println("\nfinished");
            System.out.println("Logs");
            System.out.println("no. of logs: " + logs.getLogs().size());
            System.out.println("target time: " + refreshRate);
            System.out.println("avg time: " + logs.getAverageTime());
            System.out.println("max time: " + logs.getLongestDuration());
            System.out.println("min time: " + logs.getShortestDuration());

        }
    }

    public void printState() {
        System.out.println("State: " + state);
    }

    private void updateUI() {
        gameGUI.setData(data);
        gameGUI.draw();
        this.state = State.REST_API;
    }

    public boolean isConnected() { return isConnected; }

    public static void main(String[] args) {
        launch(args);
    }

}