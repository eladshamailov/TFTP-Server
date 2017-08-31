package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.packets.Packet;
import bgu.spl171.net.srv.BaseServer;
import bgu.spl171.net.srv.BlockingConnectionHandler;
import bgu.spl171.net.srv.Reactor;
import bgu.spl171.net.srv.Server;

import java.util.function.Supplier;

import static bgu.spl171.net.srv.Server.threadPerClient;

/**
 * Created by elad on 1/21/17.
 */
public class TPCMain {
    public static <T> Server<T>  threadPerClient(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T> > encoderDecoderFactory) {

        return new BaseServer<T>(port, protocolFactory, encoderDecoderFactory) {
            @Override
            protected void execute(BlockingConnectionHandler<T> handler) {
                new Thread(handler).start();
            }
        };
    }

    public static void main(String []args){
        Supplier<BidiMessagingProtocol<Packet>> protocolFactory = () -> new BidiMessagingProtocolImpl();
        Supplier<MessageEncoderDecoder<Packet>> encDecFactory = () ->  new MessageEncoderDecoderImpl();
        Server<Packet> tpc = threadPerClient(Integer.parseInt(args[0]),protocolFactory,encDecFactory);
        tpc.serve();
    }
}
