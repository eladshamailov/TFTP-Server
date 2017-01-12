package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class Packet {
    private short opCode;

    public Packet(short opCode) {
        this.opCode = opCode;
    }

    public short getOpCode() {
        return opCode;
    }
}
