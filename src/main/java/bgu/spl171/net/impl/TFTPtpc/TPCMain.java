package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessageEncoderDecoderImpl;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl171.net.packets.Packet;
import bgu.spl171.net.srv.Server;

import java.util.function.Supplier;

import static bgu.spl171.net.srv.Server.threadPerClient;

/**
 * Created by elad on 1/21/17.
 */
public class TPCMain {
    public static void main(String[] args) {
        int port=Integer.parseInt(args[0]);
        Supplier<BidiMessagingProtocol<Packet>> protocolF = () -> new BidiMessagingProtocolImpl();
        Supplier<MessageEncoderDecoder<Packet>> encDecF = () -> new MessageEncoderDecoderImpl();
        Server<Packet> tpc = threadPerClient(port, protocolF, encDecF);
        tpc.serve();
    }
}
