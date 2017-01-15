package bgu.spl171.net.api.bidi;

import bgu.spl171.net.packets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.*;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by elad on 1/12/17.
 */
public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packet> {
    private Connections<Packet> connections;
    private int connId;
    private static ConcurrentHashMap<Integer, String> activeClients=new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<String> writing;
    private LinkedBlockingDeque<DATA> readData;
    private LinkedBlockingDeque<byte[]> writeData;
    private boolean shouldTerminate = false;
    private String fName;

    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        this.connId =connectionId;
        this.connections=connections;
        writing=new ConcurrentLinkedDeque<>();
        readData=new LinkedBlockingDeque<>();
        writeData=new LinkedBlockingDeque<>();
    }

    @Override
    public void process(Packet message) {
        boolean loggedSuccessfully = handleLOGRQ(message);
        if (loggedSuccessfully) {
            switch (message.getOpCode()) {
                case 1: {
                    handlesRRQ(message);
                    break;
                }
                case 2:{
                    handlesWRQ(message);
                break;
                }
                case 3:{
                    handlesDATA(message);
                        break;
                    }
                case 4:{
                    if(!readData.isEmpty())
                        connections.send(connId,readData.poll());
                    break;
                }
                case 6:{
                    handlesDIRQ();
                    break;
                }
                case 7:{
                    connections.send(connId,new ERROR((short)7));
                    break;
                }
                case 8:{
                    handlesDELRQ(message);
                    break;
                }
                case 10:{
                    handlesDISC();
                    break;
                }
            }
        }
    }

    /**
     * handles the login process
     * @param message
     * @return
     */
    private boolean handleLOGRQ(Packet message){
        boolean ans=false;
        if (message==null){
            connections.send(connId, new ERROR((short) 4));
        }
        else {
            if(message.getOpCode()==7){//if the user chose to connect LOGCQ
                if(activeClients.containsValue((((LOGRQ)message).getUserName()))) {
                    connections.send(connId, new ERROR((short) 7));
                }
                else{
                    activeClients.put(connId,((LOGRQ)message).getUserName());
                    connections.send(connId,new ACK((short)0));
                }
            }
            else{
                if(!(activeClients.containsKey(connId))){
                    connections.send(connId, new ERROR((short)6));
                }
                else{
                    ans=true;
                }
            }
        }
        return ans;
    }
    private void handlesRRQ(Packet message){
        RRQ rrqPack=(RRQ)message;
        try {
            Path path=Paths.get("/Files", rrqPack.getName());
            if (!isWriting(rrqPack.getName())) {
                byte[] data=Files.readAllBytes(path);
                sendData(data);
            }
        }
        catch (FileNotFoundException exp) {
            connections.send(connId, new ERROR((short) 1));
        }
        catch (IOException exp) {
            connections.send(connId, new ERROR((short) 2));
        }
    }
    private void handlesWRQ(Packet message){
        WRQ wrqPack=(WRQ)message;
        String path="/Files/"+wrqPack.getFileName();
        File file = new File(path);
        try {
            if (file.createNewFile() && !isWriting(wrqPack.getFileName())) {
                fName = wrqPack.getFileName();
                writing.add(fName);
                connections.send(connId, new ACK((short) 0));
            } else
                connections.send(connId, new ERROR((short) 5));
        }
        catch (IOException exp) {
            connections.send(connId, new ERROR((short) 2));
        }
    }
    private void handlesDATA(Packet message) {
        DATA dataPack = (DATA) message;
        writeData.addLast(dataPack.getData());
        connections.send(connId, new ACK(dataPack.getBlock()));
        if ((dataPack).getPacketSize() < (1 << 9)) {
            try (FileOutputStream fileOutputStream = new FileOutputStream("/Files/" + fName)) {
                int bLength=0;
                Iterator<byte[]> it = writeData.iterator();
                while (it.hasNext()){
                    bLength=bLength+it.next().length;
                }
                byte[] temp = new byte[bLength];
                Iterator<byte[]> it1 = writeData.iterator();
                int counter=0;
                while (it1.hasNext()){
                    byte[] tempArr=it.next();
                    for (int i = 0; i < tempArr.length; i++) {
                        temp[counter] = tempArr[i];
                        counter++;
                    }
                }
                fileOutputStream.write(temp);
                connections.send(connId, new BCAST(fName, (byte) 1));
            } catch (NoSuchFileException e) {
                connections.send(connId, new ERROR((short) 1));
            } catch (IOException exp) {
                connections.send(connId, new ERROR((short) 2));
            }
        }
    }
    private void handlesDIRQ() {
        String str = "";
        File[] tmp = (new File("/Files")).listFiles();
        ArrayList<File> filesList = new ArrayList<>();
        for (int i = 0; i < tmp.length; i++) {
            filesList.add(tmp[i]);
        }
        if (!filesList.isEmpty()) {
            char divByZero = '\0';
            for (int i = 0; i < filesList.size(); i++) {
                if (filesList.get(i).isFile())
                    str = str + filesList.get(i).getName() + divByZero;
            }
            sendData(str.getBytes());
        }
    }
    private void handlesDELRQ(Packet message){
        DELRQ delrqPack=(DELRQ)message;
        String path = "/Files/"+delrqPack.getFileName();
        //TODO:check if broadcast or send
        try {
            Files.delete(Paths.get(path));
            connections.send(connId,new ACK((short)0));
            connections.broadcast(new BCAST(delrqPack.getFileName(),(byte)0));
        }
        catch (DirectoryNotEmptyException e){
            connections.send(connId,new ERROR((short)0));
        }
        catch (NoSuchFileException e){
            connections.send(connId,new ERROR((short)1));
        }
        catch (IOException x) {
            connections.send(connId,new ERROR((short)2));
        }
    }
    private void handlesDISC(){
        activeClients.remove(connId);
        shouldTerminate=true;
        connections.send(connId,new ACK((short)0));
        connections.disconnect(connId);
    }

        @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private boolean isWriting(String str){
        boolean found=false;
        Iterator<String> it = writing.iterator();
        while (it.hasNext()&&!found){
            if((it.next()).equals(str))
                found=true;
        }
        return found;
    }
    private void sendData(byte[] data){
        int Len = data.length;
        short numOfPackets = 0;
        readData.clear();
        if (Len>(1<<9)) {
            while (Len > (1 << 9)) {
                byte[] ArrayToSend = Arrays.copyOfRange(data, 512 * numOfPackets, 512 * (numOfPackets + 1));
                readData.add(new DATA((short) 512, numOfPackets, ArrayToSend));
                numOfPackets++;
                Len -= 512;
            }
        }
            byte[] newArray = Arrays.copyOfRange(data, data.length - Len, Len);
            readData.add(new DATA((short) Len, numOfPackets, newArray));
            connections.send(connId, readData.poll());

    }
}

