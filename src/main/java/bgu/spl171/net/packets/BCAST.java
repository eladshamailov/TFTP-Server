package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class BCAST extends Packet {
    private String fileName;
    private byte deletedOrAdded;
    public BCAST(String name,byte deletedOrAdded) {
        super((short)9);
        this.fileName =name;
        this.deletedOrAdded=deletedOrAdded;
    }
    public String getFileName() {
        return fileName;
    }

    public byte isDeletedOrAdded() {
        return deletedOrAdded;
    }
}
