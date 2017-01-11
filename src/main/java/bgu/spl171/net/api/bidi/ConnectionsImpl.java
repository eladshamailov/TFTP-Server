package bgu.spl171.net.api.bidi;
import bgu.spl171.net.srv.bidi.ConnectionHandler;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nir on 11/01/17.
 */
public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, ConnectionHandler> connections=new ConcurrentHashMap<>();
    private AtomicInteger connId=new AtomicInteger(0);
    @Override
    public boolean send(int connectionId, T msg) {
        if( connections.containsKey(connectionId)){
            connections.get(connectionId).send(msg);
            return true;
        }
        else
            return false;
    }

    @Override
    public void broadcast(T msg) {
        for(int i=0;i<connections.size();i++){
            connections.get(i).send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if( connections.containsKey(connectionId))
            connections.remove(connectionId);
        }

}
