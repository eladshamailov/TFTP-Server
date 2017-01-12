package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class BCAST extends Packet {
    private String fileName;
    private boolean deletedOrAdded;
    public BCAST(String name,boolean deletedOrAdded) {
        super((short)9);
        this.fileName =name;
        this.deletedOrAdded=deletedOrAdded;
    }
    public String getFileName() {
        return fileName;
    }

    public boolean isDeletedOrAdded() {
        return deletedOrAdded;
    }
}
