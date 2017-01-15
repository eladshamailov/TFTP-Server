package bgu.spl171.net.api.bidi;

import bgu.spl171.net.packets.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by elad on 1/12/17.
 */
public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<Packet> {
    private Connections<Packet> connections;
    private int connectionId;
    private static ConcurrentHashMap<Integer, String> activeClients=new ConcurrentHashMap<>();
    @Override
    public void start(int connectionId, Connections<Packet> connections) {
        this.connectionId=connectionId;
        this.connections=connections;
    }

    @Override
    public void process(Packet message) {
        if (message==null){
            connections.send(connectionId, new ERROR((short) 4, "Illegal TFTP operation–Unknown Opcode"));
        }
        else{
            if(message.getOpCode()==7){//if the user chose to connect LOGCQ
                if(activeClients.containsValue((((LOGRQ)message).getuserName()))) {
                    connections.send(connectionId, new ERROR((short) 7, "User already logged in–Login username already connected"));
                }
                else{
                    activeClients.put(connectionId,((LOGRQ)message).getuserName());
                    connections.send(connectionId,new ACK((short)0));
                }
            }
            else{
                if(activeClients.containsKey(connectionId)){
                    switch (message.getOpCode()){
                    }
                }
                else{
                    connections.send(connectionId, new ERROR((short) 6, "User not logged in–Any opcode received before Login completes"));
                }
            }
        }
    }
    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
