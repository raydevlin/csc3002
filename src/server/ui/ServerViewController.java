package server.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import server.App;
import shared.data.ConnectionType;
import shared.data.EdgeLogic;
import shared.data.StaticData;
import shared.net.IpChecker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServerViewController {

    private App server;
    public void setServer(App server) {
        this.server = server;
    }
    boolean haveSettingsChanged = false;
    boolean lockCustomGameVars = true;
    boolean getDataFromServer = true;

    public ServerViewController(){

    }

    @FXML
    private void initialize(){
        Server_ConnectionType.getItems().addAll(ConnectionType.getConnectionTypes());
        Server_ConnectionType.setValue(ConnectionType.PEER_TO_PEER);
        Client_EdgeLogic.getItems().addAll(EdgeLogic.getConnectionTypes());
        Client_EdgeLogic.setValue(EdgeLogic.WRAP_AROUND);

        Server_LocalAddress.setText(IpChecker.getLocalIP());
        new Thread(() -> {
            String ip = IpChecker.getPublicIP();
            Platform.runLater(() -> {
                Server_PublicAddress.setText(ip);
                Server_LoadWheel.setVisible(false);
                Server_PublicAddress.setVisible(true);
            });
        }).start();
    }

    @FXML
    private VBox Container_Root;

    @FXML
    private Button Server_Start;

    @FXML
    private Button Server_StartTest;

    @FXML
    private Button Server_Stop;

    @FXML
    private Label Server_LocalAddress;

    @FXML
    private Label Server_PublicAddress;

    @FXML
    private AnchorPane Container_ServerProperties;

    @FXML
    private TextField Server_NetworkWidth;

    @FXML
    private ComboBox<ConnectionType> Server_ConnectionType;

    @FXML
    private TextField Server_Name;

    @FXML
    private Button Properties_ConfigureDisplays;

    @FXML
    private TextField Server_NetworkHeight;

    @FXML
    private AnchorPane Container_ClientProperties;

    @FXML
    private TextField Client_Width;

    @FXML
    private TextField Client_Height;

    @FXML
    private CheckBox Client_GetConfigFromServer;

    @FXML
    private Label Client_DataSource;

    @FXML
    private Label Client_DataSourceTitle;

    @FXML
    private Label Server_UploadDataSource;

    @FXML
    private ComboBox<EdgeLogic> Client_EdgeLogic;

    @FXML
    private AnchorPane Container_GameProperties;

    @FXML
    private TextField Game_Generations;

    @FXML
    private TextField Game_UpdatesPerSecond;

    @FXML
    private TextField Game_Overpopulation;

    @FXML
    private TextField Game_Underpopulation;

    @FXML
    private TextField Game_Reproduction;

    @FXML
    private TextField Game_DetectionRadius;

    @FXML
    private CheckBox Game_AllowCustomGameVariables;

    @FXML
    private AnchorPane Container_ServerStatus;

    @FXML
    private Label Status_Availability;

    @FXML
    private Circle Status_Availability_Flag;

    @FXML
    private Label Status_Devices;

    @FXML
    private Label Status_DevicesSlash;

    @FXML
    private Label Status_DevicesLimit;

    @FXML
    private TextArea Status_Log;

    @FXML
    private Button Status_ManageConnections;

    @FXML
    private Button Status_ManualConnect;

    @FXML
    private Label Server_SaveConfig;

    @FXML
    private Label Server_LoadConfig;

    @FXML
    private ImageView Server_LoadWheel;

    @FXML
    void configureDisplays(ActionEvent event) {
        System.out.println("opening display configuration");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DeviceArrangementView.fxml"));
            Parent root = fxmlLoader.load();
            ((DeviceArrangementController)(fxmlLoader.getController())).setServer(server);
            Stage stage = new Stage();
            stage.setTitle("Device Configuration");
            stage.setScene(new Scene(root));
            stage.show();

            ((DeviceArrangementController)(fxmlLoader.getController())).setClientNet(server.getClientNet());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void checkDataSource(ActionEvent event) {
        getDataFromServer = Client_GetConfigFromServer.isSelected();
        Client_DataSource.setDisable(!getDataFromServer);
        Client_DataSourceTitle.setDisable(!getDataFromServer);
        Server_UploadDataSource.setDisable(!getDataFromServer);
    }

    @FXML
    void doCustomGameVariables(ActionEvent event) {
        lockCustomGameVars = !lockCustomGameVars;

        Game_Overpopulation.setDisable(lockCustomGameVars);
        Game_Underpopulation.setDisable(lockCustomGameVars);
        Game_Reproduction.setDisable(lockCustomGameVars);
        Game_DetectionRadius.setDisable(lockCustomGameVars);

    }

    @FXML
    void loadData(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(server.getStage());
        if (file != null) {
            server.loadGameData(file);
            Client_DataSource.setText(file.getParentFile().getName() + "/" + file.getName());
            Client_DataSource.setDisable(false);
            Client_DataSourceTitle.setDisable(false);
        }
    }

    @FXML
    void saveConfig(MouseEvent event) {

    }

    @FXML
    void loadConfig(MouseEvent event) {

    }

    @FXML
    void openManualConnect(ActionEvent event) {
        System.out.println("opening manual connect");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DirectConnectView.fxml"));
            Parent root = fxmlLoader.load();
            ((DirectConnectController)(fxmlLoader.getController())).setServer(server);
            Stage stage = new Stage();
            stage.setTitle("Direct Connect");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void test_sendRequest(ActionEvent event) {
        server.test_sendRequest();
    }

    @FXML
    void test_start(ActionEvent event) {
        server.update();
    }

    public void setMaxConnectionsLabel(int maxConnections) {
        Status_DevicesLimit.setText(Integer.toString(maxConnections));
    }

    public void setNoOfConnections(int connections) {
        Status_Devices.setText(Integer.toString(connections));
    }

    @FXML
    void startServer(ActionEvent event) {
        //  validate data

        Container_GameProperties.setDisable(true);
        Container_ClientProperties.setDisable(true);
        Container_ServerProperties.setDisable(true);
        server.startServer();
    }

    public void serverInitialised() {
        setLocalServerFlags(StaticData.SERVER_STATUS_RUNNING);
        setLocalStatus("Running");
        Container_ServerStatus.setDisable(false);
        Properties_ConfigureDisplays.setDisable(false);

        int width = Integer.parseInt(Server_NetworkWidth.getText());
        int height = Integer.parseInt(Server_NetworkHeight.getText());
        Status_DevicesLimit.setText(Integer.toString(width * height));

        Server_Start.setDisable(true);
        Server_Stop.setDisable(false);
    }

    public void serverNotInitialised() {
        setLocalServerFlags(StaticData.SERVER_STATUS_CLOSED);
        setLocalStatus("Unavailable");
        Container_GameProperties.setDisable(true);
        Container_ClientProperties.setDisable(true);
        Container_ServerProperties.setDisable(true);
        Server_Start.setDisable(false);
        Server_Stop.setDisable(true);
    }

    public void setWidth(int width) {
        Client_Width.setText(Integer.toString(width));
    }

    public void setHeight(int height) {
        Client_Height.setText(Integer.toString(height));
    }

    public void setLocalServerFlags(int serverStatus) {
        Status_Availability_Flag.setFill(StaticData.SERVER_STATUS_STYLES.get(serverStatus));
    }

    public void setLocalStatus(String status) {
        Status_Availability.setText(status);
    }

    public void log(String message) {
        if(message == null || message.isEmpty()) return;
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        Status_Log.setText(Status_Log.getText() + "["+ timeStamp +"] " + message + "\n");
        Status_Log.setScrollTop(Integer.MAX_VALUE);
    }
}
