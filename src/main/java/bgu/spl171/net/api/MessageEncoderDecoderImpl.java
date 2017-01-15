package bgu.spl171.net.api;

import bgu.spl171.net.packets.Packet;
import java.nio.charset.StandardCharsets;
import bgu.spl171.net.packets.*;
import com.sun.deploy.util.ArrayUtil;
import java.util.Arrays;
import java.nio.ByteBuffer;


/**
 * Created by nir on 15/01/17.
 */
public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Packet> {
    private byte[] bytesArray = new byte[1 << 10];
    private int length = 0;
    private short packetNumber = 0;
    private int opCodeCounter = 2;
    boolean isFull=false;

    /**
     * not possible to return packets: 1,2,5,6,7
     *
     *
     */
    public Packet decodeNextByte(byte nextByte) {
        switch (packetNumber) {
            case 0:
                if (opCodeCounter > 0) {
                    pushByte(nextByte);
                    opCodeCounter=opCodeCounter-1;
                }
                if (opCodeCounter==0){
                    packetNumber = fromBytesToShort(Arrays.copyOfRange(bytesArray, 0, 2));
                }
                if (packetNumber == 10 || packetNumber == 6) {
                    return checkPacket();
                }
            case 8:
                if (nextByte=='0'){
                    return checkPacket();
                }
                pushByte(nextByte);
            case 3:
                if (opCodeCounter<2 && isFull==false){
                    pushByte(nextByte);
                    opCodeCounter=opCodeCounter+1;
                }
                if (opCodeCounter==2 && isFull==false){
                    opCodeCounter= fromBytesToShort(Arrays.copyOfRange(bytesArray,0,2))+2;
                    isFull=true;
                }
                else
                {
                    pushByte(nextByte);
                    opCodeCounter--;
                    if (opCodeCounter == 0)
                        return checkPacket();
                }
            case 4:
                pushByte(nextByte);
                opCodeCounter++;
                if (opCodeCounter == 2)
                    return checkPacket();
        }
        return null;
    }

    private Packet checkPacket() {
        short opCode = fromBytesToShort(Arrays.copyOfRange(bytesArray,0,2));
        int tempLength = length;
        this.length = 0;
        this.opCodeCounter =2;
        this.packetNumber = 0;

        switch (opCode){
            case 1:
                return new RRQ(new String(bytesArray, 2, tempLength- 3, StandardCharsets.UTF_8));
            case 2:
                return new WRQ(new String(bytesArray, 2, tempLength - 3, StandardCharsets.UTF_8));
            case 3:
                short packetSize = fromBytesToShort(Arrays.copyOfRange(bytesArray,2,4));
                short block = fromBytesToShort(Arrays.copyOfRange(bytesArray,4,6));
                byte[] data = Arrays.copyOfRange(bytesArray,6,bytesArray.length-1);
                return new DATA(packetSize,block,data);
            case 4:
                return new ACK(fromBytesToShort(Arrays.copyOfRange(bytesArray,2,4)));
            case 5:
                short error = fromBytesToShort(Arrays.copyOfRange(bytesArray,2,4));
                return new ERROR(error);
            case 6:
                return new DIRQ();
            case 7:
                return new LOGRQ(new String(bytesArray, 2, tempLength - 3, StandardCharsets.UTF_8));
            case 8:
                return new DELRQ(new String(bytesArray, 2, tempLength - 3, StandardCharsets.UTF_8));
            case 9:
                String answer = new String(bytesArray, 2, 1, StandardCharsets.UTF_8);
                byte deletedOrAdded = '1';
                if(answer == "0"){
                    deletedOrAdded = '0';
                }
                else if(answer == "1"){
                    deletedOrAdded = '1';
                }
                else{
                    throw new IllegalArgumentException("BCAST deleted Or added is not ok");
                }
                String fileName = new String(bytesArray, 3, tempLength - 4, StandardCharsets.UTF_8);
                return new BCAST(fileName,deletedOrAdded);
            case 10:
                return new DISC();
            default:
                return null;
        }
    }


    /**
     * encodes the given message to bytes array
     *
     * @param message the message to encode
     * @return the encoded bytes
     */
    @Override
    public byte[] encode(Packet message) {
        byte[] result = fromShortToBytes(message.getOpCode());
        byte[] answer=null;
        switch (message.getOpCode()){
            case 1:
                RRQ RRQPacket = (RRQ)message;
                answer = mergeArrays(result,RRQPacket.getName().getBytes());
                break;
            case 2:
                WRQ WRQPacket = (WRQ)message;
                answer = mergeArrays(result,WRQPacket.getFileName().getBytes());
                break;
            case 3:
                DATA DATAPacket = (DATA)message;
                byte[] tempDataArr = mergeArrays(result,fromShortToBytes(DATAPacket.getBlock()));
                answer = mergeArrays(tempDataArr,DATAPacket.getData());
                break;
            case 4:
                ACK ACKPacket = (ACK)message;
                answer = mergeArrays(result,fromShortToBytes(ACKPacket.getBlock()));
                break;
            case 5:
                ERROR ErrorPacket = (ERROR)message;
                byte[] tempErrorArr = mergeArrays(result,fromShortToBytes(ErrorPacket.getErrorCode()));
                answer = mergeArrays(tempErrorArr,ErrorPacket.getErrorMessage().getBytes());
                break;
            case 6:
                answer = result;
                break;
            case 7:
                LOGRQ LOGRQPacket= (LOGRQ)message;
                answer = mergeArrays(result,LOGRQPacket.getUserName().getBytes());
                break;
            case 8:
                DELRQ DELRQPacket = (DELRQ)message;
                answer = mergeArrays(result,DELRQPacket.getFileName().getBytes());
                break;
            case 9:
                BCAST BCASTPacket = (BCAST)message;
                byte[] tempBCASTArr;
                if(BCASTPacket.isDeletedOrAdded()=='1')
                    tempBCASTArr = mergeArrays(result,"1".getBytes());
                else if(BCASTPacket.isDeletedOrAdded()=='0')
                    tempBCASTArr = mergeArrays(result,"0".getBytes());
                else
                    throw new IllegalArgumentException("BCAST deleted Or added is not ok");
                answer = mergeArrays(tempBCASTArr,BCASTPacket.getFileName().getBytes());
                break;
            case 10:
                answer = result;
                break;
            default:
                answer = null;
                break;
        }

        if(answer != null){
            answer = mergeArrays(answer,"0".getBytes());
        }

        return answer;
    }




    private short fromBytesToShort(byte[] byteArr) {
        short finish = (short) ((byteArr[0] & 0xff) << 8);
        finish += (short) (byteArr[1] & 0xff);
        return finish;
    }

    private byte[] fromShortToBytes(short num) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((num >> 8) & 0xFF);
        bytes[1] = (byte) (num & 0xFF);
        return bytes;
    }

    private byte[] mergeArrays(byte[] a, byte[] b) {
        byte[] complete = new byte[a.length + b.length];
        for (int i = 0; i < a.length && i < complete.length; i++) {
            complete[i] = a[i];
        }
        for (int i = 0; i < b.length && i < complete.length; i++) {
            complete[i + a.length] = b[i];
        }
        return complete;
    }

    private void pushByte(byte nextByte) {
        if (length >= bytesArray.length) {
            bytesArray = Arrays.copyOf(bytesArray, length * 2);
        }

        bytesArray[length++] = nextByte;
    }
}
