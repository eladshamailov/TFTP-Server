package bgu.spl171.net.impl.TFTPreactor;

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
import static bgu.spl171.net.srv.Server.reactor;
/**
 * Created by elad on 1/21/17.
 */
public class ReactorMain {
//    public static void main(String[] args) {
//   //     int port = Integer.parseInt(args[0]);
//        Supplier<BidiMessagingProtocol<Packet>> protocolF = () -> new BidiMessagingProtocolImpl();
//        Supplier<MessageEncoderDecoder<Packet>> encDecF = () -> new MessageEncoderDecoderImpl();
//        Server<Packet> reactor = reactor(4, 7777, protocolF, encDecF);
//        reactor.serve();


    /**
     * The main loop of the server, Starts listening and handling new clients.
     */
//    void serve();

    /**
     *This function returns a new instance of a thread per client pattern server
     * @param port The port for the server socket
     * @param protocolFactory A factory that creats new MessagingProtocols
     * @param encoderDecoderFactory A factory that creats new MessageEncoderDecoder
     * @param <T> The Message Object for the protocol
     * @return A new Thread per client server
     */
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

    /**
     * This function returns a new instance of a reactor pattern server
     * @param nthreads Number of threads available for protocol processing
     * @param port The port for the server socket
     * @param protocolFactory A factory that creats new MessagingProtocols
     * @param encoderDecoderFactory A factory that creats new MessageEncoderDecoder
     * @param <T> The Message Object for the protocol
     * @return A new reactor server
     */
    public static <T> Server<T> reactor(
            int nthreads,
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory) {
        return new Reactor<T>(nthreads, port, protocolFactory, encoderDecoderFactory);
    }

    public static void main(String []args){
        Supplier<BidiMessagingProtocol<Packet>> protocolFactory = () -> new BidiMessagingProtocolImpl();
        Supplier<MessageEncoderDecoder<Packet>> encDecFactory = () -> new MessageEncoderDecoderImpl();
        Server<Packet> Reactor1 = reactor(4, Integer.parseInt(args[0]), protocolFactory, encDecFactory);
        Reactor1.serve();
    }
    }