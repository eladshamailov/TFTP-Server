package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class ACK extends Packet {
    private short block;
    public ACK(short block) {
        super((short)4);
        this.block=block;
    }
    public short getBlock() {
        return block;
    }}
