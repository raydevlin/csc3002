package server.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import server.App;
import shared.data.Memory;

import java.io.IOException;
import java.net.URL;

public class DirectConnectController {

    private App server;
    public void setServer(App server) { this.server = server; }


    @FXML
    private AnchorPane DirectConnect_InputAnchorPane;

    @FXML
    private TextField DirectConnect_IpAddress;

    @FXML
    private TextArea DirectConnect_Message;

    @FXML
    private Label DirectConnect_Description;

    @FXML
    private Button DirectConnect_ConnectButton;

    @FXML
    private CheckBox DirectConnect_DoRememberMessage;

    @FXML
    private Rectangle DirectConnect_InteractionGuard;

    @FXML
    private ImageView DirectConnect_LoadingWheel;

    @FXML
    private Label DirectConnect_SuccessMessage;

    @FXML
    private Label DirectConnect_SuccessHeader;

    @FXML
    void tryConnect(ActionEvent event) {

        String ipAddress = DirectConnect_IpAddress.getText();
        String message = DirectConnect_Message.getText();

        if(ipAddress == null || ipAddress.trim().isEmpty()) {
            Alert nullIpAlert = new Alert(Alert.AlertType.ERROR);
            nullIpAlert.setTitle("IP Error");
            nullIpAlert.setHeaderText("No IP Address entered!");
            nullIpAlert.setContentText("Please enter an IP address to connect to!");
            nullIpAlert.showAndWait();
            return;
        }

        Memory.DirectConnectRememberMessage = DirectConnect_DoRememberMessage.isSelected();

        if(DirectConnect_DoRememberMessage.isSelected()) {
            Memory.DirectConnectMessage = message;
        }

        System.out.println("try connect");
        lockUI();
        new Thread(()-> {
            try {
                server.directConnect(ipAddress, message, this);
            } catch (IOException e) {
                Platform.runLater(()-> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Network Error");
                    alert.setHeaderText("Unable To Reach Client");
                    alert.setContentText("The program encountered an error when attempting to connect to the client. Ensure IP is valid and that client is connected to the internet.");
                    alert.showAndWait();
                });
            }
        }).start();

    }

    private void lockUI() {
        DirectConnect_InteractionGuard.setVisible(true);
        DirectConnect_LoadingWheel.setVisible(true);
    }

    private void unlockUI() {
        DirectConnect_InteractionGuard.setVisible(false);
        DirectConnect_LoadingWheel.setVisible(false);
    }

    public void successfulConnection() {
        unlockUI();
        DirectConnect_InputAnchorPane.setVisible(false);
        DirectConnect_ConnectButton.setVisible(false);
        DirectConnect_DoRememberMessage.setVisible(false);
        DirectConnect_Description.setVisible(false);

        DirectConnect_SuccessHeader.setVisible(true);
        DirectConnect_SuccessMessage.setVisible(true);
    }

    @FXML
    public void initialize() {
        System.out.println("direct connect initialised");
        if(Memory.DirectConnectRememberMessage) {
            DirectConnect_Message.setText(Memory.DirectConnectMessage);
            DirectConnect_DoRememberMessage.setSelected(true);
        }
    }

}