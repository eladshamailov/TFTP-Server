package bgu.spl171.net.api.bidi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by elad on 1/12/17.
 */
public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {
    private Connections<T> connections;
    private int connectionId;
    private static ConcurrentHashMap<Integer, String> activeClients=new ConcurrentHashMap<>();
    @Override


    public void start(int connectionId, Connections<T> connections) {
        this.connectionId=connectionId;
        this.connections=connections;
    }

    @Override
    public void process(T message) {
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
