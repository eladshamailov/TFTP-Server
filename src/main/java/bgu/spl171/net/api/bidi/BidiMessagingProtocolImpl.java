package bgu.spl171.net.api.bidi;

import bgu.spl171.net.packets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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
                    break;
                }
                case 2:{
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
                break;
                }
                case 3:{
                    DATA dataPack=(DATA)message;
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
    @Override
    public boolean shouldTerminate() {
        return false;
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

    }
}
















