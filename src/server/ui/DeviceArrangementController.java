package server.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import server.App;
import shared.data.StaticData;
import shared.obj.ClientNet;
import shared.obj.Connection;
import shared.obj.Coordinate;

import java.util.ArrayList;
import java.util.UUID;


public class DeviceArrangementController {

    final int IP_CHILD_INDEX = 0;
    final int PSEUDO_CHILD_INDEX = 1;
    final int X_COORD_CHILD_INDEX = 2;
    final int Y_COORD_CHILD_INDEX = 3;
    final int DEVICE_NULL_CHILD_INDEX = 4;

    boolean hasClientNetChanged = false;
    double padding = 10.0;
    double maxWidth = 240;
    boolean isDragging = false;

    String initiatingDeviceIP = "";
    String initiatingDevicePseudo = "";

    App server;
    ClientNet clientNet;
    ClientNet originalState;
    VBox activeDevice = null;
    ArrayList<VBox> devices;

    @FXML
    public void initialize() {
        DeviceArrangement_DeviceContainerPane.setMaxSize(DeviceArrangement_DeviceContainerPane.getWidth(), DeviceArrangement_DeviceContainerPane.getHeight());
        DeviceArrangement_Device_Ghost.setMouseTransparent(true);
        DeviceArrangement_MouseTracker.setMouseTransparent(true);
    }


    public void setServer(App server) {
        this.server = server;
    }

    public void setClientNet(ClientNet clientNet) {
        this.clientNet = clientNet;
        if(clientNet == null /*|| clientNet.isEmpty()*/) {
            setEmptyMessage();
            return;
        }
        originalState = new ClientNet(clientNet);
        hasClientNetChanged = true;
        DeviceArrangement_ConnectedDevices.setText(Integer.toString(clientNet.getActiveConnectionCount()));
        DeviceArrangement_MaximumDevices.setText(Integer.toString(clientNet.getMaxConnections()));
        draw();
    }

    private void setEmptyMessage() {
        //  show a label saying "no connections found"
        System.out.println("implement empty label");
    }

