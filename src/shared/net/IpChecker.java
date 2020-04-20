package shared.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Code sourced from StackOverflow and technicalkeeda.com
 * getPublicIP - https://stackoverflow.com/a/14541376
 * getLocalIP - https://www.technicalkeeda.com/java-tutorials/get-local-ip-address-and-hostname-in-java
 */
public class IpChecker {

    public static String getPublicIP() {
        BufferedReader in = null;
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");

            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        }
        catch (MalformedURLException mUrlException) {
            mUrlException.printStackTrace();
            return "MalformedURL - Unable to fetch public IP";
        }
        catch(IOException exception) {
            exception.printStackTrace();
            return "Unable to fetch public IP";
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return "Unable to fetch local IP";
        }
    }

}
