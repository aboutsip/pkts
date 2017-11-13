/**
 * 
 */
package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.frame.PcapRecordHeader;
import io.pkts.packet.IPPacket;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.PCapPacket;

import java.io.IOException;

import io.pkts.packet.impl.PCapPacketImpl;
import io.pkts.protocol.Protocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class PcapFramerTest extends PktsTestBase {

    private PcapFramer framer;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testPcapFramer() throws Exception {
        // length verified using wireshark. Remember, the first frame
        // has been framed and verified by the test base
        verifyNextFrame(this.pcapStream, 348);
        verifyNextFrame(this.pcapStream, 507);
        verifyNextFrame(this.pcapStream, 398);
        verifyNextFrame(this.pcapStream, 547);
        verifyNextFrame(this.pcapStream, 348);

        // there are a total of 30 frames in this pcap.
        PCapPacket frame = null;
        for (int i = 6; i < 30; ++i) {
            frame = this.framer.frame(null, this.pcapStream);
            assertNotNull(frame);
        }

        // the last frame is supposed to 340 according to wireshark
        assertThat(340, is(frame.getPayload().capacity()));

        // we have read all the 30 frames so trying to frame
        // another one shouldn't work. Hence, we should be getting
        // back a null frame, indicating that the fun is over
        frame = this.framer.frame(null, this.pcapStream);
        assertThat(frame, is((PCapPacket) null));

    }

    private void verifyNextFrame(final Buffer in, final int expectedLength)
            throws IOException, FramingException {
        final PCapPacket frame = this.framer.frame(null, in);
        final Buffer payload = frame.getPayload();
        assertThat(expectedLength, is(payload.capacity()));
    }

    /**
     * Populate a PCap packet with IP content (omitting the Ethernet layer) -
     * ensure it can be parsed as LINKTYPE_RAW.
     */
    @Test
    public void testRawLinktypeFraming() throws Exception {
        IPv4Packet ipPacket = (IPv4Packet) loadIPPackets("sipp.pcap").get(0);

        PCapPacketImpl pcapPacket = new PCapPacketImpl(
                PcapGlobalHeader.createDefaultHeader(Protocol.IPv4),
                PcapRecordHeader.createDefaultHeader(1),
                ipPacket.getParentPacket().getPayload());

        IPv4Packet parsedIpPacket = (IPv4Packet) pcapPacket.getNextPacket();

        assertThat(parsedIpPacket.getDestinationIP(), is(ipPacket.getDestinationIP()));
        assertThat(parsedIpPacket.getSourceIP(), is(ipPacket.getSourceIP()));
        assertThat(parsedIpPacket.getIpChecksum(), is(ipPacket.getIpChecksum()));
    }
}
