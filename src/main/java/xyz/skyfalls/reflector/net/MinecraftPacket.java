package xyz.skyfalls.reflector.net;

import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@NoArgsConstructor
public class MinecraftPacket {
    /* Follows the protocol spec layed out at http://wiki.vg/Protocol */

    public static enum State {
        HANDSHAKING,
        LOGIN,
        PLAY,
        STATUS
    }

    public static enum BoundTowards {
        CLIENT,
        SERVER
    }

    private int packetId;
    private int length;
    private int lengthRaw;
    protected MinecraftInputStream in;
    private boolean compressed = false;
    private boolean readOnly = false;
    private byte[] data;
    private byte[] dataRaw;

    public MinecraftPacket(MinecraftInputStream in) throws IOException{
        this(in, false, false);
    }

    public MinecraftPacket(MinecraftInputStream in, boolean compressed) throws IOException{
        this(in, compressed, false);
    }

    public MinecraftPacket(MinecraftInputStream in, boolean compressed, boolean readOnly) throws IOException{
        this.readOnly = readOnly;
        this.compressed = compressed;

        this.lengthRaw = in.available();
        //this.lengthRaw = in.readVarInt();

        this.dataRaw = new byte[this.lengthRaw];
        in.readFully(this.dataRaw);

        this.data = this.dataRaw;
        this.length = this.lengthRaw;

        this.in = new MinecraftInputStream(new ByteArrayInputStream(this.data));
        this.packetId = this.in.readVarInt();
    }

    public int getPacketId(){
        return this.packetId;
    }

    public int getLength(){
        return this.length;
    }

    public void setPacketId(int packetId){
        if(!this.readOnly){
            this.packetId = packetId;
        }
    }

    public void writeData(MinecraftOutputStream out) throws IOException{
        out.writeVarInt(this.lengthRaw);
        out.write(this.dataRaw);
    }
}
