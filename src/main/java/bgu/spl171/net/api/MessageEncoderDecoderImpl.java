
package bgu.spl171.net.api;
import bgu.spl171.net.packets.Packet;
import java.nio.charset.StandardCharsets;
import bgu.spl171.net.packets.*;
import java.util.Arrays;

/**
 * Created by nir on 15/01/17.
 */
public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Packet> {
    private byte[] arr = new byte[1 << 10];
    private short packetNumber = -1;
    private int checks[] = {0, 1, 5, 6};
    private int[] counter = {0, 2, 0, 2, 0, 2, 0};
    private String packetNumValidation = "123478";
    private int[] validation = {1, 2, 3, 4, 7, 8,2,2};
    final char endOfLine='\0';

    @Override
    public Packet decodeNextByte(byte nextByte) {
        boolean found = false;
        switch (packetNumber) {
            case -1: {
                found = otherCaseHandler(nextByte, found);
                if (found)
                    return getNewPacket();
                break;
            }
            case 1:
                found=checkIfEndOfLine(nextByte,found);
                if (found)
                    return getNewPacket();
                break;
            case 2:
                found=checkIfEndOfLine(nextByte,found);
                if (found)
                    return getNewPacket();
                break;
            case 7:
                found=checkIfEndOfLine(nextByte,found);
                if (found)
                    return getNewPacket();
                break;
            case 8: {
                found=checkIfEndOfLine(nextByte,found);
                if (found)
                    return getNewPacket();
                break;
            }
            case 3:
                found= caseThreeHandler(nextByte,found);
                if (found)
                    return getNewPacket();
                break;
            case 4:
                found=true;
                byteInsert(nextByte);
                counter[1]=counter[1]+1;
                counter[3]=counter[3]+1;
                if (counter[1] != 2)
                    found=false;
                if (found)
                    return getNewPacket();
                break;
        }
        return null;
    }
        private boolean otherCaseHandler(byte nextByte,boolean found) {
            if(counter[1]<0)
                found=false;
            else {
                if (counter[1] > 0) {
                    counter[3]=counter[3]-1;
                    counter[1]=counter[1]-1;
                    byteInsert(nextByte);
                }
                if (counter[1] == 0) {
                    packetNumber = bytesToShort(Arrays.copyOfRange(arr, 0, 2));
                    if (packetNumber == 6 || packetNumber == 10)
                        found = true;
                    else {
                        boolean send = true;
                        for (int i = 0; i < validation.length&&send; i++)
                            if (packetNumber == validation[i])
                                send = false;
                        if (send)
                            found = true;
                    }
                }
            }
            return found;
        }
    private boolean caseThreeHandler(byte nextByte, boolean found){
        if(counter[5]<0){
            if(counter[6]>0){
                counter[6]=counter[6]-1;
                byteInsert(nextByte);
                if (counter[6] == 0)
                    found=true;
            }
        }
        else{
            if(counter[5]!=0){
                byteInsert(nextByte);
                counter[5]=counter[5]-1;
            }
            else{
                int valueToAdd=bytesToShort(Arrays.copyOfRange(arr, 2, 4));
                counter[6] = valueToAdd+2;
                counter[5]=counter[5]-1;
            }
        }
        return found;
    }
    private boolean checkIfEndOfLine(byte nextByte, boolean found) {
        if(nextByte!=endOfLine)
            byteInsert(nextByte);
        else
            found=true;
        return found;
    }
        @Override
    public byte[] encode(Packet message) {
        byte[]ans;
        byte[] endLine = {((byte)endOfLine)};
        switch (message.getOpCode()) {
            case 3:
                ans=DATAEncode((DATA)message);
                return ans;
            case 4:
                ACK ackPack = (ACK) message;
                return concatenateArrays(shortToBytes(message.getOpCode()), shortToBytes(ackPack.getBlock()));
            case 5:
                ans=ERROREncode((ERROR)message,endLine );
                return ans;
            case 9:
                byte[] tmp;
                byte[] bcastArr;
                BCAST bcastPack = (BCAST)message;
                byte[] arr1={0};
                byte[]arr2={1};
                if (bcastPack.isDeletedOrAdded()=='1')
                    bcastArr = concatenateArrays(shortToBytes(message.getOpCode()), arr2);
                else
                    bcastArr = concatenateArrays(shortToBytes(message.getOpCode()), arr1);
                tmp = concatenateArrays(bcastArr, bcastPack.getFileName().getBytes());
                return concatenateArrays(tmp,endLine);
            default:
                return null;
        }
    }
    public byte[] DATAEncode(DATA dataPack){
        byte[] sum = shortToBytes((short)3);
        counter[4]=counter[4]+1;
        sum = concatenateArrays(sum, shortToBytes(dataPack.getPacketSize()));
        sum = concatenateArrays(sum, shortToBytes(dataPack.getBlock()));
        return concatenateArrays(sum, dataPack.getData());
    }
    public byte[] ERROREncode(ERROR errorPack,byte []zeroArr) {
        byte[] tmp;
        byte[] tempErrorArr = concatenateArrays(shortToBytes((short)5), shortToBytes(errorPack.getErrorCode()));
        tmp = concatenateArrays(tempErrorArr, errorPack.getErrorMessage().getBytes());
        return concatenateArrays(tmp, zeroArr);
    }
    private byte[] concatenateArrays(byte[] arr1, byte[] arr2) {
        int len1 = arr1.length;
        int len2 = arr2.length;
        byte[] c= new byte[len1+len2];
        System.arraycopy(arr1, 0, c, 0, len1);
        System.arraycopy(arr2, 0, c, len1, len2);
        return c;
    }

    private Packet getNewPacket() {
        byte[]tmp= Arrays.copyOfRange(arr, 0, 2);
        short opCode = bytesToShort(tmp);
        int length = counter[0]-2;
        counter[0] = 0;
        counter[1] = 2;
        counter[2]=0;
        counter[3]=2;
        counter[5] = 2;
        counter[6] = 0;
        packetNumber = -1;
        int offset=2;
        String ans=new String(arr, offset, length, StandardCharsets.UTF_8);
        if(opCode==1)
            return new RRQ(ans);
        else if(opCode==2)
            return new WRQ(ans);
        else if(opCode==6)
            return new DIRQ();
        else if(opCode==7)
            return new LOGRQ(ans);
        else if(opCode==8)
            return new DELRQ(ans);
        else if(opCode==10)
            return new DISC();
        else{
            switch (opCode) {
                case 1:
                    return new RRQ(ans);
                case 2:
                    return new WRQ(ans);
                case 3:
                    short packetSize = bytesToShort(Arrays.copyOfRange(arr, offset, 4));
                    short block = bytesToShort(Arrays.copyOfRange(arr, 4, 6));
                    byte[] data = Arrays.copyOfRange(arr, 6, 6 + packetSize);
                    counter[4] = counter[4] + 1;
                    return new DATA(packetSize, block, data);
                case 4:
                    return new ACK(bytesToShort(Arrays.copyOfRange(arr, offset, 4)));
                    default:
                        return null;
            }
        }
    }
    private void byteInsert(byte nextByte) {
        if (isNewCopy(nextByte)) {
            int length=counter[0]*2;
            arr = Arrays.copyOf(arr, length);
        }
        int size=counter[0]++;
        arr[size] = nextByte;
    }
    private boolean isNewCopy(byte nextByte){
        boolean newCopy;
        if(counter[0] < arr.length)
            newCopy=false;
        else
            newCopy=true;
        return newCopy;
    }
    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }
}