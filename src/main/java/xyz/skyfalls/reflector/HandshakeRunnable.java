package xyz.skyfalls.reflector;

import lombok.AllArgsConstructor;
import xyz.skyfalls.reflector.net.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

@AllArgsConstructor
public class HandshakeRunnable implements Runnable {
    Socket clientSocket;
    Server server;

    @Override
    public void run(){
        MinecraftInputStream input = null;
        MinecraftOutputStream output = null;
        try {
            input = new MinecraftInputStream(clientSocket.getInputStream());
            HandshakePacket handshake = (HandshakePacket) input.readMinecraftPacket(
                    HandshakePacket.State.HANDSHAKING,
                    HandshakePacket.BoundTowards.SERVER);
            output = new MinecraftOutputStream(clientSocket.getOutputStream());
            if(handshake.nextState == MinecraftPacket.State.STATUS){
                while (!(clientSocket.isClosed())) {
                    MinecraftPacket packet = input.readMinecraftPacket();
                    switch (packet.getPacketId()) {
                        case 0x00:
                            String dest = server.getMapping(handshake.serverAddress);
                            if(dest != null){
                                new JsonResponsePacket(String.format(Constants.MESSAGE_PING_LIST, handshake.protocolVersion, Server.getMaxConnections(), server.connections.get(), String.format(Constants.MESSAGE_FOUND, dest), "")).writeData(output);
                            } else {
                                new JsonResponsePacket(String.format(Constants.MESSAGE_PING_LIST, 0, 0, 0, String.format(Constants.MESSAGE_NOT_FOUND, handshake.serverAddress), "")).writeData(output);
                            }
                            break;
                        case 0x01:
                            packet.writeData(output);
                    }
                    output.flush();
                }
            } else {
                if(Server.getMaxConnections() <= server.connections.get()){
                    new JsonResponsePacket(Constants.MESSAGE_SERVER_FULL).writeData(output);
                    output.flush();
                } else {
                    String dest = server.getMapping(handshake.serverAddress);
                    if(dest != null){
                        Socket serverSocket = new Socket(dest, 25565);
                        handshake.writeData(new MinecraftOutputStream(serverSocket.getOutputStream()));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ProxyThread t1 = new ProxyThread(server, clientSocket.getInputStream(), serverSocket.getOutputStream());
                        ProxyThread t2 = new ProxyThread(server, serverSocket.getInputStream(), clientSocket.getOutputStream());
                        t1.setName("c2s");
                        t2.setName("s2c");
                        t1.start(t2);
                        t2.start(t1);
                        server.connections.addAndGet(2);
                        return;
                    } else {
                        new JsonResponsePacket(String.format(Constants.MESSAGE_NOT_FOUND, handshake.serverAddress)).writeData(output);
                    }
                }
            }
        } catch (EOFException ignored) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
