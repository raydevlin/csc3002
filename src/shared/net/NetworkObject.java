package shared.net;

import shared.data.Command;
import shared.data.StaticData;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

/**
 * Request object used to easily parse the contents of incoming requests
 * as well as simplifying the process of creating a request.
 * Requests stored in String form: "404 var0_var1_var2_var3" where
 * 404 is the opCode and vars are listed separated by underscores.
 * */
public class NetworkObject {

    private int command;
    private String sourceHost;
    private int sourcePort;

    private HashMap<String, String> vars;

    private static final String separator = StaticData.networkObjectSerialisationSeparator;

    public NetworkObject(int command) {
        this.command = command;
        this.vars = new HashMap<>();
    }

    public NetworkObject(int command, HashMap<String,String> vars) {
        this.command = command;
        this.vars = new HashMap<>(vars);
    }

    public NetworkObject(NetworkObject networkObject) {
        this.command = networkObject.command;
        this.vars = networkObject.vars;
        this.sourceHost = networkObject.sourceHost;
        this.sourcePort = networkObject.sourcePort;
    }

    public int getCommand() { return command; }
    public int getSourcePort() { return sourcePort; }
    public String getSourceHost() { return sourceHost; }
    public void setCommand(int command) { this.command = command; }

    //  depreciated due to the introduction of NetworkInterface
    @Deprecated
    public void send(PrintWriter out) {
        out.println(toSerialisedString());
    }

    public void stripNetSource(Socket socket) {
        try {
            String rawSource = socket.getRemoteSocketAddress().toString();
            this.sourcePort = Integer.parseInt(rawSource.substring(rawSource.lastIndexOf(':') + 1));
            this.sourceHost = rawSource.substring(1, rawSource.lastIndexOf(':'));
        }
        catch (Exception e) {
            System.out.println("unable to strip socket source, does not follow expected format: " + socket.getRemoteSocketAddress().toString());
        }
    }

    public String toSerialisedString() {
        String request = Integer.toString(command);
        int count = 0;
        for(String key : vars.keySet()) {
            request += key + separator + vars.get(key);
            if(count < vars.keySet().size()-1) request += separator;
            count++;
        }
        return request;
    }

    public static NetworkObject fromSerialisedString(String requestString) {
        try {
            int command = Integer.parseInt(requestString.substring(0,3));
            if (requestString.length() < 4) return new NetworkObject(command);
            String[] varsSet = requestString.substring(3).split(separator);
            HashMap<String, String> vars = new HashMap<>();
            for(int i = 0; i < varsSet.length; i += 2) {
                if(i + 1 >= varsSet.length) break;
                String key = varsSet[i];
                String val = varsSet[i + 1];
                vars.put(key,val);
            }
            return new NetworkObject(command, vars);
        }
        catch (Exception e) {
            System.out.println("malformed request");
            System.out.println("request: " + requestString);
            return new NetworkObject(Command.ACKNOWLEDGE);
        }
    }

    public void addVariable(String key, String variable) {
        if(vars == null) return;
        if(variable != null && variable.contains(separator)) return;
        vars.put(key,variable);
    }

    public String getVariable(String key) {
        if(vars == null) return null;
        return vars.get(key);
    }

    @Override
    public String toString() {
        return toSerialisedString();
    }
}

