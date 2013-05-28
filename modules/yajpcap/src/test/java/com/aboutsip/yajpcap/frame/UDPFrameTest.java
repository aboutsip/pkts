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
import com.aboutsip.yajpcap.framer.UDPFramer;
import com.aboutsip.yajpcap.packet.TransportPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public class UDPFrameTest extends YajTestBase {

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
        final Layer1Framer pcapFramer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
        final Layer1Frame pcapFrame = pcapFramer.frame(null, this.pcapStream);

        final EthernetFramer ethFramer = new EthernetFramer(this.framerManager);
        final EthernetFrame ethFrame = ethFramer.frame(pcapFrame, pcapFrame.getPayload());

        final IPv4Framer ipv4Framer = new IPv4Framer(this.framerManager);
        final IPv4Frame ipv4Frame = ipv4Framer.frame(ethFrame, this.ipv4FrameBuffer);

        final UDPFramer framer = new UDPFramer(this.framerManager);
        final UDPFrame frame = framer.frame(ipv4Frame, this.udpFrameBuffer);
        final TransportPacket p = frame.parse();
        assertThat(p.getSourcePort(), is(5060));
        assertThat(p.getDestinationPort(), is(5090));

        assertThat(p.getDestinationIP(), is("127.0.0.1"));
        assertThat(p.getSourceIP(), is("127.0.0.1"));
        assertThat(p.getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(p.getDestinationMacAddress(), is("00:00:00:00:00:00"));
        assertThat(p.getArrivalTime(), is(1340495109792862L));
    }

}
