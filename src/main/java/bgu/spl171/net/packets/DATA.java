package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class DATA extends Packet {
    private short packetSize;
    private short block;
    private byte[] data;

    //TODO:check how we enter the bytes array and the get data
    public DATA(short packetSize, short block, byte[] data) {
        super((short)3);
        this.packetSize=packetSize;
        this.block=block;
        this.data=new byte[data.length];
        for (int i=0;i<data.length;i++){
            this.data[i]=data[i];
        }
    }

    public short getBlock() {
        return block;
    }

    public byte[] getData() {
        return data;
    }

    public short getPacketSize() {
        return packetSize;
    }

}
