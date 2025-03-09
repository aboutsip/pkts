package io.pkts.streams.impl;

import java.util.ArrayList;

import io.hektor.fsm.FSM;
import io.pkts.Pcap;
import io.pkts.packet.TCPPacket;
import io.pkts.protocol.Protocol;
import io.pkts.streams.StreamsTestBase;
import io.pkts.streams.impl.tcpFSM.TcpStreamContext;
import io.pkts.streams.impl.tcpFSM.TcpStreamData;
import io.pkts.streams.impl.tcpFSM.TcpStreamFSM;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link TcpStreamFSM}.
 *
 * @author sebastien.amelinckx@gmail.com
 */
public class TcpStreamFSMTest {

    FSM stream;
    TcpStreamContext ctx;
    TcpStreamData data;
    ArrayList<TCPPacket> packets;

    @Before
    public void setUp(){
        ctx = new TcpStreamContext();
        data = new TcpStreamData();
        stream = TcpStreamFSM.definition.newInstance("uuid-123",ctx, data);
        stream.start();
    }

    @Test
    public void testFewEstablishedOnly() {
        packets = retrievePackets("tcp-fsm/few_established_only.pcap");
        assertEquals(TcpStreamFSM.TcpState.INIT, stream.getState());
        for (TCPPacket packet : packets) {
            stream.onEvent(packet);
            assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());
        }
    }

    @Test
    public void testEstablishedOnly() {
        packets = retrievePackets("tcp-fsm/established_only.pcap");
        assertEquals(TcpStreamFSM.TcpState.INIT, stream.getState());
        for (TCPPacket packet : packets) {
            stream.onEvent(packet);
            assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());
        }
    }

    @Test
    public void testStartFin() {
        packets = retrievePackets("tcp-fsm/start_fin.pcap");

        assertEquals(TcpStreamFSM.TcpState.INIT, stream.getState());
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_2, stream.getState());
        stream.onEvent(packets.get(2));
        assertEquals(TcpStreamFSM.TcpState.CLOSED_1_CLOSING_2, stream.getState());
        stream.onEvent(packets.get(3));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());
    }

    @Test
    public void testGracefulFin1Fin2() {
        packets = retrievePackets("tcp-fsm/graceful_fin1_fin2.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        packets.remove(0);
        packets.remove(0);

        // process established connection until start of graceful end
        int count = 0;
        for (TCPPacket packet : packets) {
            count++;
            stream.onEvent(packet);
            if (stream.getState() != TcpStreamFSM.TcpState.ESTABLISHED){
                assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());
                break;
            }
        }

        stream.onEvent(packets.get(count));
        assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_2, stream.getState());

        stream.onEvent(packets.get(++count));
        assertEquals(TcpStreamFSM.TcpState.CLOSED_1_CLOSING_2, stream.getState());

        stream.onEvent(packets.get(++count));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());

    }

    @Test
    public void testGracefulFin1Fin2PlusAck() {
        packets = retrievePackets("tcp-fsm/graceful_fin1_fin2_+_ack.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        packets.remove(0);
        packets.remove(0);

        // process established connection until start of gracefull end
        int count = 0;
        for (TCPPacket packet : packets) {
            count++;
            stream.onEvent(packet);
            if (stream.getState() != TcpStreamFSM.TcpState.ESTABLISHED){
                assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());
                break;
            }
        }

        stream.onEvent(packets.get(count)); // case FIN + ACK of first FIN
        assertEquals(TcpStreamFSM.TcpState.CLOSED_1_CLOSING_2, stream.getState());

        stream.onEvent(packets.get(++count));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());
    }

    @Test
    public void testGracefulSimultaneous() {
        packets = retrievePackets("tcp-fsm/graceful_simultaneous.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        packets.remove(0);
        packets.remove(0);

        // process established connection until graceful end
        int count = 0;
        for (TCPPacket packet : packets) {
            count++;
            stream.onEvent(packet);
            if (stream.getState() != TcpStreamFSM.TcpState.ESTABLISHED){
                assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());
                break;
            }
        }

        stream.onEvent(packets.get(count));
        assertEquals(TcpStreamFSM.TcpState.CLOSING_1_CLOSING_2, stream.getState());

        stream.onEvent(packets.get(++count));
        assertEquals(TcpStreamFSM.TcpState.CLOSING_1_CLOSED_2, stream.getState());

        stream.onEvent(packets.get(++count));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());
    }

    @Test
    public void testAbruptInit() {
        packets = retrievePackets("tcp-fsm/abrupt_init.pcap");

        assertEquals(TcpStreamFSM.TcpState.INIT, stream.getState());
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());
    }

    @Test
    public void testAbruptHandshake() {
        packets = retrievePackets("tcp-fsm/abrupt_handshake.pcap");

        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(2));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());
    }

    @Test
    public void testAbruptEstablished() {
        packets = retrievePackets("tcp-fsm/abrupt_established.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        packets.remove(0);
        packets.remove(0);

        // process established connection until abrupt end
        for (TCPPacket packet : packets) {
            stream.onEvent(packet);
            if (stream.getState() != TcpStreamFSM.TcpState.ESTABLISHED){
                assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());
                break;
            }
        }
    }

    @Test
    public void testAbruptFin1() {
        packets = retrievePackets("tcp-fsm/abrupt_fin1.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        packets.remove(0);
        packets.remove(0);

        // process established connection until graceful end
        int count = 0;
        for (TCPPacket packet : packets) {
            count++;
            stream.onEvent(packet);
            if (stream.getState() != TcpStreamFSM.TcpState.ESTABLISHED){
                assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());
                break;
            }
        }
        // abrupt end in FIN_WAIT_1
        stream.onEvent(packets.get(count));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());

    }

    @Test
    public void testAbruptFin2() {
        packets = retrievePackets("tcp-fsm/abrupt_fin2.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        packets.remove(0);
        packets.remove(0);

        // process established connection until graceful end
        int count = 0;
        for (TCPPacket packet : packets) {
            count++;
            stream.onEvent(packet);
            if (stream.getState() != TcpStreamFSM.TcpState.ESTABLISHED){
                assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());
                break;
            }
        }

        stream.onEvent(packets.get(count));
        assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_2, stream.getState());

        // abrupt end in FIN_WAIT_2
        stream.onEvent(packets.get(++count));
        assertEquals(TcpStreamFSM.TcpState.CLOSED, stream.getState());

    }

    @Test
    public void testSynEndEstablished() {
        packets = retrievePackets("tcp-fsm/established_syn_duplicate.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(2));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());

        // syn packet but it is a duplicate, should have no effect on the FSM state
        stream.onEvent(packets.get(3));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());

    }

    @Test
    public void testFin1SynDuplicate() {
        packets = retrievePackets("tcp-fsm/fin1_syn_duplicate.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(2));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());
        stream.onEvent(packets.get(3));
        assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());

        // syn packet but it is a duplicate, should have no effect on the FSM state
        stream.onEvent(packets.get(4));
        assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());

    }

    @Test
    public void testEstablishedSynPortsReused() {
        packets = retrievePackets("tcp-fsm/established_syn_ports_reused.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(2));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());

        // 3 data packets
        stream.onEvent(packets.get(3));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());
        stream.onEvent(packets.get(4));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());
        stream.onEvent(packets.get(5));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());

        // new syn reusing ports (marked in wireshark), FSM should consider the connection closed and ports reused
        stream.onEvent(packets.get(6));
        assertEquals(TcpStreamFSM.TcpState.CLOSED_PORTS_REUSED, stream.getState());

    }

    @Test
    public void testFin1SynPortsReused() {
        packets = retrievePackets("tcp-fsm/fin1_syn_ports_reused.pcap");

        // syn exchange
        stream.onEvent(packets.get(0));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(1));
        assertEquals(TcpStreamFSM.TcpState.HANDSHAKE, stream.getState());
        stream.onEvent(packets.get(2));
        assertEquals(TcpStreamFSM.TcpState.ESTABLISHED, stream.getState());
        stream.onEvent(packets.get(3));
        assertEquals(TcpStreamFSM.TcpState.FIN_WAIT_1, stream.getState());

        // new syn reusing ports (marked in wireshark), FSM should consider the connection closed and ports reused
        stream.onEvent(packets.get(4));
        assertEquals(TcpStreamFSM.TcpState.CLOSED_PORTS_REUSED, stream.getState());

    }

    private static ArrayList<TCPPacket> retrievePackets(String filename){
        ArrayList<TCPPacket> packets = new ArrayList<>();
        try {
            Pcap pcap = Pcap.openStream(StreamsTestBase.class.getResourceAsStream(filename));
            pcap.loop(packet -> {
                if (packet.hasProtocol(Protocol.TCP)) {
                    packets.add((TCPPacket) packet.getPacket(Protocol.TCP));
                }
                return true;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return packets;
    }

}
