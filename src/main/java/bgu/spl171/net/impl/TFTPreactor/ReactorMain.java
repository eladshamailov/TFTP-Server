package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.packets.Packet;
import bgu.spl171.net.srv.Server;

import java.util.function.Supplier;
import static bgu.spl171.net.srv.Server.reactor;
/**
 * Created by elad on 1/21/17.
 */
public class ReactorMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Supplier<BidiMessagingProtocol<Packet>> protocolF = () -> new BidiMessagingProtocolImpl();
        Supplier<MessageEncoderDecoder<Packet>> encDecF = () -> new MessageEncoderDecoderImpl();
        Server<Packet> reactor = reactor(4, port, protocolF, encDecF);
        reactor.serve();
    }
}
