package xyz.skyfalls.reflector.net;

import lombok.AllArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@AllArgsConstructor
public class JsonResponsePacket extends MinecraftPacket{
    /*This packet is both used by:
        Packet Response(Status)
        Packet Disconnect(Login)
     */
    String payload;

    @Override
    public void writeData(MinecraftOutputStream out) throws IOException{
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        MinecraftOutputStream minecraftStream=new MinecraftOutputStream(stream);
        minecraftStream.writeVarInt(0x00);
        minecraftStream.writeString(payload);
        byte data[]=stream.toByteArray();
        out.writeVarInt(data.length);
        out.write(data);
    }
}

