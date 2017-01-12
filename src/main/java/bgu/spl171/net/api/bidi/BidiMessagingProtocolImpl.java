package bgu.spl171.net.api.bidi;

/**
 * Created by elad on 1/12/17.
 */
public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {
    @Override


    public void start(int connectionId, Connections<T> connections) {

    }

    @Override
    public void process(T message) {

    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
