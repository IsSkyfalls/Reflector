package xyz.skyfalls.reflector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends Thread {
    private static Map<String, String> proxyMapping = new HashMap<>();
    private static int maxConnections;

    AtomicInteger connections = new AtomicInteger();
    private int port;

    private Server(int port){
        this.port = port;
    }

    String getMapping(String addr){
        return proxyMapping.get(addr);
    }

    static int getMaxConnections(){
        return maxConnections;
    }

    public void run(){
        ServerSocket server_handler = null;
        Socket client_connection;

        try {
            server_handler = new ServerSocket(port, 10);
            System.out.println("Started server socket on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to start server: " + e);
        }
        while (!server_handler.isClosed()) {
            try {
                client_connection = server_handler.accept();
                new Thread(new HandshakeRunnable(client_connection, this)).run();
            } catch (Exception err) {
                err.printStackTrace();
                System.out.println("Connection failed: " + err);
            }
        }
    }

    public static void main(String[] args){
        try {
            JSONObject json = new JSONObject(new JSONTokener(new FileInputStream(new File("./config.json"))));
            System.out.println(json);
            Server.maxConnections = json.getInt("maxConnections");
            Map<String, Object> mappings = json.getJSONObject("mappings").toMap();
            for (Map.Entry<String, Object> e : mappings.entrySet()) {
                proxyMapping.put(e.getKey(), (String) e.getValue());
            }
            System.out.println("Read " + proxyMapping.size() + " mappings from config");
            JSONArray listeners = json.getJSONArray("listeners");
            for (Object obj : listeners) {
                int port = (int) obj;
                new Server(port).start();
            }
        } catch (JSONException e) {
            System.out.println("An error has occurred when reading the config file.");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.out.println("Config file not found.");
            e.printStackTrace();
        }

    }
}
