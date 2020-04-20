package client.ui;

import client.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class DirectConnectConfirmationViewController {

    App client;
    public void setClient(App client) { this.client = client; }

    @FXML
    private TextField DirectConnect_IpAddress;

    @FXML
    private TextArea DirectConnect_Message;

    @FXML
    private TextField DirectConnect_Pseudo;

    @FXML
    private Button DirectConnect_ConnectButton;

    @FXML
    private Button DirectConnect_ConnectButton1;

    @FXML
    void cancelDirectConnect(ActionEvent event) {

    }

    @FXML
    void doDirectConnect(ActionEvent event) {
        try {
            client.connectViaDirectConnect(DirectConnect_IpAddress.getText());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Network Error");
            alert.setHeaderText("Unable To Reach Server");
            alert.setContentText("The program encountered an error when attempting to connect to the server.");

            alert.showAndWait();
        }
        ((Stage) DirectConnect_IpAddress.getScene().getWindow()).close();
    }

    public void setDirectConnect_IpAddress(String ipAddress) { DirectConnect_IpAddress.setText(ipAddress); }
    public void setDirectConnect_Message(String message) { DirectConnect_Message.setText(message); }

}