package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class LOGRQ extends Packet {
    private String userName;
    public LOGRQ(String userName) {
        super((short)7);
        this.userName=userName;
    }
    public String getuserName() {
        return userName;
    }
}
