package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class RRQ extends Packet {
    private String fileName;
    public String getName() {
        return fileName;
    }
    public RRQ(String name) {
        super((short)1);

        this.fileName=name;
    }
}
