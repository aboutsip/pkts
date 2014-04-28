/**
 * 
 */
package io.pkts.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.framer.SllFramer;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.protocol.Protocol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class RTPFrameTest extends PktsTestBase {

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test frame an RTP packet all the way through.
     * 
     * @throws Exception
     */
    @Test
    public void testFrameRTP() throws Exception {
        final SllFramer framer = new SllFramer();
        final Buffer buffer = Buffers.wrap(RawData.rtp);
        final Packet frame = framer.frame(mock(PCapPacket.class), buffer);

        final RtpPacket rtp = (RtpPacket) frame.getPacket(Protocol.RTP);
        assertThat(rtp, not((RtpPacket) null));
        assertThat(rtp.getVersion(), is(2));
        assertThat(rtp.hasExtensions(), is(false));
        assertThat(rtp.hasPadding(), is(false));
        assertThat(rtp.hasMarker(), is(false));
        assertThat(rtp.getSeqNumber(), is(20937));
        assertThat(rtp.getTimestamp(), is(8396320L));
    }

    /**
     * Test frame an RTCP packet.
     * 
     * @throws Exception
     */
    @Test
    public void testFrameRTCP() throws Exception {
        final SllFramer framer = new SllFramer();
        final Buffer buffer = Buffers.wrap(RawData.rtcpSenderReportFullFrame);
        final Packet frame = framer.frame(mock(PCapPacket.class), buffer);
        final RtpPacket rtp = (RtpPacket) frame.getPacket(Protocol.RTP);

    }
}
