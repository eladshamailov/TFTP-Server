package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class WRQ extends Packet {
    private String fileName;
    public String getFileName() {
        return fileName;
    }
    public WRQ(String name) {
        super((short)2);
        this.fileName =name;
    }

}
