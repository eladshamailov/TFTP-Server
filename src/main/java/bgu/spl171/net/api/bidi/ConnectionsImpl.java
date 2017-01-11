package bgu.spl171.net.api.bidi;

/**
 * Created by nir on 11/01/17.
 */
public class ConnectionsImpl<T> implements Connections<T> {
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
