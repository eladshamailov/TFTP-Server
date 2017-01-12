package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class ERROR extends Packet {
    private short errorCode;
    private String errorMassage;
    public ERROR(short errorCode, String errorMassage) {
        super((short)5);
        this.errorCode=errorCode;
        this.errorMassage=errorMassage;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public String getErrorMassage() {
        return errorMassage;
    }
}
