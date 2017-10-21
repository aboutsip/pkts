/**
 * 
 */
package io.pkts.packet.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.PcapOutputStream;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.packet.*;
import io.pkts.protocol.Protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class TransportPacketFactoryImplTest {

    final TransportPacketFactory factory = PacketFactory.getInstance().getTransportFactory();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    private void createUDPPacket(final Buffer payload) {

    }

    /**
     * Make sure that the default values of a UDP packet is correct as specified
     * by the javadoc of {@link TransportPacketFactory#createUDP(Buffer)}
     */
    @Test
    public void testCreateDefaultUDPPacket() throws Exception {
        final UDPPacket pkt = this.factory.createUDP(Buffers.wrap("this is some random text"));
        assertThat(((MACPacket)pkt.getPacket(Protocol.ETHERNET_II)).getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(((MACPacket)pkt.getPacket(Protocol.ETHERNET_II)).getDestinationMacAddress(), is("00:00:00:00:00:00"));
        assertThat((pkt.getParentPacket()).getSourceIP(), is("127.0.0.1"));
        assertThat(pkt.getSourcePort(), is(0));
        assertThat((pkt.getParentPacket()).getDestinationIP(), is("127.0.0.1"));
        assertThat(pkt.getDestinationPort(), is(0));
        assertThat(pkt.getPayload().toString(), is("this is some random text"));
    }

    @Test
    public void testCreateUDPPacket() throws Exception {
        createAndVerifyUDP("hello world");
        createAndVerifyUDP("hello");
        createAndVerifyUDP(null);

    }

    /**
     * Helper method for serializing the {@link TransportPacket} to a byte
     * stream and then it will read it again and spit it back out.
     * 
     * @param pkt
     * @return
     */
    private TransportPacket serializeAndDeserialize(final TransportPacket pkt) throws Exception {

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PcapGlobalHeader header = PcapGlobalHeader.createDefaultHeader();
        final PcapOutputStream out = PcapOutputStream.create(header, outputStream);
        out.write(pkt);
        out.close();

        final InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
        final Pcap pcap = Pcap.openStream(is);
        final AtomicReference<TransportPacket> result = new AtomicReference<TransportPacket>();
        pcap.loop(new PacketHandler() {
            @Override
            public boolean nextPacket(final Packet frame) throws IOException {
                if (frame.hasProtocol(Protocol.UDP)) {
                    result.set((TransportPacket) frame.getPacket(Protocol.UDP));
                } else if (frame.hasProtocol(Protocol.TCP)) {
                    result.set((TransportPacket) frame.getPacket(Protocol.TCP));
                }

                return true;
            }
        });

        return result.get();
    }

    /**
     * Create and verify a simple UDP packet.
     */
    private void createAndVerifyUDP(final String payload) throws Exception {
        final Buffer buffer = payload != null ? Buffers.wrap(payload) : null;
        final TransportPacket pkt = this.factory.create(Protocol.UDP, "10.36.10.10", 9999, "192.168.0.10", 7654,
                buffer);
        assertThat(pkt, not((TransportPacket) null));
        assertThat(((MACPacket)pkt.getParentPacket().getParentPacket()).getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat((pkt.getParentPacket()).getSourceIP(), is("10.36.10.10"));
        assertThat(pkt.getSourcePort(), is(9999));
        assertThat((pkt.getParentPacket()).getDestinationIP(), is("192.168.0.10"));
        assertThat(pkt.getDestinationPort(), is(7654));
        assertThat(((MACPacket)pkt.getParentPacket().getParentPacket()).getDestinationMacAddress(), is("00:00:00:00:00:00"));
        assertPayload(pkt, payload);

        // change stuff
        ((MACPacket)pkt.getParentPacket().getParentPacket()).setSourceMacAddress("12:13:14:15:16:17");
        assertThat(((MACPacket)pkt.getParentPacket().getParentPacket()).getSourceMacAddress(), is("12:13:14:15:16:17"));

        ((MACPacket)pkt.getParentPacket().getParentPacket()).setDestinationMacAddress("01:02:03:04:05:06");
        assertThat(((MACPacket)pkt.getParentPacket().getParentPacket()).getDestinationMacAddress(), is("01:02:03:04:05:06"));

        final TransportPacket pkt2 = serializeAndDeserialize(pkt);
        assertThat(((MACPacket)pkt.getPacket(Protocol.ETHERNET_II)).getSourceMacAddress(), is("12:13:14:15:16:17"));
        assertThat(((MACPacket)pkt.getPacket(Protocol.ETHERNET_II)).getDestinationMacAddress(), is("01:02:03:04:05:06"));
        assertThat((pkt2.getParentPacket()).getSourceIP(), is("10.36.10.10"));
        assertThat((pkt2.getParentPacket()).getDestinationIP(), is("192.168.0.10"));
        assertPayload(pkt2, payload);
    }

    private void assertPayload(final TransportPacket pkt, final String payload) {
        if (payload != null) {
            assertThat(pkt.getPayload().toString(), is(payload));
        } else {
            assertThat(pkt.getPayload(), is((Buffer) null));
        }
    }

}
