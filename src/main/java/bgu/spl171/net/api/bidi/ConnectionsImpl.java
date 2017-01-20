package bgu.spl171.net.api.bidi;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nir on 11/01/17.
 */
public class ConnectionsImpl<Packet> implements Connections<Packet> {
    private ConcurrentHashMap<Integer, ConnectionHandler> connections=new ConcurrentHashMap<>();
    private AtomicInteger connId=new AtomicInteger(0);
    @Override
    public boolean send(int connectionId, Packet msg) {
        if(connections==null)
            return false;
        else {
            if (connections.containsKey(connectionId)) {
                connections.get(connectionId).send(msg);
                return true;
            } else
                return false;
        }
    }

    @Override
    public void broadcast(Packet msg) {
        Set<Integer> tmpSetOfKeys=connections.keySet();
        if(!(tmpSetOfKeys.isEmpty())) {
            for (int i = 0; i < tmpSetOfKeys.size(); i++) {
                connections.get(i).send(msg);
            }
        }
    }

    @Override
    public void disconnect(int connectionId) throws IOException{
        //TODO:check if the close() needed
        if( connections.containsKey(connectionId)) {
            connections.get(connectionId).close();
            connections.remove(connectionId);
        }
        }

        public int registerNewId(){
        int ans=connId.getAndIncrement();
        return ans;
    }

    public int getCurrentId(){
            return connId.get();
    }

    public int addNewConnection(ConnectionHandler cnh){
        int newId = registerNewId();
        connections.put(newId,cnh);
        return newId;
    }
}
