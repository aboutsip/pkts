package io.pkts.streams;

import io.pkts.Pcap;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.framer.FramingException;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.TCPPacket;
import io.pkts.protocol.Protocol;
import io.pkts.streams.impl.TcpStreamHandler;
import io.pkts.streams.impl.tcpFSM.TcpStreamFSM;
import org.junit.After;
import org.junit.Test;
import io.pkts.streams.impl.TransportStreamId;
import io.pkts.streams.impl.DefaultTcpStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Simple unit tests for objects used for implementing tcp streams and the example file.
 *
 * @author sebastien.amelinckx@gmail.com
 */
public class TcpStreamTest {
    TransportStreamId id;
    TcpStream stream;

    @After
    public void tearDown(){
        id = null;
        stream = null;
    }

    @Test
    public void basicTcpStreamTest() {
        try {
            Pcap pcap = Pcap.openStream(TcpStreamTest.class.getResourceAsStream("tcp-fsm/few_established_only.pcap"));
            pcap.loop(packet -> {
                if (packet.hasProtocol(Protocol.TCP)){
                    TCPPacket TcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);

                    if(id == null){
                        id = new TransportStreamId(TcpPacket);
                        stream = new DefaultTcpStream(assignGlobalHeader(TcpPacket.getParentPacket().getParentPacket()), id, 1, null);
                    }
                    stream.addPacket(TcpPacket);
                }
                return true;
            });
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to open pcap file");
        }

        assertEquals(stream.getStreamIdentifier(), id);
        assertEquals(stream.getState(), TcpStreamFSM.TcpState.ESTABLISHED);
        assertFalse(stream.isEnded());

        assertEquals(stream.getSrcAddr(), "172.16.100.13");
        assertEquals(stream.getDestAddr(), "172.16.100.10");
        assertEquals(stream.getSrcPort(), 2436);
        assertEquals(stream.getDestPort(), 389);
        assertEquals(stream.getUuid(), 1);

    }

    /**
     * StreamExample002 as a JUnit test to verify the output.
     *
     * @author sebastien.amelinckx@gmail.com
     */
    @Test
    public void StreamExample002Test() throws IOException, FramingException {

        // Step 1 - Open the pcap containing our traffic.
        final Pcap pcap = Pcap.openStream(TcpStreamTest.class.getResourceAsStream("tcp-streams/example_tcp_traffic.pcap"));
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
            System.out.println("Stream " + stream.getUuid() + " has " + stream.getPackets().size() + " packets.");
        }
    }


    private static PcapGlobalHeader assignGlobalHeader(Packet frame) throws PacketParseException {
        PcapGlobalHeader header = null;
        try {
            if (frame.hasProtocol(Protocol.SLL)) {
                header = PcapGlobalHeader.createDefaultHeader(Protocol.SLL);
            } else if (frame.hasProtocol(Protocol.ETHERNET_II)) {
                header = PcapGlobalHeader.createDefaultHeader(Protocol.ETHERNET_II);
            } else {
                throw new PacketParseException(0, "Unable to create the PcapGlobalHeader because the "
                        + "link type isn't recognized. Currently only Ethernet II "
                        + "and Linux SLL (linux cooked capture) are implemented");
            }

        } catch (IOException e){
            e.printStackTrace();
        }
        return header;
    }
}
