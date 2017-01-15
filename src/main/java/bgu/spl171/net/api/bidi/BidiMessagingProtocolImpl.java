package bgu.spl171.net.api.bidi;

import bgu.spl171.net.packets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
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
                    //TODO:case 3
                    DATA dataPack=(DATA)message;
                    writeData.addLast(dataPack.getData());
                    connections.send(connId, new ACK(dataPack.getBlock()));
                    if((dataPack).getPacketSize()<(1<<9)){

                    }
                }
                case 4:{
                    if(!readData.isEmpty())
                        connections.send(connId,readData.poll());
                    break;
                }
                case 6:{
                    String str="";
                    File[] tmp=(new File("/Files")).listFiles();
                    ArrayList<File> filesList=new ArrayList<>();
                    for(int i=0;i<tmp.length;i++){
                        filesList.add(tmp[i]);
                    }
                    if(!filesList.isEmpty()){
                        char divByZero='\0';
                        for (int i=0;i<filesList.size();i++){
                            if (filesList.get(i).isFile())
                                str=str+filesList.get(i).getName()+divByZero;
                        }
                        sendData(str.getBytes());
                    }
                    break;
                }
                case 7:{
                    connections.send(connId,new ERROR((short)7));
                    break;
                }
                case 8:{
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
                    break;
                }
                case 10:{
                    activeClients.remove(connId);
                    shouldTerminate=true;
                    connections.send(connId,new ACK((short)0));
                    connections.disconnect(connId);
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

    }
}
