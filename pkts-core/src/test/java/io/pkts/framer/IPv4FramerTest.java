/**
 * 
 */
package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import io.pkts.RawData;
import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.Protocol;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author jonas@jonasborjesson.com
 */
public class IPv4FramerTest extends PktsTestBase {

    @Test(expected = IllegalArgumentException.class)
    public void testIPFramerNoParent() throws Exception {
        final IPv4Framer framer = new IPv4Framer();
        framer.frame(null, this.ipv4FrameBuffer);
    }

    @Test
    public void testFragmentedIpPacket() throws Exception {
        final EthernetFramer framer = new EthernetFramer();
        final MACPacket frame = framer.frame(mock(PCapPacket.class), Buffers.wrap(RawData.fragmented));
        final IPv4Packet ip = (IPv4Packet) frame.getPacket(Protocol.IPv4);
        assertThat(ip.isMoreFragmentsSet(), is(true));
    }

    /**
     * Loads a packet with 7 bytes of padding placed after the 4 byte UDP payload. Ensure the padding has been
     * removed by the IP framer.
     *
     * @throws Exception
     */
    @Test
    public void testPaddedIpPacket() throws Exception {
        final EthernetFramer framer = new EthernetFramer();
        final MACPacket frame = framer.frame(mock(PCapPacket.class), Buffers.wrap(RawData.padded));

        //The Ethernet packet should have "padding" at the end
        assertTrue(new String(frame.getPayload().getArray(), StandardCharsets.US_ASCII).endsWith("padding"));
        final UDPPacket udp = (UDPPacket) frame.getPacket(Protocol.UDP);
        //The UDP payload should have had the "padding" stripped, and now only be "data"
        assertThat(udp.getPayload().getArray(), is("data".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    public void testTruncatedIpPacket() throws Exception {
        final EthernetFramer framer = new EthernetFramer();
        final MACPacket frame = framer.frame(mock(PCapPacket.class), Buffers.wrap(RawData.truncatedFrame));
        // if the framer choked, it would throw an IndexOutOfBoundsException here
        frame.getPacket(Protocol.IPv4);
    }
}
