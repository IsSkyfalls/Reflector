package xyz.skyfalls.reflector.net;

import java.io.IOException;

public class HandshakePacket extends MinecraftPacket {

  public int protocolVersion;
  public String serverAddress;
  public int serverPort;
  public State nextState;

  public HandshakePacket(MinecraftInputStream in) throws IOException {
    super(in);

    this.protocolVersion = this.in.readVarInt();
    int serverStringLength = this.in.readVarInt();
    byte[] serverStringByte = new byte[serverStringLength];
    this.in.readFully(serverStringByte);
    this.serverAddress = new String(serverStringByte);
    if( this.serverAddress.length() != serverStringLength ) {
      throw new IOException("Malformed string read, size does not match listed size");
    }
    this.serverPort = this.in.readUnsignedShort();
    int readNextState = this.in.readVarInt();
    if( readNextState == 1 ) {
      this.nextState = State.STATUS;
    } else if( readNextState == 2 ) {
      this.nextState = State.LOGIN;
    } else {
      throw new IOException("Next state specified is unrecognized");
    }
  }

  public String getServerAddress() { return this.serverAddress; }
  public int getServerPort() { return this.serverPort; }
}
