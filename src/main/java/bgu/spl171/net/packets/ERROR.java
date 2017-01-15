package bgu.spl171.net.packets;

/**
 * Created by elad on 1/12/17.
 */
public class ERROR extends Packet {
    private short errorCode;
    private String errorMassage;
    public ERROR(short errorCode) {
        super((short)5);
        this.errorCode=errorCode;
        switch (errorCode){
            case 0: {
                this.errorMassage = "Not defined, see error message (if any).";
                break;
            }
            case 1: {
                this.errorMassage = "File not found–RRQ\\DELRQ of non-existing file";
                break;
            }
            case 2:{
                this.errorMassage = "Access violation–File cannot be written, read or deleted.";
                break;
            }
            case 3:{
                this.errorMassage = "Access violation–File cannot be written, read or deleted.Disk full or allocation exceeded–No room in disk.";
                break;
            }
            case 4:{
                this.errorMassage = "Illegal TFTP operation–Unknown Opcode.";
                break;
            }
            case 5:{
                this.errorMassage = "File already exists–File name exists on WRQ.";
                break;
            }
            case 6:{
                this.errorMassage = "User not logged in–Any opcode received before Login completes.";
                break;
            }
            case 7:{
                this.errorMassage = "User already logged in–Login usernamealready connected.";
                break;
            }
            default:{
                this.errorMassage = "Not defined, see error message (if any).";
                break;
            }
        }
    }

    public short getErrorCode() {
        return errorCode;
    }

    public String getErrorMassage() {
        return errorMassage;
    }
}
