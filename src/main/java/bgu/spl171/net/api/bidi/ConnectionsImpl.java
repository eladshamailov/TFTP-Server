package bgu.spl171.net.api.bidi;
import bgu.spl171.net.srv.bidi.ConnectionHandler;
import java.util.HashMap;

/**
 * Created by nir on 11/01/17.
 */
public class ConnectionsImpl<T> implements Connections<T> {
    private HashMap<Integer, ConnectionHandler> connections;
    @Override
    public boolean send(int connectionId, T msg) {
        return false;
    }

    @Override
    public void broadcast(T msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
}
