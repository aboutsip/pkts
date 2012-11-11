/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.EthernetFramer;
import com.aboutsip.yajpcap.framer.IPv4Framer;
import com.aboutsip.yajpcap.framer.Layer1Framer;
import com.aboutsip.yajpcap.framer.PcapFramer;
import com.aboutsip.yajpcap.packet.IPPacket;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class IPFrameTest extends YajTestBase {

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

    @Test
    public void testParsePacket() throws Exception {
        final Layer1Framer pcapFramer = new PcapFramer(this.defaultByteOrder, this.framerManager);
        final Layer1Frame pcapFrame = pcapFramer.frame((Frame) null, this.pcapStream);

        final EthernetFramer ethFramer = new EthernetFramer(this.framerManager);
        final EthernetFrame ethFrame = ethFramer.frame(pcapFrame, pcapFrame.getPayload());

        final IPv4Framer framer = new IPv4Framer(this.framerManager);
        final IPv4Frame frame = framer.frame(ethFrame, this.ipv4FrameBuffer);

        final IPPacket p = frame.parse();
        assertThat(p.getDestinationIP(), is("127.0.0.1"));
        assertThat(p.getSourceIP(), is("127.0.0.1"));

        assertThat(p.getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(p.getDestinationMacAddress(), is("00:00:00:00:00:00"));
        assertThat(p.getArrivalTime(), is(1340495109792862L));
    }

}
