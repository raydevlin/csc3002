package shared.data;

import java.util.HashMap;

public class Command {

    //  CONNECTION/SETUP CODES
    public static final int REGISTER = 100;
    public static final int ACKNOWLEDGE = 101;
    public static final int INITIALISE = 102;
    public static final int SET_ID = 103;
    public static final int NAME_OK = 104;
    public static final int SET_CONNECTION = 105;
    public static final int CLOSE_CONNECTION = 106;
    public static final int SET_SPEED = 107;
    public static final int SET_MAX_GEN = 108;
    public static final int SET_SEED = 109;
    public static final int DIRECT_CONNECT = 110;

    public static final int SET_DIMENSIONS = 120;
    public static final int SET_PORT = 121;
    public static final int SET_GAME_DATA = 122;

    public static final int GET_DIMENSIONS = 130;
    public static final int GET_PORT = 131;
    public static final int GET_GAME_DATA = 132;
    public static final int GET_DATA = 133;

    //  RUNNING CODES
    public static final int PRE_START = 200;
    public static final int START = 201;
    public static final int PAUSE = 202;
    public static final int UPDATE = 203;
    public static final int RESET = 204;
    public static final int CLOSE = 205;
    public static final int MESSAGE = 206;
    public static final int QUIT = 207;
    public static final int REDRAW = 208;
    public static final int SEND_DATA = 209;
    public static final int RECEIVE_DATA = 210;
    public static final int READY = 211;

    //  ERROR CODES
    public static final int GENERIC_ERROR = 400;
    public static final int SERVER_FULL = 401;
    public static final int MULTIPLE_REGISTRATION = 402;
    public static final int PORT_OVERFLOW = 403;
    public static final int OUT_OF_SYNC = 404;
    public static final int NOT_READY_TO_START = 405;

    //  DEFAULT CODE
    public static final int TEST_COMMAND = 997;
    public static final int MESSAGE_IGNORED = 998;
    public static final int DEFAULT_CODE = 999;


    //  CMD TRANSLATION MAP
    public static final HashMap<String, Integer> TRANSLATION_MAP = new HashMap<String, Integer>() {{
        put("UPDATE", UPDATE);
        put("START", START);
        put("RESET", RESET);
        put("SPEED", SET_SPEED);
        put("GENERATIONS", SET_MAX_GEN);
        put("GAMEDIMS", SET_DIMENSIONS);
        put("SEED", SET_SEED);
        put("QUIT", QUIT);
    }};

}

