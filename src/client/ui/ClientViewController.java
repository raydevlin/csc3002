package client.ui;

import client.App;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import shared.data.StaticData;

import java.io.IOException;

public class ClientViewController {

    private App client;
    public void setClient(App client) { this.client = client; }

    public ClientViewController(){

    }

    @FXML
    private void initialize(){

    }

    @FXML
    private TextField ClientProperties_ServerAddress;

    @FXML
    private TextField ClientProperties_ClientPseudo;

    @FXML
    private CheckBox ClientProperties_GetConfigFromServer;

    @FXML
    private Label ClientProperties_LocalAddress;

    @FXML
    private Button ClientProperties_ConnectToServerButton;

    @FXML
    private Label ConnStatus_LocalStatus;

    @FXML
    private Label ConnStatus_ServerStatus;

    @FXML
    private Circle ConnStatus_LocalStatus_Flag;

    @FXML
    private Circle ConnStatus_ServerStatus_Flag;

    @FXML
    private Rectangle Connect_InteractionGuard;

    @FXML
    private ImageView Connect_LoadingWheel;

    @FXML
    private Hyperlink ClientProperties_LoadConfigButton;

    @FXML
    private Button Client_StartButton;

    @FXML
    void connect(ActionEvent event) {
        lockConnectionUI();
        new Thread(()->{
            try {
                client.connectViaManualEntry(ClientProperties_ServerAddress.getText().trim());
            }
            catch (IOException e) {
                Platform.runLater(()-> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Network Error");
                    alert.setHeaderText("Unable To Reach Server");
                    alert.setContentText("The program encountered an error when attempting to connect to the server. Ensure IP is valid and that client is connected to the internet.");
                    alert.showAndWait();
                });
                unlockConnectionUI();
            }
        }).start();
    }

    public void lockConnectionUI() {
        Connect_InteractionGuard.setVisible(true);
        Connect_LoadingWheel.setVisible(true);
        disableServerAddressTextField();
        disableConnectToServerButton();
    }

    public void unlockConnectionUI() {
        Connect_InteractionGuard.setVisible(false);
        Connect_LoadingWheel.setVisible(false);
        enableServerAddressTextField();
        enableConnectToServerButton();
    }

    @FXML
    public void checkConnectToServerButtonValidity() {
        ClientProperties_ConnectToServerButton.setDisable(
                !client.isConnected() &&
                        !(ClientProperties_ServerAddress.getText() != null && !ClientProperties_ServerAddress.getText().isEmpty())
        );
    }

    public void openDirectConnectConfirmation() {
        System.out.println("opening manual connect");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DirectConnectConfirmationView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Direct Connect");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLocalServerFlags(int serverStatus) {
        ConnStatus_LocalStatus_Flag.setFill(StaticData.SERVER_STATUS_STYLES.get(serverStatus));
    }

    public void setServerStatusFlags(int serverStatus) {
        ConnStatus_ServerStatus_Flag.setFill(StaticData.SERVER_STATUS_STYLES.get(serverStatus));
    }

    @FXML
    void loadConfig(ActionEvent event) {

    }

    @FXML
    void testSwitch(ActionEvent event) {

    }

    public String getPseudo() { return (ClientProperties_ClientPseudo.getText().isEmpty())?"client-default-pseudo":ClientProperties_ClientPseudo.getText(); }

    public void setLocalAddress(String ip) {
        ClientProperties_LocalAddress.setText(ip);
    }

    public void setLocalStatus(String localStatus) { ConnStatus_LocalStatus.setText(localStatus); }

    public void setServerStatus(String serverStatus) { ConnStatus_ServerStatus.setText(serverStatus); }


    public void disableConnectToServerButton() { ClientProperties_ConnectToServerButton.setDisable(true); }

    public void enableConnectToServerButton() { ClientProperties_ConnectToServerButton.setDisable(false); }

    public void disableServerAddressTextField() { ClientProperties_ServerAddress.setDisable(true); }

    public void enableServerAddressTextField() { ClientProperties_ServerAddress.setDisable(false); }



    @FXML
    public void loadConfig() {
        System.out.println("load config!");
    }

}