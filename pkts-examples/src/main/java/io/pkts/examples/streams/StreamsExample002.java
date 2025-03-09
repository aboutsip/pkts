package io.pkts.examples.streams;

import io.pkts.Pcap;
import io.pkts.framer.FramingException;
import io.pkts.packet.TCPPacket;
import io.pkts.streams.Stream;
import io.pkts.streams.StreamId;
import io.pkts.streams.StreamListener;
import io.pkts.streams.TcpStream;
import io.pkts.streams.impl.TcpStreamHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Simple example showing how to use streams.
 *
 * The core pcap support provided by pkts.io is only focusing on each individual
 * packet but quite often your application may be interested in a stream of
 * packets. A stream can mean different things for different protocols. E.g. for
 * UDP, a stream in this context could be all packets sent and received from the
 * same local and remote port-pair (which is how the stream support in pkts.io
 * has defined a UDP stream).
 *
 * For other protocols, there may be other identifiers within the protocol that
 * defines what a stream is. As an example, TCP is a connection oriented protocol that
 * uses signaling packets (SYN, SYN-ACK, RST, FIN) to establish and end connections. A TCP
 * stream is therefore equivalent to the exchange of packets within a TCP connection.
 *
 * This particular example shows how to setup pkts.io and its stream support to
 * consume {@link TcpStream}s.
 *
 * @author sebastien.amelinckx@gmail.com
 */
public class StreamsExample002 {

    public static void main(final String[] args) throws IOException, FramingException {

        // Step 1 - Open the pcap containing our traffic.
        final Pcap pcap = Pcap.openStream("my_tcp_traffic.pcap");
        // Step 2 - Instead of implementing our own PacketHandler we will be
        //          using a TcpStreamHandler provided for us by the io.pkts.streams
        //          library. It has a StreamHandler (which obviously
        //          implements the FrameHandler) that will detect new tcp streams
        //          and call a StreamListener when appropriate.
        final TcpStreamHandler streamHandler = new TcpStreamHandler();

        // Step 3 - In this simple example we will just supply a very basic
        //          StreamListener for TCP. All we will do is
        //          print to std out when a new event occurs for a stream.
        streamHandler.addStreamListener(new StreamListener<TCPPacket>() {

            @Override
            public void startStream(final Stream<TCPPacket> stream, final TCPPacket packet) {

                TcpStream tcpStream = (TcpStream) stream;

                System.out.println("New TCP stream detected. Stream n°" + tcpStream.getUuid() + "\n" +
                        " Stream id: " + stream.getStreamIdentifier());
                System.out.println("First packet seq num was: " + packet.getSequenceNumber());
            }

            @Override
            public void packetReceived(final Stream<TCPPacket> stream, final TCPPacket packet) {
                TcpStream tcpStream = (TcpStream) stream;
                System.out.println("Received a new TCP packet for stream: " + tcpStream.getUuid());
            }

            @Override
            public void endStream(final Stream<TCPPacket> stream) {
                TcpStream tcpStream = (TcpStream) stream;
                System.out.println("The stream ended. Stream n°" + tcpStream.getUuid());
            }
        });

        // Step 4 - Call the loop function as usual but pass in the TcpStreamHandler
        //          instead of your own "raw" FrameHandler.
        pcap.loop(streamHandler);

        // Step 5 - Do whatever with the streams and packets inside
        Map<StreamId, TcpStream> allStreams = streamHandler.getStreams();

        ArrayList<TcpStream> streams = new ArrayList<TcpStream>(allStreams.values());

        for(TcpStream stream : streams) {
            stream.getPackets().forEach(packet -> {
                System.out.println("Packet seq num: " + packet.getSequenceNumber());
            });
        }
    }
}
