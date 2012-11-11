/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.PcapFramer;
import com.aboutsip.yajpcap.packet.PCapPacket;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class PcapFrameTest extends YajTestBase {

    private PcapFramer framer;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // remember, the defaultByteOrder etc has already been parsed out by
        // the test base
        this.framer = new PcapFramer(this.defaultByteOrder, this.framerManager);
    }

    /**
     * Make sure that the Packet that we get from {@link Frame#parse()} is
     * actually correct.
     * 
     * @throws Exception
     */
    @Test
    public void testParsePacket() throws Exception {
        // remember, this is the first packet in the pcap and it has already
        // been framed by the test base class
        final PCapPacket p = (PCapPacket) this.defaultPcapFrame.parse();
        assertThat(p.getTotalLength(), is(547L));
        assertThat(p.getCapturedLength(), is(547L));
        assertThat(p.getArrivalTime(), is(1340495109792454L));

        // all times have been copied from wireshark so we know they are good
        verifyNextPacket(this.pcapStream, 348, 1340495109792862L);
        verifyNextPacket(this.pcapStream, 507, 1340495109793013L);
        verifyNextPacket(this.pcapStream, 398, 1340495109793443L);
        verifyNextPacket(this.pcapStream, 547, 1340495110791005L);
        verifyNextPacket(this.pcapStream, 348, 1340495110791405L);

        // there are a total of 30 frames in this pcap.
        // Read all but the last
        for (int i = 6; i < 29; ++i) {
            this.framer.frame(null, this.pcapStream);
        }

        // now verify this last one
        verifyNextPacket(this.pcapStream, 340, 1340495114795676L);
    }

    private void verifyNextPacket(final Buffer in, final int expectedLength, final long expectedArrivalTime)
            throws Exception {
        final PcapFrame frame = this.framer.frame(null, in);
        final PCapPacket p = (PCapPacket) frame.parse();
        assertThat(p.getTotalLength(), is(((long) expectedLength)));
        assertThat(p.getCapturedLength(), is(((long) expectedLength)));
        assertThat(p.getArrivalTime(), is(expectedArrivalTime));
    }

}
