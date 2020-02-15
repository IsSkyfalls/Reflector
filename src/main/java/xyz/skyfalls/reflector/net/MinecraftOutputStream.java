package xyz.skyfalls.reflector.net;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.OutputStream;

public class MinecraftOutputStream extends DataOutputStream {

  public MinecraftOutputStream(OutputStream out) throws IOException {
    super(out);
  }

  public void writeVarInt(int value) throws IOException {
    do {
      byte temp = (byte)(value & 0b01111111);
      value >>>= 7;
      if (value != 0) {
        temp |= 0b10000000;
      }
      this.writeByte(temp);
    } while (value != 0);
  }

  public void writeVarLong(long value) throws IOException {
    do {
      byte temp = (byte)(value & 0b01111111);
      value >>>= 7;
      if (value != 0) {
        temp |= 0b10000000;
      }
      this.writeByte(temp);
    } while (value != 0);
  }

  public void writeString(String s) throws IOException{
    byte[] data=s.getBytes();
    writeVarInt(data.length);
    this.write(data);
  }
}
