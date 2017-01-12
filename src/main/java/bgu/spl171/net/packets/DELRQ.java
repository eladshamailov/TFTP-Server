package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class DELRQ extends Packet {
    private String fileName;
    public DELRQ(String name) {
        super((short)8);
        this.fileName =name;
    }
    public String getFileName() {
        return fileName;
    }
}