    public void draw() {
        if(clientNet == null) {
            setEmptyMessage();
            return;
        }
        if(!hasClientNetChanged) return;

        devices = new ArrayList<>();
        for(int y = 0; y < clientNet.getHeight(); y++) {
            for(int x = 0; x < clientNet.getWidth(); x++) {
                Connection connection = clientNet.get(x,y);
                VBox device = createDevice(connection);
                devices.add(device);
            }
        }

        double spacingX = DeviceArrangement_DeviceContainerPane.getWidth() / (double)clientNet.getWidth();
        double spacingY = DeviceArrangement_DeviceContainerPane.getHeight() / (double)clientNet.getHeight();
        double midPointX = DeviceArrangement_DeviceContainerPane.getWidth() / 2.0;
        double midPointY = DeviceArrangement_DeviceContainerPane.getHeight() / 2.0;


        double deviceWidth = 100;
        double deviceHeight = 100;

        double startingPosX = 0;
        double startingPosY = 0;

        if(clientNet.getWidth() >= clientNet.getHeight()) {
            deviceWidth = (DeviceArrangement_DeviceContainerPane.getWidth() - (padding*(clientNet.getWidth()-1)))/clientNet.getWidth();
            if(deviceWidth > maxWidth) deviceWidth = maxWidth;
            deviceHeight = deviceWidth * 9.0/16.0;

            DeviceArrangement_Device_Ghost.setPrefWidth(deviceWidth);
            DeviceArrangement_Device_Ghost.setPrefHeight(deviceHeight);

            double totalWidth = (deviceWidth * clientNet.getWidth()) + (padding * (clientNet.getWidth()-1));
            double totalHeight = (deviceHeight * clientNet.getHeight()) + (padding * (clientNet.getHeight()-1));

            startingPosX = (DeviceArrangement_DeviceContainerPane.getWidth() - totalWidth) / 2;
            startingPosY = (DeviceArrangement_DeviceContainerPane.getHeight() - totalHeight) / 2;
        }
        else {
            System.out.println("ADD CASE WHEN TALLER THAN WIDE");
        }


        for(int y = 0; y < clientNet.getHeight(); y++) {
            for (int x = 0; x < clientNet.getWidth(); x++) {

                VBox device = devices.get((clientNet.getWidth()*y)+x);
                ((Label)device.getChildren().get(X_COORD_CHILD_INDEX)).setText(Integer.toString(x));
                ((Label)device.getChildren().get(Y_COORD_CHILD_INDEX)).setText(Integer.toString(y));
                device.setPrefWidth(deviceWidth);
                device.setPrefHeight(deviceHeight);


                double locationX = startingPosX + x * (device.getPrefWidth() + padding);
                double locationY = startingPosY + y * (device.getPrefHeight() + padding);;

                DeviceArrangement_DeviceContainerPane.getChildren().add(device);

                device.setLayoutX(locationX);
                device.setLayoutY(locationY);

            }
        }

        DeviceArrangement_DeviceContainerPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DeviceArrangement_Device_Ghost.toFront();
                DeviceArrangement_Device_Ghost.setLayoutX(mouseEvent.getX() - DeviceArrangement_Device_Ghost.getWidth()/2);
                DeviceArrangement_Device_Ghost.setLayoutY(mouseEvent.getY() - DeviceArrangement_Device_Ghost.getHeight()/2);
                DeviceArrangement_MouseTracker.setLayoutX(mouseEvent.getX());
                DeviceArrangement_MouseTracker.setLayoutY(mouseEvent.getY());
            }
        });

        Timeline timeline = new Timeline(60, new KeyFrame(Duration.millis(33), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(isDragging) {
                    Shape tracker = new Circle(DeviceArrangement_MouseTracker.getLayoutX(), DeviceArrangement_MouseTracker.getLayoutY(), DeviceArrangement_MouseTracker.getRadius());
                    for(VBox device : devices) {
                        Shape recip = new Rectangle(device.getLayoutX(), device.getLayoutY(),
                                device.getWidth(),device.getHeight());
                        if (isCollision(tracker, recip)) {
                            if(isNull(device)) device.setStyle(StaticData.deviceConfig_HoverNullStyle);
                            else device.setStyle(StaticData.deviceConfig_HoverStyle);
                        }
                        else {
                            if(isNull(device)) device.setStyle(StaticData.deviceConfig_NullStyle);
                            else device.setStyle(StaticData.deviceConfig_DefaultStyle);
                        }
                    }
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    private boolean isCollision(Node node0, Node node1) {
        if(node0 == null || node1 == null) return false;
        return node0.getLayoutBounds().intersects(node1.getLayoutBounds());
    }

    private VBox createDevice(Connection connection) {
        VBox device = new VBox();
        device.setPadding(new Insets(10,10,10,10));
        Label deviceIsNull = new Label("false");
        deviceIsNull.setVisible(false);

        if(connection == null) {
            device.setId("device-null-" + UUID.randomUUID().toString());
            deviceIsNull.setText("true");
            Label ip = new Label("null");
            Label pseudo = new Label("null");
            Label x = new Label("-1");
            Label y = new Label("-1");
            ip.setVisible(false);
            pseudo.setVisible(false);
            x.setVisible(false);
            y.setVisible(false);
            device.getChildren().addAll(ip,pseudo, x, y);
            device.setStyle(StaticData.deviceConfig_NullStyle);
        }
        else {
            device.setId("device-" + connection.getUuid());

            device.setStyle(StaticData.deviceConfig_DefaultStyle);

            Label ip = new Label(connection.getIp());
            Label pseudo = new Label("pseudo-placeholder-" + connection.getPort());

            Label x = new Label("-1");
            Label y = new Label("-1");
            x.setVisible(false);
            y.setVisible(false);

            device.getChildren().addAll(ip,pseudo, x, y);

            //  Bring ghost to clicked device, set styling
            device.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    activeDevice = device;
                    DeviceArrangement_Device_Ghost.setLayoutX(device.getLayoutX());
                    DeviceArrangement_Device_Ghost.setLayoutY(device.getLayoutY());
                    DeviceArrangement_Device_Ghost.setVisible(true);
                    device.setStyle(StaticData.deviceConfig_HoverStyle);
                    isDragging = true;
                }
            });

            //  check if a collision happened
            device.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    for(VBox recipient : devices) {
                        if(recipient == device) continue;
                        Shape tracker = new Circle(DeviceArrangement_MouseTracker.getLayoutX(), DeviceArrangement_MouseTracker.getLayoutY(), DeviceArrangement_MouseTracker.getRadius());
                        Shape recip = new Rectangle(recipient.getLayoutX(), recipient.getLayoutY(),recipient.getWidth(),recipient.getHeight());

                        if (isCollision(tracker, recip)) {
                            System.out.println("collision released from " + ((Label)device.getChildren().get(0)).getText() + " on " + ((Label)recipient.getChildren().get(0)).getText());
                            int deviceCoordX = Integer.parseInt(((Label)device.getChildren().get(X_COORD_CHILD_INDEX)).getText());
                            int deviceCoordY = Integer.parseInt(((Label)device.getChildren().get(Y_COORD_CHILD_INDEX)).getText());
                            int recipientCoordX = Integer.parseInt(((Label)recipient.getChildren().get(X_COORD_CHILD_INDEX)).getText());
                            int recipientCoordY = Integer.parseInt(((Label)recipient.getChildren().get(Y_COORD_CHILD_INDEX)).getText());
                            clientNet.swap(new Coordinate(deviceCoordX, deviceCoordY), new Coordinate(recipientCoordX, recipientCoordY));
                            activeDevice = null;
                            draw();
                            break;
                        }

                    }

                    DeviceArrangement_Device_Ghost.setVisible(false);
                    DeviceArrangement_Device_Ghost.setLayoutX(0);
                    DeviceArrangement_Device_Ghost.setLayoutY(0);
                    DeviceArrangement_MouseTracker.setLayoutX(0);
                    DeviceArrangement_MouseTracker.setLayoutY(0);
                    isDragging = false;
                }
            });
        }


        device.getChildren().add(deviceIsNull);
        device.setVisible(true);
        return device;
    }

    @FXML
    private AnchorPane Root;

    @FXML
    private Circle DeviceArrangement_MouseTracker;

    @FXML
    private Label DeviceArrangement_ConnectedDevices;

    @FXML
    private Label DeviceArrangement_MaximumDevices;

    @FXML
    private AnchorPane DeviceArrangement_DeviceContainerPane;

    @FXML
    private VBox DeviceArrangement_Device_Default;

    @FXML
    private VBox DeviceArrangement_Device_Ghost;

    @FXML
    private Label DeviceArrangement_Ghost_SourceNode;

    @FXML
    private Label DeviceArrangement_Ghost_Pseudo;

    @FXML
    private Label DeviceArrangement_IpLabel;

    @FXML
    private Label DeviceArrangement_Pseudo;

    @FXML
    private VBox DeviceArrangement_Device_Recipient;

    @FXML
    private Label DeviceArrangement_IpLabel_Recipient;

    @FXML
    private Label DeviceArrangement_Pseudo_Recipient;

    @FXML
    private Button DeviceArrangement_DoneButton;

    @FXML
    void updateClientNet(ActionEvent event) {
        //clientNet.print();
        server.printClientNet();
        server.updateNeighbours();
        System.out.println();
    }

    boolean isNull(VBox device) {
        return device == null || "true".equals(((Label)device.getChildren().get(DEVICE_NULL_CHILD_INDEX)).getText());
    }

}
