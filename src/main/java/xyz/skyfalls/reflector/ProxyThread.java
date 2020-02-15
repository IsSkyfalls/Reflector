package xyz.skyfalls.reflector;

import xyz.skyfalls.reflector.net.MinecraftInputStream;
import xyz.skyfalls.reflector.net.MinecraftOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class ProxyThread extends Thread {
    private Server serverInstance;
    private MinecraftInputStream incoming;
    private MinecraftOutputStream outgoing;
    private ProxyThread twinThread;

    public ProxyThread(Server serverInstance,InputStream incoming, OutputStream outgoing) throws IOException{
        this.serverInstance = serverInstance;
        this.incoming = new MinecraftInputStream(incoming);
        this.outgoing = new MinecraftOutputStream(outgoing);
    }

    public synchronized void start(ProxyThread t){
        this.twinThread = t;
        super.start();
    }

    public void run(){
        try {
            byte[] buffer = new byte[8192]; // Adjust if you want
            int bytesRead;
            while ((bytesRead = incoming.read(buffer)) != -1 && !this.isInterrupted()) {
                outgoing.write(buffer, 0, bytesRead);
                outgoing.flush();
            }
        } catch (SocketException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        twinThread.interrupt();
        serverInstance.connections.decrementAndGet();
        try {
            this.incoming.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.outgoing.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
