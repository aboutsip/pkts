package io.pkts;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import io.pkts.packet.Packet;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PcapTest extends PktsTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testLoop() throws Exception {
        // there are 30 packets in this capture.
        final InputStream stream = PktsTestBase.class.getResourceAsStream("sipp.pcap");
        final Pcap pcap = Pcap.openStream(stream);
        final FrameHandlerImpl handler = new FrameHandlerImpl();
        pcap.loop(handler);
        pcap.close();
        assertThat(handler.count, is(30));
    }

    private static class FrameHandlerImpl implements PacketHandler {
        public int count;

        @Override
        public void nextPacket(final Packet packet) {
            try {
                final SipPacket sip = (SipPacket) packet.getPacket(Protocol.SIP);
                ++this.count;
            } catch (final Exception e) {
                e.printStackTrace();
                fail("ooops");
            }
        }
    }

}
