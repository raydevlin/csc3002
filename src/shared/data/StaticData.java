package shared.data;

import javafx.scene.paint.Color;

import java.util.HashMap;

public class StaticData {

    public static final String serverHost = "localhost";
    public static final int serverPort = 59712;
    public static final int clientPort = 61713;

    public static final String networkObjectSerialisationSeparator = "_--_";
    public static final String neighbourhoodSerialisationSeparator = "_!!_";
    public static final String matrixSerialisationSeparator = "_@@_";
    public static final String gameSettingsSerialisationSeparator = "_&&_";
    public static final String serverSettingsSerialisationSeparator = "_&&_";

    public static final String deviceConfig_HoverStyle = "-fx-border-color:#ebc400; -fx-border-width: 2px; -fx-background-color: #dedede";
    public static final String deviceConfig_DefaultStyle = "-fx-border-color:black; -fx-border-width: 1px; -fx-background-color: #dedede";
    public static final String deviceConfig_NullStyle = "-fx-border-color:#999; -fx-border-width: 1px; -fx-background-color: #999";
    public static final String deviceConfig_HoverNullStyle = "-fx-border-color:#ebc400; -fx-border-width: 2px; -fx-background-color: #999";

    public static final int SERVER_STATUS_NULL = 0;
    public static final int SERVER_STATUS_RUNNING = 1;
    public static final int SERVER_STATUS_STALL = 2;
    public static final int SERVER_STATUS_CLOSED = 3;

    public static final HashMap<Integer, Color> SERVER_STATUS_STYLES = new HashMap<Integer, Color>() {{
        put(SERVER_STATUS_NULL, Color.rgb(173,173,173));
        put(SERVER_STATUS_RUNNING, Color.rgb(6,178,0));
        put(SERVER_STATUS_STALL, Color.rgb(255,218,33));
        put(SERVER_STATUS_CLOSED, Color.rgb(197,0,0));
    }};


}

